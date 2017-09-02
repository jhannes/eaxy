package org.eaxy.usage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eaxy.Xml.el;
import static org.eaxy.Xml.text;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.Iterator;

import org.eaxy.Element;
import org.eaxy.ElementFilters;
import org.eaxy.Namespace;
import org.eaxy.NonMatchingPathException;
import org.junit.Test;

public class ElementFinderTest {

    @Test
    public void shouldExcludeNonmatchingElement() {
        assertThat(el("something").find("something-else")).isEmpty();
    }

    @Test
    public void shouldIncludeMatchingChild() {
        assertThat(el("root", el("something")).find("something")).contains(el("something"));
    }

    @Test
    public void shouldFindNestedElement() {
        Element xml = el("root", el("a", el("b")));
        assertThat(xml.find("a").find("b")).contains(el("b"));
    }

    @Test
    public void shouldFindByMultiplePathElements() {
        Element xml = el("root", el("a", el("b", el("c"))));
        assertThat(xml.find("a").find("b", "c")).contains(el("c"));
        assertThat(xml.find("a", "b", "c")).contains(el("c"));
    }

    @Test
    public void shouldFindTexts() {
        Element xml = el("div",
                el("p", "first para"),
                el("p", "second para"),
                el("p", text("para with "), el("b", "bold text")));
        assertThat(xml.find("p").check().texts()).containsExactly("first para", "second para", "para with bold text");
    }

    @Test
    public void shouldFindDeeplyNestedElements() {
        Element xml = el("div",
                el("p", "level 1a"),
                el("div", el("p", "level 2a")),
                el("p", "level 1b"),
                el("div", "nonmatch"),
                el("div", el("div", el("p", "level 3"))),
                el("div", el("p", "level 2b")));
        assertThat(xml.find("...", "p").texts())
            .containsExactly("level 1a", "level 2a", "level 1b", "level 3", "level 2b");

        assertThat(xml.find("...", "p").firstPath().getPath()).extracting(e -> e.tagName())
            .containsExactly("div", "p");
        assertThat(xml.find("...", "p").getPaths().get(1).getPath()).extracting(e -> e.tagName())
            .containsExactly("div", "div", "p");
    }

    @Test
    public void shouldIterateDeeplyNestedElements() {
        Element xml = el("div",
                el("p", "level 1a"),
                el("div", el("p", "level 2a")),
                el("p", "level 1b"),
                el("div", "nonmatch"),
                el("div", el("div", el("p", "level 3"))),
                el("div", el("p", "level 2b")));
        assertThat(ElementFilters.create("...", "p").iterate(new StringReader(xml.toXML())))
            .extracting(e -> e.text())
            .contains("level 1a", "level 2a", "level 1b", "level 3", "level 2b");
    }

    @Test
    public void shouldFindStrangelyNestedElements() {
        Element xml = el("div",
                el("div").id("not-here").add(text("something")),
                el("div").id("below-here").add(
                        el("div", el("div", el("p", text("around "), el("span", "HERE"), text(" around"))))));
        assertThat(xml.find("...", "#below-here", "...", "p", "...").single().text())
            .isEqualTo("HERE");
        assertThat(xml.find("...", "p", "...").single().text())
            .isEqualTo("HERE");
    }

    @Test
    public void shouldFindDescendantsAtSeveralLevels() {
        Element xml = el("section",
                el("div").id("top").addAll(
                        el("div").id("child-1"),
                        el("div").id("child-2")));
        assertThat(xml.find("...", "div").ids()).contains("top", "child-1", "child-2");
    }

    @Test
    public void shouldFindDissimilarChildren() {
        Element xml = el("div",
                el("h1", "header 1"),
                el("h2", "header 2"),
                el("div",
                        el("h2", "nested header 2")),
                el("h2", "second header 2"));
        assertThat(xml.find("*").tagNames()).contains("h1", "h2", "div", "h2");
        assertThat(xml.find("*", "h2").single().text()).isEqualTo("nested header 2");
    }

    @Test
    public void shouldFindChildrenByPosition() {
        Element xml = el("div",
                el("div", el("h1", "Wrong one")),
                el("div", el("h1", "Right one")));
        assertThat(xml.find("div", 1, "h1").check().texts()).containsOnly("Right one");
        assertThat(xml.find("div", 2, "h1").texts()).isEmpty();
    }

