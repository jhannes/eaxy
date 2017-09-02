package org.eaxy;

import static org.eaxy.Xml.cdata;
import static org.eaxy.Xml.comment;
import static org.eaxy.Xml.text;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.Iterator;
import java.util.Stack;
import java.util.zip.GZIPInputStream;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.sun.org.apache.xerces.internal.impl.Constants;

@SuppressWarnings("restriction")
public class XmlIterator implements XMLStreamConstants, Iterator<Element> {

    private final Stack<Element> elementStack = new Stack<Element>();
    private XMLStreamReader streamReader;
    private ElementQuery query;

    private Element next;

    public XmlIterator(XMLStreamReader streamReader, ElementQuery query) {
        this.streamReader = streamReader;
        this.query = query;

        try {
            this.next = nextMatchingElement();
        } catch (XMLStreamException e) {
            throw new MalformedXMLException(e.getMessage(), e.getLocation().getLineNumber());
        }
    }

    private static XMLInputFactory getInputFactory() {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
        inputFactory.setProperty(Constants.ZEPHYR_PROPERTY_PREFIX + Constants.STAX_REPORT_CDATA_EVENT, Boolean.TRUE);
        return inputFactory;
    }

    public static Iterable<Element> read(ElementQuery query, Reader reader) throws IOException {
        Document doc = Xml.read(reader);
        return query.search(new ElementSet(doc.getRootElement()));
    }

    private Element nextMatchingElement() throws XMLStreamException {
        while (streamReader.hasNext()) {
            streamReader.next();

            switch (streamReader.getEventType()) {
            case START_ELEMENT:
                Element element = readElement();
                elementStack.push(element);

                if (query.matches(elementStack, 1)) {
                    readSubtree(element);
                    elementStack.pop();
                    return element;
                }

                break;
            case END_ELEMENT:
                elementStack.pop();
                break;
            case CDATA:
            case SPACE:
            case CHARACTERS:
            case COMMENT:
                break;
            case START_DOCUMENT:
                break;
            case END_DOCUMENT:
                return null;
            case DTD:
                break;
            default:
                throw new IllegalStateException("Unknown event type " + streamReader.getEventType());
            }
        }
        throw new IllegalStateException("Document not properly ended");
    }

    private void readSubtree(Element rootElement) throws XMLStreamException {
        Stack<Element> elementStack = new Stack<>();
        elementStack.push(rootElement);
        while (streamReader.hasNext()) {
            streamReader.next();

            switch (streamReader.getEventType()) {
            case START_ELEMENT:
                Element element = readElement();
                elementStack.peek().add(element);
                elementStack.push(element);
                break;
            case END_ELEMENT:
                elementStack.pop();
                if (elementStack.isEmpty()) {
                    return;
                }
                break;
            case CDATA:
                elementStack.peek().add(cdata(streamReader.getText()));
                break;
            case SPACE:
            case CHARACTERS:
                elementStack.peek().add(text(streamReader.getText()));
                break;
            case COMMENT:
                elementStack.peek().add(comment(streamReader.getText()));
                break;
            case START_DOCUMENT:
            case END_DOCUMENT:
            case DTD:
                break;
            default:
                throw new IllegalStateException("Unknown event type " + streamReader.getEventType());
            }
        }
        throw new IllegalStateException("Didn't find closing tag for " + rootElement);
    }

    private Element readElement() {
        QName name = streamReader.getName();
        Element element = new Element(toName(name));

        for (int i = 0; i < streamReader.getNamespaceCount(); i++) {
            element.namespace(new Namespace(streamReader.getNamespaceURI(i), streamReader.getNamespacePrefix(i)));
        }

        for (int i = 0; i < streamReader.getAttributeCount(); i++) {
            element.attr(toName(streamReader.getAttributeName(i)), streamReader.getAttributeValue(i));
        }
        return element;
    }

    static QualifiedName toName(QName name) {
        return new QualifiedName(name.getNamespaceURI(), name.getLocalPart(), name.getPrefix());
    }

    @SuppressWarnings("resource")
    public static Iterable<Element> iterate(ElementQuery query, URL url) {
        final InputStream inputStream = openStream(url);
        return new Iterable<Element>() {
            @Override
            public Iterator<Element> iterator() {
                try {
                    return new XmlIterator(getInputFactory().createXMLStreamReader(inputStream), query);
                } catch (XMLStreamException e) {
                    throw new MalformedXMLException(e.getMessage(), e.getLocation().getLineNumber());
                }
            }
        };
    }

    private static InputStream openStream(URL url) {
        try {
            if (url.getFile().endsWith(".gz")) {
                return new GZIPInputStream(url.openStream());
            } else {
                return url.openStream();
            }
        } catch (IOException e) {
            throw new RuntimeException("Can't open " + url, e);
        }
    }

    public static Iterable<Element> iterate(ElementQuery query, Reader reader) {
        return new Iterable<Element>() {
            @Override
            public Iterator<Element> iterator() {
                try {
                    return new XmlIterator(getInputFactory().createXMLStreamReader(reader), query);
                } catch (XMLStreamException e) {
                    throw new MalformedXMLException(e.getMessage(), e.getLocation().getLineNumber());
                }
            }
        };
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public Element next() {
        Element current = next;
        try {
            next = nextMatchingElement();
            if (next == null) {
                streamReader.close();
            }
        } catch (XMLStreamException e) {
            throw new MalformedXMLException(e.getMessage(), e.getLocation().getLineNumber());
        }
        return current;
    }

}
