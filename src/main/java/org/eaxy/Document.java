package org.eaxy;

import java.util.ArrayList;
import java.util.List;

public class Document {

    private Element rootElement;
    private List<String> dtds = new ArrayList<String>();

    public void setRootElement(Element rootElement) {
        this.rootElement = rootElement;
    }

    public Element getRootElement() {
        return rootElement;
    }

    public String toXML() {
        StringBuilder result = new StringBuilder();
        result.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        for (String dtd : dtds) {
            result.append(dtd).append("\n");
        }
        result.append(rootElement.toXML());
        return result.toString();
    }

    public void addDTD(String dtdString) {
        this.dtds.add(dtdString);
    }
}
