package org.eaxy;

import static org.eaxy.Xml.cdata;
import static org.eaxy.Xml.comment;
import static org.eaxy.Xml.text;

import java.io.Reader;
import java.util.Stack;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.sun.org.apache.xerces.internal.impl.Constants;

public class StaxReader implements XMLStreamConstants {

    private final Stack<Element> elementStack = new Stack<Element>();
    private final XMLStreamReader streamReader;
    private final Document document = new Document();

    public StaxReader(Reader reader) throws XMLStreamException {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
        inputFactory.setProperty(Constants.ZEPHYR_PROPERTY_PREFIX + Constants.STAX_REPORT_CDATA_EVENT, Boolean.TRUE);
        this.streamReader = inputFactory.createXMLStreamReader(reader);
    }

    public static Document read(Reader reader) {
        try {
            return new StaxReader(reader).doParse();
        } catch (XMLStreamException e) {
            throw new MalformedXMLException(e.getMessage(), e.getLocation().getLineNumber());
        }
    }

    private Document doParse() throws XMLStreamException {
        while (streamReader.hasNext()) {
            streamReader.next();

            switch (streamReader.getEventType()) {
            case START_ELEMENT:
                Element element = readElement();

                if (!elementStack.isEmpty())
                    current().add(element);
                if (document.getRootElement() == null)
                    document.setRootElement(element);

                elementStack.push(element);
                break;
            case CDATA:
                current().add(cdata(streamReader.getText()));
                break;
            case SPACE:
            case CHARACTERS:
                current().add(text(streamReader.getText()));
                break;
            case END_ELEMENT:
                elementStack.pop();
                break;
            case COMMENT:
                current().add(comment(streamReader.getText()));

            case START_DOCUMENT:
            case END_DOCUMENT:
                break;
            case DTD:
                document.addDTD(streamReader.getText());
                break;
            default:
                throw new IllegalStateException("Unknown event type " + streamReader.getEventType());
            }
        }

        return document;
    }

    private Element current() {
        return elementStack.peek();
    }

    private Element readElement() {
        QName name = streamReader.getName();
        Element element = new Element(getName(name));

        for (int i = 0; i < streamReader.getNamespaceCount(); i++) {
            element.namespace(new Namespace(streamReader.getNamespaceURI(i), streamReader.getNamespacePrefix(i)));
        }

        for (int i = 0; i < streamReader.getAttributeCount(); i++) {
            element.attr(getName(streamReader.getAttributeName(i)), streamReader.getAttributeValue(i));
        }
        return element;
    }

    static QualifiedName getName(QName name) {
        return new QualifiedName(name.getNamespaceURI(), name.getLocalPart(), name.getPrefix());
    }

}