    @Test
    public void shouldFindAttributes() {
        Element xml = el("div",
                el("p", text("para with "), el("a", "a link").attr("href", "http://foo.com")),
                el("p", text("para with "), el("a", "another link").attr("href", "http://bar.com")),
                el("p", text("para with "), el("a", "anchor").attr("name", "something")),
                el("p", text("para with "), el("b", "bold text")));
        assertThat(xml.find("p", "a").check().attrs("href"))
            .containsExactly("http://foo.com", "http://bar.com");
    }

    @Test
    public void shouldMatchOnNamespace() {
        Namespace A_NS = new Namespace("uri:a", "a");
        Namespace A_NS_WITH_OTHER_PREFIX = new Namespace("uri:a", "a2");
        Namespace B_NS = new Namespace("uri:b", "b");

        Element xml = A_NS.el("root", A_NS.el("parent", A_NS.el("child")));
        assertThat(xml.find("parent", "child")).isNotEmpty();
        assertThat(xml.find(A_NS.name("parent"), A_NS.name("child"))).isNotEmpty();
        assertThat(xml.find(A_NS.name("parent"), A_NS_WITH_OTHER_PREFIX.name("child"))).isNotEmpty();

        assertThat(xml.find(A_NS.name("parent"), B_NS.name("child"))).isEmpty();
    }

    @Test
    public void shouldMatchAnyOnNoNamespace() {
        Namespace SOAP_NS = new Namespace("http://schemas.xmlsoap.org/soap/envelope/", "S");
        SOAP_NS.el("Envelope", SOAP_NS.el("Body", el("something"))).find("Body", "*").check();
    }

    @Test
    public void shouldMatchOnAttribute() {
        Namespace NS = new Namespace("uri:a", "a");
        Element xml = NS.el("parent",
                NS.el("child", "wrong").attr(NS.name("included"), "false"),
                NS.el("child", "right").attr(NS.name("included"), "true"));
        assertThat(xml.find(NS.attr("included", "true")).single().text()).isEqualTo("right");
        assertThat(xml.find("[included=true]").single().text()).isEqualTo("right");
    }

    @Test
    public void shouldIterateOnAttribute() {
        Namespace NS = new Namespace("uri:a", "a");
        Element xml = NS.el("parent",
                NS.el("child", "wrong").attr(NS.name("included"), "false"),
                NS.el("child", "right").attr(NS.name("included"), "true"));
        assertThat(ElementFilters.create(NS.attr("included", "true")).iterate(new StringReader(xml.toXML())))
            .extracting(e -> e.text())
            .containsExactly("right");
    }

    @Test
    public void shouldKeepFullPath() {
        Element xml = el("root", el("a", el("b", el("c"))));
        assertThat(xml.find("a").find("b", "c").getPath()).containsExactly("root", "a", "b", "c");
    }

    @Test
    public void shouldCheckPath() {
        Element xml = el("root", el("a", el("b"), el("b")));
        xml.find("a").check().find("b").check();
    }

    @Test
    public void shouldThrowOnMissingPath() {
        Namespace NS = new Namespace("http://a.org/b/", "a");
        Element xml = el("root", el("top", el("parent", el("actual-child"), NS.el("actual-child"))));
        try {
            xml.find("top", "parent", "searched-child", "foo").check();
            fail("expected exception");
        } catch (NonMatchingPathException e) {
            assertThat(e.getMessage())
                .contains("below [root, top, parent]")
                .contains("Can't find <searched-child>")
                .doesNotContain("foo")
                .contains("\"http://a.org/b/\":actual-child");
        }
    }

    @Test
    public void shouldIterateOverFiles() {
        URL file = getClass().getResource("/medsample-mini.xml");
        Iterator<Element> it = ElementFilters.create("MedlineCitation").iterate(file).iterator();
        int count = 0;
        while (it.hasNext()) {
            it.next();
            count++;
        }
        assertThat(count).isEqualTo(1);
    }

    @Test
    public void scansShouldBeFast() throws IOException {
        long startTime = System.currentTimeMillis();
        URL file = new File("src/test/xml/performance-suite/medsamp2012.xml.gz").toURI().toURL();
        int maxReferences = Integer.MIN_VALUE;
        Element mostReferenced = null;
        for (Element element : ElementFilters.create("MedlineCitation").iterate(file)) {
            Element references = element.find("NumberOfReferences").singleOrDefault();
            if (references != null) {
                int numberOfReferences = Integer.parseInt(references.text());
                if (numberOfReferences > maxReferences) {
                    mostReferenced = element;
                }
            }
        }
        assertThat(System.currentTimeMillis() - startTime).as("millis").isLessThan(1000);
        assertThat(mostReferenced.find("Article", "ArticleTitle").single().text())
            .isEqualTo("Outcome of patients with sepsis and septic shock after ICU treatment.");
    }

}
