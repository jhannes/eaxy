package org.eaxy.usage;

import static org.eaxy.Xml.el;
import static org.fest.assertions.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.eaxy.Element;
import org.eaxy.html.EaxyHtml;
import org.junit.Test;

public class HtmlConvenienceTest {

    @Test
    public void shouldManipulateHtmlAttributes() {
        Element form = el("form",
                el("input").id("first_name_id").name("first_name").type("text").val("Darth"),
                el("input").id("last_name_id").name("last_name").type("text").val("Vader"),
                el("input").name("createPerson").type("submit").val("Create person"));
        assertThat(form.find("input").ids()).containsExactly("first_name_id", "last_name_id");
        assertThat(form.find("input").values()).containsExactly("Darth", "Vader", "Create person");
        assertThat(form.find("input").names()).containsExactly("first_name", "last_name", "createPerson");

        assertThat(form.find("input").first().val()).isEqualTo("Darth");
        assertThat(form.find("input").first().id()).isEqualTo("first_name_id");
        assertThat(form.find("input").first().name()).isEqualTo("first_name");
    }

    @Test
    public void shouldManipulateHtmlClass() {
        Element div = el("div");
        assertThat(div.className()).isNull();
        div.addClass("important");
        div.addClass("hidden");
        assertThat(div.className()).isEqualTo("important hidden");
        div.removeClass("hidden");
        div.removeClass("hidden");
        assertThat(div.className()).isEqualTo("important");
        div.removeClass("important");
        assertThat(div.className()).isEmpty();
    }

    @Test
    public void shouldFindByClassName() {
        Element ul = el("ul",
                el("li", "incorrect 1").addClass("firstClass"),
                el("li", "correct 1").addClass("matchingClass"),
                el("li", "correct 2").addClass("matchingClass"),
                el("li", "last element").addClass("lastClass"));
        assertThat(ul.find(".matchingClass").check().texts()).containsOnly("correct 1", "correct 2");
        assertThat(ul.find("li.lastClass").first().text()).isEqualTo("last element");
    }

    @Test
    public void shouldFindById() {
        Element ul = el("ul",
                el("li", "item 1").id("item-1"),
                el("li", "item 2").id("item-2"),
                el("li", "item 3").id("item-3"),
                el("li", "item 4").id("item-4"));
        assertThat(ul.find("#item-3").first().text()).isEqualTo("item 3");
        assertThat(ul.find("li#item-3").first().text()).isEqualTo("item 3");
    }

    @Test
    public void shouldSerializeForm() {
        Element form = el("div",
                el("form").id("form-id").addAll(
                        el("div", el("input").name("first_name")),
                        el("input").name("last_name")));
        form.find("#form-id", "...", "[name=first_name]").first().val("Johannes");
        form.find("#form-id", "...", "[name=last_name]").first().val("Brodwall");
        assertThat(EaxyHtml.serialize(form.find("#form-id")))
            .isEqualTo("first_name=Johannes&last_name=Brodwall");
        assertThat(EaxyHtml.formToMap(form.find("#form-id")).get("first_name"))
            .containsOnly("Johannes");
    }

    @Test
    public void shouldUpdateForm() {
        Map<String,String> values = new HashMap<String, String>();
        values.put("first_name", "Johannes");
        values.put("last_name", "Brodwall");
        Element form = el("div",
                el("form").id("form-id").addAll(
                        el("input").name("first_name"),
                        el("input").name("last_name")));
        EaxyHtml.updateForm(form.find("form#form-id"), values);
        assertThat(form.find("form", "[name=first_name]").first().val()).isEqualTo("Johannes");
        assertThat(form.find("form", "[name=last_name]").first().val()).isEqualTo("Brodwall");
    }
}
