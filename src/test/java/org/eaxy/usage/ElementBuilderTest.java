package org.eaxy.usage;

import static org.eaxy.Xml.el;
import static org.eaxy.Xml.text;
import static org.eaxy.Xml.xml;
import static org.fest.assertions.api.Assertions.assertThat;

import org.eaxy.Element;
import org.eaxy.MalformedXMLException;
import org.eaxy.Namespace;
import org.junit.Test;

public class ElementBuilderTest {

    @Test
    public void shouldSerializeEmptyElement() {
        assertThat(el("foo").toXML()).isEqualTo("<foo />");
    }

    @Test
    public void shouldSerializeElementWithText() {
        assertThat(el("foo", text("hello world")).toXML()).isEqualTo("<foo>hello world</foo>");
    }

    @Test
    public void shouldSerializeNestedElements() {
        assertThat(el("foo", el("bar", "hello"), el("baz", "world")).toXML())
            .isEqualTo("<foo><bar>hello</bar><baz>world</baz></foo>");
    }

    @Test
    public void shouldSerializeAttributes() {
        assertThat(el("foo", el("bar", "gz").attr("href", "http://a.com")).attr("alt", "test").toXML())
            .isEqualTo("<foo alt=\"test\"><bar href=\"http://a.com\">gz</bar></foo>");
    }

    @Test
    public void shouldSerializeAttributesWithSpecialChars() {
        assertThat(el("foo").attr("attr", "This is \"<\" - a less than sign").attr("attr"))
            .isEqualTo("This is \"<\" - a less than sign");
        assertThat(el("foo").attr("attr", "This is \"<\" - a less than sign").toXML())
            .isEqualTo("<foo attr=\"This is &quot;&lt;&quot; - a less than sign\" />");
    }

    @Test
    public void shouldPrintElementWithNameSpace() {
        Namespace SOAP_NS = new Namespace("http://soap.com");
        assertThat(SOAP_NS.el("Envelope").toXML()).isEqualTo("<Envelope xmlns=\"http://soap.com\" />");
    }

    @Test
    public void shouldPrintNamespacePrefix() {
        Namespace SOAP_NS = new Namespace("http://soap.com", "SOAP");
        assertThat(SOAP_NS.el("Envelope").toXML()).isEqualTo("<SOAP:Envelope xmlns:SOAP=\"http://soap.com\" />");
    }

    @Test
    public void shouldPrintAttributeNamespaces() {
        Namespace A1_NS = new Namespace("uri:a1", "a");
        Namespace A2_NS = new Namespace("uri:a2", "b");

        assertThat(A1_NS.el("foo").attr(A2_NS.name("bar"), "test").toXML())
            .isEqualTo("<a:foo xmlns:a=\"uri:a1\" xmlns:b=\"uri:a2\" b:bar=\"test\" />");
    }

    @Test
    public void shouldOnlyPrintNamespaceOnce() {
        Namespace A_NS = new Namespace("uri:a", "a");
        assertThat(A_NS.el("foo").attr(A_NS.name("first"), "one").attr(A_NS.name("second"), "two").toXML())
            .isEqualTo("<a:foo xmlns:a=\"uri:a\" a:first=\"one\" a:second=\"two\" />");
    }

    @Test
    public void shouldNotPrintNestedNamespaces() {
        Namespace SOAP_NS = new Namespace("http://soap.com", "SOAP");
        assertThat(SOAP_NS.el("Envelope", SOAP_NS.el("Header"), SOAP_NS.el("Body", SOAP_NS.el("SubBody"))).toXML())
            .isEqualTo("<SOAP:Envelope xmlns:SOAP=\"http://soap.com\"><SOAP:Header /><SOAP:Body><SOAP:SubBody /></SOAP:Body></SOAP:Envelope>");
    }

    @Test
    public void shouldNotPrintAncestorNamespace() {
        Namespace A_NS = new Namespace("http://a.com", "a");
        Namespace B_NS = new Namespace("http://b.com", "b");
        assertThat(A_NS.el("Ancestor", B_NS.el("Child", A_NS.el("GrandChild"))).toXML())
            .isEqualTo("<a:Ancestor xmlns:a=\"http://a.com\">" +
                    "<b:Child xmlns:b=\"http://b.com\"><a:GrandChild /></b:Child>" +
                    "</a:Ancestor>");

    }

