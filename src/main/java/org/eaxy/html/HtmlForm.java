package org.eaxy.html;

import static org.eaxy.Xml.attr;

import java.util.ArrayList;
import java.util.List;

import org.eaxy.Element;
import org.eaxy.ElementSet;

public class HtmlForm {

    private final Element formElement;

    public HtmlForm(Element formElement) {
        this.formElement = formElement;
    }

    public HtmlForm set(String parameterName, String value) {
        ElementSet elements = formElement.find("...", attr("name", parameterName));
        for (Element element : elements) {
            if (element.tagName().equalsIgnoreCase("textarea")) {
                element.text(value);
            } else if ("select".equalsIgnoreCase(element.tagName())) {
                for (Element option : element.find("...", "option")) {
                    option.selected(option.val().equalsIgnoreCase(value));
                }
            } else if ("checkbox".equalsIgnoreCase(element.type())) {
                element.checked(value != null);
            } else if ("radio".equalsIgnoreCase(element.type())) {
                for (Element radio : elements) {
                    radio.checked(radio.val().equalsIgnoreCase(value));
                }
                return this;
            } else {
                element.attr("value", value);
            }
        }
        return this;
    }

    public String get(String parameterName) {
        ElementSet elementSet = formElement.find("...", attr("name", parameterName));
        Element element = elementSet.first();
        if ("radio".equalsIgnoreCase(element.type())) {
            return selectedRadio(elementSet, element.name()).val();
        }
        return value(element, elementSet);
    }

    public List<String> getAll(String parameterName) {
        List<String> arrayList = new ArrayList<String>();
        ElementSet elementSet = formElement.find("...", attr("name", parameterName));
        for (Element element : elementSet) {
            String value = value(element, elementSet);
            if (value != null) {
                arrayList.add(value);
            }
        }
        return arrayList;
    }

    private String value(Element element, ElementSet elementSet) {
        if ("radio".equalsIgnoreCase(element.type())) {
            if (element == selectedRadio(elementSet, element.name())) {
                return element.val();
            } else {
                return null;
            }
        }
        if (element.tagName().equalsIgnoreCase("textarea")) {
            return element.text();
        }
        if (element.tagName().equalsIgnoreCase("select")) {
            for (Element option : element.find("...", "option")) {
                if (option.selected()) return option.val();
            }
            return null;
        }
        if ("checkbox".equalsIgnoreCase(element.type())) {
            return element.checked() ? "on" : null;
        }
        return element.val();
    }

    private Element selectedRadio(ElementSet elementSet, String name) {
        Element result = null;
        for (Element element : elementSet) {
            if (element.name().equalsIgnoreCase(name) && element.checked()) {
                result = element;
            }
        }
        return result;
    }

}
