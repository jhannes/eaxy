package org.eaxy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.LinkedList;

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
            writer.write(("<![CDATA[" + text() + "]]>").toString());
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
            writer.write("<!--" + text() + "-->");
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
        try (FileInputStream inputStream = new FileInputStream(file)) {
            return readAndClose(inputStream);
        }
    }

    public static Document readAndClose(InputStream inputStream) throws IOException {
        return readAndClose(inputStream, UTF_8);
    }

    public static Document readAndClose(InputStream inputStream, Charset charset) throws IOException {
        try (Reader reader = new InputStreamReader(inputStream, charset)) {
            return read(reader);
        }
    }

    public static Document read(Reader reader) throws IOException {
        return StaxReader.read(reader);
    }

    public static Document fromDom(org.w3c.dom.Document document) {
        return DomTransformer.fromDom(document);
    }

    public static org.w3c.dom.Document toDom(Element element) {
        return DomTransformer.toDom(new Document(element));
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
