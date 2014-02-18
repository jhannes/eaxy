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

    public void validate(Element xml) {
        try {
            validator.validate(new DOMSource(DomTransformer.toDom(new Document(xml))));
        } catch (SAXException e) {
            throw new RuntimeException(e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException("StringReader should never throw IOException", e);
        }
    }

}
