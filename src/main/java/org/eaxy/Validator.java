package org.eaxy;

import java.io.IOException;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

public class Validator {

    private javax.xml.validation.Validator validator;

    public Validator(String[] resourcePaths) {
        Source[] sources = new Source[resourcePaths.length];
        for (int i = 0; i < resourcePaths.length; i++) {
            URL url = getClass().getClassLoader().getResource(resourcePaths[i]);
            if (url == null) {
                throw new RuntimeException("Missing resource " + resourcePaths[i]);
            }
            sources[i] = new StreamSource(url.toExternalForm());
        }
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            validator = schemaFactory.newSchema(sources).newValidator();
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    public Validator(Element schema) {
        this(new Document(schema));
    }

    public Validator(Document schemaDoc) {
        this(Xml.toDom(schemaDoc));
    }

    public Validator(org.w3c.dom.Document dom) {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            validator = schemaFactory.newSchema(new DOMSource(dom)).newValidator();
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Throw an exception if the element does not conform with the schema.
     * @return element to be validated for easier chaining of method calls
     */
    public Element validate(Element xml) {
        try {
            validator.validate(new DOMSource(Xml.toDom(new Document(xml))));
            return xml;
        } catch (SAXException e) {
            throw new SchemaValidationException(e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException("StringReader should never throw IOException", e);
        }
    }

}
