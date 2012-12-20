package org.eaxy.html;

import java.io.StringWriter;

import org.eaxy.Element;
import org.eaxy.ElementFilters;
import org.eaxy.Xml;

public class Xhtml {

	private final Element rootElement;

    public Xhtml(Element rootElement) {
        this.rootElement = rootElement;
    }

    public static Xhtml xhtml(StringWriter html) {
		return parse(html.toString());
	}

	public static Xhtml parse(CharSequence html) {
		return new Xhtml(Xml.xml(html).getRootElement());
	}

    public HtmlForm getForm() {
        return new HtmlForm(rootElement.select("form"));
    }

    public Element findById(String id) {
        return rootElement.select(ElementFilters.attrFilter("id", id));
    }

}
