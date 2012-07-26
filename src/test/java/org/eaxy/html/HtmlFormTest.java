package org.eaxy.html;

import static org.eaxy.Xml.attr;
import static org.eaxy.Xml.el;
import static org.fest.assertions.Assertions.assertThat;

import org.eaxy.Element;
import org.junit.Test;

public class HtmlFormTest {

    @Test
    public void shouldShowFormValue() {
        Element html = el("form", el("div",
                el("input").name("text_field").val("input value").type("text"),
                el("textarea").name("text_area").text("text area content"),
                el("input").name("radio_field").val("first").type("radio").checked(false),
                el("input").name("radio_field").val("second").type("radio").checked(true),
                el("input").name("checked_box").type("checkbox").checked(true),
                el("input").name("unchecked_box").type("checkbox").checked(false),
                el("select").name("select_field").addAll(
                        el("optgroup",
                            el("option", "Option name").val("option1"),
                            el("option", "Option name").val("option2").selected(true)),
                        el("option", "Option name").val("option3")
                        )
                ));
        HtmlForm form = new HtmlForm(html);
        assertThat(form.get("text_field")).isEqualTo("input value");
        assertThat(form.get("text_area")).isEqualTo("text area content");
        assertThat(form.get("radio_field")).isEqualTo("second");
        assertThat(form.get("checked_box")).isEqualTo("on");
        assertThat(form.get("unchecked_box")).isNull();
        assertThat(form.get("select_field")).isEqualTo("option2");
    }

    @Test
    public void shouldShowMultipleValues() {
        Element html = el("form",
                el("input").name("text_field").val("input value").type("text"),
                el("input").name("text_field").val("another value").type("text"),
                el("textarea").name("text_area").text("text area content"),
                el("textarea").name("text_area").text("second area content"),
                el("input").name("text_area_or_field").val("text in field").type("text"),
                el("textarea").name("text_area_or_field").text("text in area"),
                el("input").name("checked_box").type("checkbox").checked(true),
                el("input").name("checked_box").type("checkbox").checked(false),
                el("input").name("checked_box").type("checkbox").checked(true),
                el("input").name("radio_field").val("first").type("radio").checked(true),
                el("input").name("radio_field").val("second").type("radio").checked(true),
                el("select").name("select_field").addAll(
                        el("option", "Option name").val("option1"),
                        el("option", "Option name").val("option2").selected(true)),
                el("select").name("select_field").addAll(
                        el("option", "Option name").val("option1").selected(true),
                            el("option", "Option name").val("option2"))
                        );
        HtmlForm form = new HtmlForm(html);
        assertThat(form.getAll("text_field")).containsExactly("input value", "another value");
        assertThat(form.getAll("text_area")).containsExactly("text area content", "second area content");
        assertThat(form.getAll("text_area_or_field")).containsExactly("text in field", "text in area");
        assertThat(form.getAll("checked_box")).containsExactly("on", "on");
        assertThat(form.getAll("select_field")).containsExactly("option2", "option1");
        assertThat(form.getAll("radio_field")).containsExactly("second");
    }

    @Test
    public void shouldHandleRadioAndTextInputsWithSameName() {
        Element html = el("form",
                el("input").name("field_name").val("input value").type("text"),
                el("input").name("field_name").val("first_radio").type("radio").checked(true),
                el("input").name("field_name").val("second_radio").type("radio").checked(true),
                el("input").name("field_name").val("another value").type("text"),
                el("input").name("field_name").val("third_radio").type("radio").checked(false),
                el("input").type("submit")
                );
        System.out.println(html.toXML());
        HtmlForm form = new HtmlForm(html);
        assertThat(form.get("field_name")).isEqualTo("input value");
        assertThat(form.getAll("field_name"))
            .containsExactly("input value", "second_radio", "another value");
    }

    @Test
    public void shouldUpdateValues() {
        Element html = el("form", el("div",
                el("input").name("text_field").val("input value").type("text"),
                el("textarea").name("text_area").text("text area content"),
                el("input").name("radio_field").val("first").type("radio").checked(false),
                el("input").name("radio_field").val("second").type("radio").checked(true),
                el("input").name("checked_box").type("checkbox").checked(true),
                el("input").name("unchecked_box").type("checkbox").checked(false),
                el("select").name("select_field").addAll(
                        el("optgroup",
                            el("option", "Option name").val("option1"),
                            el("option", "Option name").val("option2").selected(true)),
                        el("option", "Option name").val("option3")
                        )
                ));
        HtmlForm form = new HtmlForm(html);
        assertThat(form.set("text_field", "new value").get("text_field"))
            .isEqualTo("new value");
        assertThat(form.set("text_area", "new text").get("text_area"))
            .isEqualTo("new text");
        assertThat(form.set("radio_field", "first").get("radio_field"))
            .isEqualTo("first");
        assertThat(html.find("...", attr("name", "radio_field")).values()).contains("first", "second");
        assertThat(form.set("checked_box", null).get("checked_box"))
            .isNull();
        assertThat(form.set("unchecked_box", "on").get("unchecked_box"))
            .isEqualTo("on");
        assertThat(form.set("select_field", "option1").get("select_field"))
            .isEqualTo("option1");
    }

}
