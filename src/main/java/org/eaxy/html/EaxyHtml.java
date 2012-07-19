package org.eaxy.html;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eaxy.Element;
import org.eaxy.ElementSet;

public abstract class EaxyHtml {

    public static String serialize(ElementSet form) {
        return serialize(form.first());
    }

    public static String serialize(Element form) {
        StringBuilder result = new StringBuilder();
        for (Entry<String, List<String>> entry : formToMap(form).entrySet()) {
            if (result.length() > 0) result.append("&");
            for (String value : entry.getValue()) {
                // TODO: URL encode
                result.append(entry.getKey()).append("=").append(value);
            }
        }
        return result.toString();
    }

    public static ElementSet updateForm(ElementSet forms, Map<String, String> values) {
        for (Element form : forms) {
            updateForm(form, values);
        }
        return forms;
    }

    public static Element updateForm(Element form, Map<String, String> values) {
        for (Entry<String, String> entry : values.entrySet()) {
            // TODO: Throw exception on unrecognized names?
            // TODO: Multiple inputs with the same name - values should be a Map<String,List<String>>?
            // TODO: <select> <input type="radio"> <input type="checkbox"> <textarea>
            form.find("...", "input[name=" + entry.getKey() + "]").check().attr("value", entry.getValue());
        }
        return form;
    }

    public static Map<String,List<String>> formToMap(ElementSet forms) {
        return formToMap(forms.first());
    }

    public static Map<String, List<String>> formToMap(Element form) {
        // TODO: <input name="foo"> <input name="bar"> <input name="foo">
        // TODO: <select> <input type="radio"> <input type="checkbox"> <textarea>
        HashMap<String, List<String>> result = new LinkedHashMap<String, List<String>>();
        for (Element element : form.find("...", "input")) {
            if (!result.containsKey(element.name())) {
                result.put(element.name(), new ArrayList<String>());
            }
            result.get(element.name()).add(element.val());
        }
        return result;
    }

}
