package org.eaxy;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.eaxy.utils.IOUtils;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/** Validator can validate a Element tree against a XSD */
public class Validator {

    private javax.xml.validation.Validator validator;

    
    /**
     * Load schemas for validation from class path
     * @param resourcePaths the resource path for the schemas from the classpath
     */
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
        } catch (SAXParseException cause) {
            MalformedXMLException e = new MalformedXMLException(cause.getMessage(), cause.getLineNumber());
            replaceStackTrace(cause, e);
            throw e;
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    public Validator(String resourcePath) {
        this(new String[] { resourcePath });
    }

    /** Create a validator based on a XSD loaded as a Eaxy Element */
    public Validator(Element schema) {
        this(new Document(schema));
    }

    /** Create a validator based on a XSD loaded as a Eaxy Document */
    public Validator(Document schemaDoc) {
        this(Xml.toDom(schemaDoc), schemaDoc.getBaseUrl() != null ? schemaDoc.getBaseUrl().toExternalForm() : "" );
    }

    /**
     * Create a Validator consisting of several documents that may have imports to each other.
     * To facilitate &lt;import&gt;, the schemas are saved to temporary storage.
     * TODO: Is it possible to solve this more elegantly?
     * 
     * @throws IOException if an included schema could not be saved to temporary disk
     */
    public Validator(List<Document> includedSchemas) throws IOException {
        int index = 0;

        Map<Namespace, Document> schemas = new HashMap<>();
        for (Document document : includedSchemas) {
            schemas.put(new Namespace(document.getRootElement().attr("targetNamespace")), document);
        }

        for (Document document : includedSchemas) {
            saveImportedSchemas(schemas, document);
        }

        Source[] sources = new Source[includedSchemas.size()];
        for (Document schema : includedSchemas) {
            sources[index++] = new StreamSource(schema.getBaseUrl().toExternalForm());
        }

        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            validator = schemaFactory.newSchema(sources).newValidator();
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveImportedSchemas(Map<Namespace, Document> schemas, Document document) throws IOException {
        for (Element importElement : document.getRootElement().find("import")) {
            if (!importElement.hasAttr("schemaLocation")) {
                Document imported = schemas.get(new Namespace(importElement.attr("namespace")));
                if (imported == null) {
                    throw new IllegalArgumentException("Can't find namespace " + importElement.attr("namespace") + " in " + schemas.keySet());
                }
                saveImportedSchemas(schemas, imported);
                importElement.attr("schemaLocation", imported.getBaseUrl().toExternalForm());
            }
        }
        ensureBaseUrl(document);
    }

    private void ensureBaseUrl(Document schema) throws IOException {
        if (schema.getBaseUrl() == null) {
            File schemaFile = IOUtils.toTmpFile(schema.toIndentedXML(), "eaxy-schema-", ".xsd");
            schema.setBaseUrl(schemaFile.toURI().toURL());
        }
    }

    public Validator(org.w3c.dom.Document dom, String systemId) {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            validator = schemaFactory.newSchema(new DOMSource(dom, systemId)).newValidator();
        } catch (SAXParseException cause) {
            MalformedXMLException e = new MalformedXMLException(cause.getMessage(), cause.getLineNumber());
            replaceStackTrace(cause, e);
            throw e;
        } catch (SAXException e) {
            if (e.getMessage().startsWith("src-resolve") && dom.getBaseURI() == null) {
                throw new RuntimeException(e.getMessage() + ". Did you forget to set base URI?");
            }
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

    private void replaceStackTrace(SAXParseException cause, MalformedXMLException e) {
        if (cause.getSystemId() != null) {
            StackTraceElement[] stackTrace = e.getStackTrace();
            StackTraceElement[] newStackTrace = new StackTraceElement[stackTrace.length+1];
            File file = new File(cause.getSystemId());
            newStackTrace[0] = new StackTraceElement(file.getName(), "", file.getName(), cause.getLineNumber());
            System.arraycopy(stackTrace, 0, newStackTrace, 1, stackTrace.length);
            e.setStackTrace(newStackTrace);
        }
    }
}
