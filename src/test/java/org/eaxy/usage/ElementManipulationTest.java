package org.eaxy.usage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eaxy.Xml.el;
import static org.eaxy.Xml.text;

import org.eaxy.Element;
import org.junit.Test;

public class ElementManipulationTest {

    @Test
    public void shouldUpdateText() {
        Element xml = el("div",
                el("p", text("hello "), el("b", "world (in bold)"), text(" whassup?")));
        assertThat(xml.text()).isEqualTo("hello world (in bold) whassup?");
        xml.text("New text");
        assertThat(xml.text()).isEqualTo("New text");
    }

    @Test
    public void shouldAppendElements() {
        Element xml = el("div");
        xml.addAll(text("hello "), el("b", "brave"), text(" world"));
        assertThat(xml.text()).isEqualTo("hello brave world");
        assertThat(xml.find("b").single().text()).isEqualTo("brave");
    }

    @Test
    public void shouldUpdateAttributes() {
        Element xml = el("ul",
                el("li", "first"),
                el("li", "second"),
                el("li", "third"));
        xml.find("li").attr("class", "new-class");
        assertThat(xml.find("li").first().className()).isEqualTo("new-class");
    }

    @Test
    public void shouldUseFragmentAsTemplate() throws Exception {
        Element xml = el("ul",
                el("li", "some text"),
                el("p", "other text"));
        Element template = xml.take("li");
        xml.add(template.copy().text("foo"));
        xml.add(template.copy().text("bar"));
        xml.add(template.copy().text("baz"));
        assertThat(xml.find("li").check().texts()).containsExactly("foo", "bar", "baz");
        assertThat(xml.find("p").check().texts()).containsExactly("other text");
    }

}