    @Test
    public void shouldPrintSiblingNamespaces() {
        Namespace SOAP_NS = new Namespace("http://soap.com", "S");
        assertThat(el("Super", SOAP_NS.el("Envelope"), SOAP_NS.el("Body")).toXML())
            .isEqualTo("<Super><S:Envelope xmlns:S=\"http://soap.com\" /><S:Body xmlns:S=\"http://soap.com\" /></Super>");
    }

    @Test
    public void shouldEscapeHtmlCharacters() {
        Element element = el("Element", "Text with <, > and &");
        assertThat(element.toXML()).isEqualTo("<Element>Text with &lt;, &gt; and &amp;</Element>");
        assertThat(element.text()).isEqualTo("Text with <, > and &");

    }

    @Test(expected=MalformedXMLException.class)
    public void shouldRejectIncompleteXml() {
        xml("<unclosed-element>Unclosed");
    }

    @Test(expected=MalformedXMLException.class)
    public void shouldRejectUnmatchedXml() {
        xml("<open-tag>Malformed</close-tag>");
    }

    @Test(expected=MalformedXMLException.class)
    public void shouldRejectDoubleRootedXml() {
        xml("<first-root /><second-root />");
    }

    @Test
    public void shouldNotPrintDeclaredNamespaces() {
        Namespace SOAP_NS = new Namespace("http://soap.com", "S");
        assertThat(el("Super",
                    SOAP_NS.el("Envelope"),
                    SOAP_NS.el("Body"))
                .xmlns(SOAP_NS).toXML())
            .isEqualTo("<Super xmlns:S=\"http://soap.com\"><S:Envelope /><S:Body /></Super>");
    }

    @Test
    public void shouldReadXml() {
        Namespace SOAP_NS = new Namespace("http://soap.com", "S");
        Namespace INNER_NS = new Namespace("http://inner.com", "i");
        String xml = el("Super",
                    SOAP_NS.el("Envelope"),
                    SOAP_NS.el("Body", INNER_NS.el("content", "some string")))
                .xmlns(SOAP_NS).toXML();
        assertThat(xml(xml).getRootElement().toXML()).isEqualTo(xml);
    }

    @Test
    public void shouldReadDocument() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<super>Some text<!-- only a comment --></super>";
        assertThat(xml(xml).toXML()).isEqualTo(xml);
    }

    @Test
    public void shouldReadDocType() {
        String docType = "<!DOCTYPE MedlineCitationSet PUBLIC \"-//NLM//DTD Medline Citation, 1st January, 2012//EN\" \"http://www.nlm.nih.gov/databases/dtd/nlmmedlinecitationset_120101.dtd\">";
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                docType +
                "\n<super>Some text<!-- only a comment --></super>";
        assertThat(xml(xml).toXML()).isEqualTo(xml);
    }

    @Test
    public void shouldReadXmlComment() {
        String xml = "<super>Some text<!-- only a comment --></super>";
        assertThat(xml(xml).getRootElement().toXML()).isEqualTo(xml);
    }

    @Test
    public void shouldReadCDATA() {
        String xml = "<super>Some text<![CDATA[ some cdata text ]]></super>";
        assertThat(xml(xml).getRootElement().toXML()).isEqualTo(xml);
    }

    @Test
    public void shouldReadPrefixedNamespace() {
        String xml = "<s:super xmlns:s=\"uri:test\">some data</s:super>";
        assertThat(xml(xml).getRootElement().toXML()).isEqualTo(xml);
    }

    @Test
    public void shouldReadDefaultNamespace() {
        String xml = "<super xmlns=\"uri:test\">some data</super>";
        assertThat(xml(xml).getRootElement().toXML()).isEqualTo(xml);
    }

    @Test
    public void shouldReadHtml() {
        String xml = el("html",
                el("body",
                        el("div",
                                text("Me thinks"),
                                el("i", "it"),
                                text("looks"),
                                el("strong", "like a"),
                                text("weasel")
                                ))).toXML();
        assertThat(xml(xml).getRootElement().toXML()).isEqualTo(xml);
    }

}
