package org.eaxy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.zip.GZIPInputStream;

import org.eaxy.html.Xhtml;

public abstract class Xml {

    public static final Charset UTF_8 = Charset.forName("UTF-8");

    static class CDataElement implements Node {

        private final String stringContent;

        CDataElement(CharSequence stringContent) {
            this.stringContent = stringContent.toString();
        }

        @Override
        public CharSequence text() {
            return stringContent;
        }

        @Override
        public void writeTo(Writer writer, LinkedList<Namespace> printedNamespaces) throws IOException {
            writeTo(writer, printedNamespaces, "");
        }

        @Override
        public void writeTo(Writer writer, LinkedList<Namespace> printedNamespaces, String indent) throws IOException {
            writer.write(indent + "<![CDATA[" + text() + "]]>");
        }

        @Override
        public Node copy() {
            return new CDataElement(stringContent);
        }
    }

    static class CommentElement implements Node {

        private final String stringContent;

        CommentElement(CharSequence stringContent) {
            this.stringContent = stringContent.toString();
        }

        @Override
        public void writeTo(Writer writer, LinkedList<Namespace> printedNamespaces) throws IOException {
            writeTo(writer, printedNamespaces, "");
        }

        @Override
        public void writeTo(Writer writer, LinkedList<Namespace> printedNamespaces, String indent) throws IOException {
            writer.write(indent + "<!--" + text() + "-->");
        }

        @Override
        public String text() {
            return stringContent;
        }

        @Override
        public Node copy() {
            return new CommentElement(stringContent);
        }

    }

    static class TextElement implements Node {

        private final String stringContent;

        TextElement(CharSequence stringContent) {
            this.stringContent = stringContent.toString();
        }

        @Override
        public void writeTo(Writer writer, LinkedList<Namespace> printedNamespaces) throws IOException {
            writeTo(writer, printedNamespaces, "");
        }

        @Override
        public void writeTo(Writer writer, LinkedList<Namespace> printedNamespaces, String indent) throws IOException {
            writer.write(text().replaceAll("&", "&amp;")
                    .replaceAll("<", "&lt;")
                    .replaceAll(">", "&gt;"));
        }

        @Override
        public String text() {
            return stringContent;
        }

        @Override
        public Node copy() {
            return new TextElement(stringContent);
        }
    }

    public static Element el(String tagName, Content... contents) {
        return Namespace.NO_NAMESPACE.el(tagName, contents);
    }

    public static Node comment(String string) {
        return new Xml.CommentElement(string);
    }

    public static Element el(String tagName, String stringContent) {
        return el(tagName, text(stringContent));
    }

    public static Node text(CharSequence stringContent) {
        return new Xml.TextElement(stringContent);
    }

    public static Node cdata(CharSequence stringContent) {
        return new Xml.CDataElement(stringContent);
    }

    public static Attribute attr(String key, String value) {
        return Namespace.NO_NAMESPACE.attr(key, value);
    }

    public static Document xml(CharSequence xml) {
        try {
            return read(new StringReader(xml.toString()));
        } catch (IOException e) {
            throw new CanNeverHappenException("StringReader never throws IOException", e);
        }
    }

    public static Document read(File file) throws IOException {
        if (file.getName().endsWith(".gz")) {
            try (InputStream inputStream = new GZIPInputStream(new FileInputStream(file))) {
                return StaxReader.read(inputStream);
            }
        } else {
            try (InputStream inputStream = new FileInputStream(file)) {
                return StaxReader.read(inputStream);
            }
        }
    }

    public static Document readResource(String name) throws IOException {
        try(InputStream input = Xhtml.class.getResourceAsStream(name)) {
            if (input == null) {
                throw new IllegalArgumentException("Can't load " + name);
            }
            if (name.endsWith(".gz")) {
                return StaxReader.read(new GZIPInputStream(input));
            } else {
                return StaxReader.read(input);
            }
        }
    }

    public static Document read(Reader reader) throws IOException {
        return StaxReader.read(reader);
    }

    public static Document fromDom(org.w3c.dom.Document document) {
        return DomTransformer.fromDom(document);
    }

    public static org.w3c.dom.Document toDom(Document document) {
        return DomTransformer.toDom(document);
    }

    public static Document doc(Element el) {
        return new Document(el);
    }

    public static Validator validatorFromResource(String... resourcePaths) {
        return new Validator(resourcePaths);
    }

}
