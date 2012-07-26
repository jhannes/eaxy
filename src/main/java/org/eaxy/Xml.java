package org.eaxy;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.DefaultHandler2;

public abstract class Xml {

    private static final class ElementBuilderHandler extends DefaultHandler2 {
        private final Stack<Element> elementStack = new Stack<Element>();
        private StringBuilder currentText;
        private Document document;

        @Override
        public void startDocument() {
            document = new Document();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            Element newElement = el(qName, Namespace.NO_NAMESPACE).attrs(attributes);
            pushTextToTopElement();
            if (!elementStack.isEmpty()) elementStack.peek().add(newElement);
            elementStack.add(newElement);
        }

        @Override
        public void startEntity(String name) throws SAXException {
            // TODO Auto-generated method stub
            super.startEntity(name);
        }

        @Override
        public void comment(char[] ch, int start, int length) {
            pushTextToTopElement();
            if (!elementStack.isEmpty()) {
                elementStack.peek().add(Xml.comment(new String(ch, start, length)));
            }
        }

        @Override
        public void startDTD(String name, String publicId, String systemId) {
            document.addDTD("<!DOCTYPE " + name + " PUBLIC \"" + publicId + "\" \"" + systemId + "\">");
            System.out.println(name + publicId + systemId);
        }

        @Override
        public void processingInstruction(String target, String data) {
            System.out.println(target + data);
        }

        @Override
        public void startCDATA() {
            pushTextToTopElement();
            currentText = new StringBuilder();
        }

        @Override
        public void endCDATA() {
            if (!elementStack.isEmpty()) {
                if (currentText.length() > 0) {
                    elementStack.peek().add(cdata(currentText));
                }
            }
            this.currentText = new StringBuilder();
        }

        private void pushTextToTopElement() {
            if (!elementStack.isEmpty()) {
                if (currentText.length() > 0) {
                    elementStack.peek().add(text(currentText));
                }
            }
            this.currentText = new StringBuilder();
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            currentText.append(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            pushTextToTopElement();
            this.document.setRootElement(elementStack.pop());
        }

        public Document getDocument() {
            return document;
        }
    }

    private static class CDataElement implements Node {

        private final String stringContent;

        CDataElement(CharSequence stringContent) {
            this.stringContent = stringContent.toString();
        }

        @Override
        public CharSequence text() {
            return "<![CDATA[" + stringContent + "]]>";
        }

        @Override
        public CharSequence print(LinkedList<Namespace> printedNamespaces) {
            return text();
        }

    }

    private static class CommentElement implements Node {

        private final String stringContent;

        CommentElement(CharSequence stringContent) {
            this.stringContent = stringContent.toString();
        }

        @Override
        public CharSequence print(LinkedList<Namespace> printedNamespaces) {
            return "<!--" + text() + "-->";
        }

        @Override
        public String text() {
            return stringContent;
        }

    }

    private static class TextElement implements Node {

        private final String stringContent;

        TextElement(CharSequence stringContent) {
            this.stringContent = stringContent.toString();
        }

        @Override
        public CharSequence print(LinkedList<Namespace> printedNamespaces) {
            return text().replaceAll("&", "&amp;")
                    .replaceAll("<", "&lt;")
                    .replaceAll(">", "&gt;");
        }

        @Override
        public String text() {
            return stringContent;
        }
    }

    public static Element el(String tagName, Node... content) {
        return new Element(Namespace.NO_NAMESPACE.name(tagName), new ArrayList<Node>(Arrays.asList(content)));
    }

    public static Node comment(String string) {
        return new Xml.CommentElement(string);
    }

    public static Element el(String tagName, Namespace namespace, Node... content) {
        return new Element(namespace.name(tagName), new ArrayList<Node>(Arrays.asList(content)));
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

    public static Document xml(String xml) {
        try {
            return read(new StringReader(xml));
        } catch (IOException e) {
            throw new CanNeverHappenException("StringReader never throws IOException", e);
        }
    }

    public static Document read(File file) throws IOException {
        FileReader reader = new FileReader(file);
        try {
            return read(reader);
        } finally {
            reader.close();
        }
    }

    public static Document read(Reader reader) throws IOException {
        return parse(new InputSource(reader));
    }

    public static Document parse(InputSource inputSource) throws IOException {
        try {
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            ElementBuilderHandler handler = new ElementBuilderHandler();
            parser.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
            parser.parse(inputSource, handler);
            return handler.getDocument();
        } catch (SAXParseException e) {
            throw new MalformedXMLException(e.getMessage(), e.getLineNumber());
        } catch (SAXException e) {
            throw new UnexpectedException(e);
        } catch (ParserConfigurationException e) {
            throw new CanNeverHappenException("SAXParserFactory is always supported", e);
        }
    }


}
