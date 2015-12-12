package org.eaxy;

import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.DefaultHandler2;

public class SaxReader {

    static final class ElementBuilderHandler extends DefaultHandler2 {
        private final Stack<Element> elementStack = new Stack<Element>();
        private StringBuilder currentText;
        private Document document;

        // TODO: Keep entities like &aring; and &oslash;

        @Override
        public void startDocument() {
            document = new Document();

        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            Namespace namespace;
            if (uri != null && !uri.isEmpty()) {
                String prefix = qName.contains(":") ? qName.split(":")[0] : null;
                namespace = new Namespace(uri, prefix);
            } else {
                namespace = Namespace.NO_NAMESPACE;
            }

            Element newElement = namespace.el(localName);
            addAttrs(newElement, attributes);
            pushTextToTopElement();
            if (!elementStack.isEmpty()) elementStack.peek().add(newElement);
            elementStack.add(newElement);
        }

        private void addAttrs(Element element, Attributes attributes) {
            for (int i = 0; i < attributes.getLength(); i++) {
                if (attributes.getQName(i).startsWith("xmlns:")) {
                    element.namespace(new Namespace(attributes.getValue(i), attributes.getQName(i).substring("xmlns:".length())));
                } else {
                    element.attr(new QualifiedName(attributes.getURI(i), attributes.getQName(i)),
                            attributes.getValue(i));
                }
            }
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
                    elementStack.peek().add(Xml.cdata(currentText));
                }
            }
            this.currentText = new StringBuilder();
        }

        private void pushTextToTopElement() {
            if (!elementStack.isEmpty()) {
                if (currentText.length() > 0) {
                    elementStack.peek().add(Xml.text(currentText));
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

    static Document read(InputSource inputSource) throws IOException {
        try {
            ElementBuilderHandler handler = new ElementBuilderHandler();
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            parserFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            parserFactory.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
            parserFactory.setNamespaceAware(true);
            SAXParser parser = parserFactory.newSAXParser();
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

    public static Document read(InputStream input) throws IOException {
        return read(new InputSource(input));
    }

}
