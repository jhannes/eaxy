package org.eaxy.html;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.eaxy.Document;
import org.eaxy.Element;
import org.eaxy.ElementFilters;
import org.eaxy.Xml;

public class Xhtml extends Document {

    public Xhtml(Document document) {
        super(document.getRootElement());
    }

    public static Xhtml xhtml(StringWriter html) {
        return parse(html.toString());
    }

    public static Xhtml parse(CharSequence html) {
        return new Xhtml(Xml.xml(html));
    }

    public HtmlForm getForm(Object filter) {
        return new HtmlForm(select(filter));
    }

    public Element findById(String id) {
        return select(ElementFilters.attrFilter("id", id));
    }

    public static Element div(String text) {
        return Xml.el("div", text);
    }

    public static Xhtml readAndClose(InputStream stream) throws IOException {
        return new Xhtml(Xml.readAndClose(stream));
    }

}
