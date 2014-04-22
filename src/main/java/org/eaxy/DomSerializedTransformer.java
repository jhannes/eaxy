package org.eaxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class DomSerializedTransformer {

    public static Document fromDom(org.w3c.dom.Document document) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            Source source = new DOMSource(document);
            Result target = new StreamResult(out);
            transformer.transform(source, target);
            return Xml.xml(out.toString());
        } catch (TransformerException e) {
            throw new CanNeverHappenException("Oh, shut up!", e);
        }
    }

    public static org.w3c.dom.Document toDom(Document document) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            String xml = document.toXML();
            return builder.parse(new InputSource(new StringReader(xml)));
        } catch (ParserConfigurationException e) {
            throw new CanNeverHappenException("Oh, just shut up!", e);
        } catch (SAXException e) {
            throw new CanNeverHappenException("Oh, just shut up!", e);
        } catch (IOException e) {
            throw new CanNeverHappenException("Oh, just shut up!", e);
        }
    }
}
