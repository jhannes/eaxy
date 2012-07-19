package org.eaxy.usage;

import static org.eaxy.Xml.el;
import static org.eaxy.Xml.text;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

import java.util.Arrays;

import org.eaxy.Element;
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
                el("div", el("div", el("p", "level 3"))),
                el("div", el("p", "level 2b")));
        assertThat(xml.find("...", "p").texts())
            .containsExactly("level 1a", "level 2a", "level 1b", "level 3", "level 2b");
    }

    @Test
    public void shouldFindStrangelyNestedElements() {
        Element xml = el("div",
                el("div").id("not-here").add(text("something")),
                el("div").id("below-here").add(
                        el("div", el("div", el("p", text("around "), el("span", "HERE"), text(" around"))))));
        assertThat(xml.find("...", "#below-here", "...", "p", "...").first().text())
            .isEqualTo("HERE");
        assertThat(xml.find("...", "p", "...").first().text())
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
        assertThat(xml.find("*", "h2").first().text()).isEqualTo("nested header 2");
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
    public void shouldMatchOnAttribute() {
        Namespace NS = new Namespace("uri:a", "a");
        Element xml = NS.el("parent",
                NS.el("child", "wrong").attr(NS.name("included"), "false"),
                NS.el("child", "right").attr(NS.name("included"), "true"));
        assertThat(xml.find(NS.attr("included", "true")).check().texts()).containsExactly("right");
        assertThat(xml.find("[included=true]").check().texts()).containsExactly("right");
    }

    @Test
    public void shouldKeepFullPath() {
        Element xml = el("root", el("a", el("b", el("c"))));
        assertThat(xml.find("a").find("b", "c").getPath()).isEqualTo(
                Arrays.asList("root", "a", "b", "c"));
    }

    @Test
    public void shouldCheckPath() {
        Element xml = el("root", el("a", el("b"), el("b")));
        xml.find("a").check().find("b").check();
    }

    @Test
    public void shouldThrowOnMissingPath() {
        Element xml = el("root", el("top", el("parent", el("actual-child"), el("actual-child"))));
        try {
            xml.find("top", "parent", "searched-child", "foo").check();
            fail("expected exception");
        } catch (NonMatchingPathException e) {
            assertThat(e.getMessage())
                .contains("below [root, top, parent]")
                .contains("Can't find <searched-child>")
                .excludes("foo")
                .contains("actual-child");
        }
    }


}
