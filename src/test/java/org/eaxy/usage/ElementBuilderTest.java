package org.eaxy.usage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eaxy.Xml.attr;
import static org.eaxy.Xml.el;
import static org.eaxy.Xml.text;
import static org.eaxy.Xml.xml;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eaxy.Document;
import org.eaxy.Element;
import org.eaxy.MalformedXMLException;
import org.eaxy.Namespace;
import org.eaxy.QualifiedName;
import org.eaxy.StaxReader;
import org.eaxy.Xml;
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
        assertThat(el("foo", el("bar", text("gz"), attr("href", "http://a.com"))).attr("alt", "test").toXML())
            .isEqualTo("<foo alt=\"test\"><bar href=\"http://a.com\">gz</bar></foo>");
    }

    @Test
    public void shouldGetAllAttributes() {
        Map<String, String> attrs = el("element", attr("abc", "a"), attr("xyz", "b"), attr("def", "c")).attrs();
        assertThat(attrs.keySet()).containsExactly("abc", "xyz", "def");
        assertThat(attrs.values()).containsExactly("a", "b", "c");
    }

    @Test
    public void shouldCreateAttributes() throws Exception {
        assertThat(el("element", attr("a", "b")).hasAttr("a")).isTrue();
    }

    @Test
    public void shouldSerializeAttributesWithSpecialChars() {
        assertThat(el("foo").attr("attr", "This is \"<\" - a less than sign").attr("attr"))
            .isEqualTo("This is \"<\" - a less than sign");
        assertThat(el("foo").attr("attr", "This is \"<\" - a less than sign").toXML())
            .isEqualTo("<foo attr=\"This is &quot;&lt;&quot; - a less than sign\" />");
    }

    @Test
    public void shouldCreateAttributesAsArguments() {
        assertThat(el("foo", attr("attr", "This is \"<\" - a less than sign")).attr("attr"))
            .isEqualTo("This is \"<\" - a less than sign");
        assertThat(el("foo", attr("attr", "This is \"<\" - a less than sign")).toXML())
            .isEqualTo("<foo attr=\"This is &quot;&lt;&quot; - a less than sign\" />");
    }

    @Test
    public void shouldCreateDocument() throws Exception {
        Document doc = Xml.doc(Xml.el("empty"));
        doc.setVersion("1.1");
        doc.setEncoding("iso-8859-1");
        assertThat(doc.copy().toXML()).contains("<?xml version=\"1.1\" encoding=\"iso-8859-1\"?>");
    }

    @Test
    public void shouldPrintElementWithNameSpace() {
        Namespace SOAP_NS = new Namespace("http://soap.com");
        assertThat(SOAP_NS.el("Envelope").copy().toXML()).isEqualTo("<Envelope xmlns=\"http://soap.com\" />");
    }

    @Test
    public void shouldPrintNamespacePrefix() {
        Namespace SOAP_NS = new Namespace("http://soap.com", "SOAP");
        assertThat(SOAP_NS.el("Envelope").copy().toXML()).isEqualTo("<SOAP:Envelope xmlns:SOAP=\"http://soap.com\" />");
    }

    @Test
    public void shouldPrintAttributeNamespaces() {
        Namespace A1_NS = new Namespace("uri:a1", "a");
        Namespace A2_NS = new Namespace("uri:a2", "b");

        assertThat(A1_NS.el("foo").attr(A2_NS.name("bar"), "test").copy().toXML())
            .isEqualTo("<a:foo xmlns:a=\"uri:a1\" xmlns:b=\"uri:a2\" b:bar=\"test\" />");
    }

    @Test
    public void shouldOnlyPrintNamespaceOnce() {
        Namespace A_NS = new Namespace("uri:a", "a");
        assertThat(A_NS.el("foo").attr(A_NS.name("first"), "one").attr(A_NS.name("second"), "two").copy().toXML())
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

    @Test
    public void shouldMapElementsOverList() {
        List<String> data = Arrays.asList("a", "b", "c");
        Element element = el("ul", Xml.map(data, s -> el("li", s)));
        assertThat(element.toXML()).isEqualTo("<ul><li>a</li><li>b</li><li>c</li></ul>");
    }

    @Test
    public void shouldPrintIndentedXml() throws Exception {
        Document doc = Xml.doc(Xml.el("root",
                Xml.el("first", "with some text"),
                Xml.el("empty").attr("foo", "bar"),
                Xml.el("second", Xml.el("nested", "Indented at level 2"))));

        String nl = Document.LINE_SEPARATOR;
        assertThat(doc.toIndentedXML("***"))
            .isEqualTo(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + nl +
                    "<root>" + nl +
                    "***<first>with some text</first>" + nl +
                    "***<empty foo=\"bar\" />" + nl +
                    "***<second>" + nl +
                    "******<nested>Indented at level 2</nested>" + nl +
                    "***</second>" + nl +
                    "</root>" + nl
                    );
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
    public void shouldUseNamespacesDeclaredInParent() {
        Namespace SOAP_NS = new Namespace("http://soap.com", "S");
        assertThat(xml(el("Super", SOAP_NS,
                    SOAP_NS.el("Envelope"),
                    SOAP_NS.el("Body"))
                .copy().toXML()).getRootElement().toXML())
            .isEqualTo("<Super xmlns:S=\"http://soap.com\"><S:Envelope /><S:Body /></Super>");
    }

    @Test
    public void shouldReadXml() {
        Namespace SOAP_NS = new Namespace("http://soap.com", "S");
        Namespace INNER_NS = new Namespace("uri:inner", "i");
        String xml = el("Super", SOAP_NS,
                    SOAP_NS.el("Envelope"),
                    SOAP_NS.el("Body", INNER_NS.el("content", "some string"))).copy().toXML();
        Document doc = xml(xml);
        assertThat(doc.getRootElement().toXML()).isEqualTo(xml);
    }

    @Test
    public void shouldHandleAttributesOnNamespaces() throws Exception {
        // <a:foo xmlns:a="uri:a" a:first="one" a:second="two" />
        Namespace A_NS = new Namespace("uri:a", "a");
        Element xml = A_NS.el("foo", A_NS.attr("first", "one"), A_NS.attr("second", "two"));
        Document doc = xml(xml.copy().toXML());
        assertThat(doc.getRootElement().toXML()).isEqualTo("<a:foo xmlns:a=\"uri:a\" a:first=\"one\" a:second=\"two\" />");
    }


    @Test
    public void shouldReadDocument() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + Document.LINE_SEPARATOR
                + "<super>Some text<!-- only a comment --></super>";
        assertThat(xml(xml).copy().toXML()).isEqualTo(xml);
    }

    @Test
    public void shouldReadDocType() {
        String docType = "<!DOCTYPE MedlineCitationSet PUBLIC \"-//NLM//DTD Medline Citation, 1st January, 2012//EN\" \"http://www.nlm.nih.gov/databases/dtd/nlmmedlinecitationset_120101.dtd\">";
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                Document.LINE_SEPARATOR +
                docType +
                Document.LINE_SEPARATOR +
                "<super>Some text<!-- only a comment --></super>";
        assertThat(xml(xml).copy().toXML()).isEqualTo(xml);
    }

    @Test
    public void shouldReadEncoding() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>"
                + Document.LINE_SEPARATOR
                + "<empty />";
        assertThat(StaxReader.read(new ByteArrayInputStream(xml.getBytes()), null).copy().toXML())
            .isEqualTo(xml);
    }

    @Test
    public void shouldReadXmlComment() {
        String xml = "<super>Some text<!-- only a comment --></super>";
        assertThat(xml(xml).getRootElement().toXML()).isEqualTo(xml);
    }

    @Test
    public void shouldTranslateWithAttributeNamespaces() throws Exception {
        String text = "<msg:message xmlns:msg=\"http://eaxy.org/test/mailmessage\" msg:type=\"email\" other=\"true\" />";
        Element email = StaxReader.read(new StringReader(text))
                .getRootElement();

        org.w3c.dom.Document dom = Xml.toDom(new Document(email));
        Element transformed = Xml.fromDom(dom).getRootElement();
        assertThat(transformed.toXML())
            .isEqualTo(email.toXML());
    }

    @Test
    public void shouldReadWithAttributeNamespaces() throws Exception {
        String text = "<msg:message xmlns:msg=\"http://eaxy.org/test/mailmessage\" msg:type=\"email\" other=\"true\" />";
        Element email = StaxReader.read(new StringReader(text)).getRootElement();
        QualifiedName attrName = new QualifiedName("http://eaxy.org/test/mailmessage", "msg:type");
        assertThat(email.attr(attrName)).isEqualTo("email");
        assertThat(email.attr(new QualifiedName("other"))).isEqualTo("true");
    }

    @Test
    public void shouldReadCDATA() {
        String xml = "<super>Some text<![CDATA[ some cdata text ]]></super>";
        assertThat(xml(xml).getRootElement().toXML()).isEqualTo(xml);
    }

    @Test
    public void shouldReadPrefixedNamespace() {
        String xml = "<s:super xmlns:s=\"uri:test\"><s:sub>some data</s:sub></s:super>";
        assertThat(xml(xml).getRootElement().toXML()).isEqualTo(xml);
        assertThat(xml(xml).find(new Namespace("uri:test", "s").name("sub")).single().text())
            .isEqualTo("some data");
    }

    @Test
    public void shouldReadDefaultNamespace() {
        String xml = "<super xmlns=\"uri:test\">some data</super>";
        assertThat(xml(xml).getRootElement().copy().toXML()).isEqualTo(xml);
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
                                ))).copy().toXML();
        assertThat(xml(xml).getRootElement().toXML()).isEqualTo(xml);
    }

}
