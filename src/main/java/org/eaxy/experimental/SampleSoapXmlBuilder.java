package org.eaxy.experimental;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eaxy.Document;
import org.eaxy.Element;
import org.eaxy.ElementSet;
import org.eaxy.Namespace;
import org.eaxy.NonMatchingPathException;
import org.eaxy.Validator;
import org.eaxy.Xml;

public class SampleSoapXmlBuilder {

    private static final Namespace SOAP = new Namespace("http://schemas.xmlsoap.org/wsdl/soap/", "SOAP");
    private static final Namespace XS = new Namespace("http://www.w3.org/2001/XMLSchema");

    public class SoapOperationDefinition {

        private Element operationElement;

        public SoapOperationDefinition(Element operationElement) {
            this.operationElement = operationElement;
        }

        public Element randomOutput(String nsPrefix) {
            Element input = operationElement.find("output").single();
            if (input.attr("element") != null) {
                String[] inputRefParts = input.attr("element").split(":");
                return new SampleXmlBuilder(new Document(targetSchema()), nsPrefix).createRandomElement(inputRefParts[1]);
            } else if (input.name() != null) {
                Element message = wsdlFile.find("message[name=" + input.name() + "]").single();
                Element part = message.find("part").single();
                String[] elementRefParts = part.attr("element").split(":");
                Namespace namespace = wsdlFile.getRootElement().getNamespace(elementRefParts[0]);

                return new SampleXmlBuilder(new Document(getSchema(namespace)), nsPrefix)
                        .createRandomElement(elementRefParts[1]);
            } else if (input.hasAttr("message")) {
                String[] messageQname = input.attr("message").split(":");
                Element message = wsdlFile.find("message[name=" + messageQname[1] + "]").single();
                Element part = message.find("part").single();
                String[] elementRefParts = part.attr("element").split(":");
                Namespace namespace = wsdlFile.getRootElement().getNamespace(elementRefParts[0]);
                Namespace outputNamespace = new Namespace(namespace.getUri(), nsPrefix);
                return new SampleXmlBuilder(new Document(getSchema(namespace)), nsPrefix)
                        .createRandomElement(outputNamespace.name(elementRefParts[1]));
            } else {
                throw new IllegalArgumentException("Don't know what to do with " + input);
            }
        }

        public Element randomInput(String nsPrefix) {
            Element input = operationElement.find("input").single();
            if (input.attr("element") != null) {
                String[] inputRefParts = input.attr("element").split(":");
                return new SampleXmlBuilder(new Document(targetSchema()), nsPrefix).createRandomElement(inputRefParts[1]);
            } else if (input.name() != null) {
                Element message = wsdlFile.find("message[name=" + input.name() + "]").single();
                Element part = message.find("part").single();
                String[] elementRefParts = part.attr("element").split(":");
                Namespace namespace = wsdlFile.getRootElement().getNamespace(elementRefParts[0]);

                return new SampleXmlBuilder(new Document(getSchema(namespace)), nsPrefix)
                        .createRandomElement(elementRefParts[1]);
            } else if (input.hasAttr("message")) {
                String[] messageQname = input.attr("message").split(":");
                Element message = wsdlFile.find("message[name=" + messageQname[1] + "]").single();
                Element part = message.find("part").single();
                String[] elementRefParts = part.attr("element").split(":");
                Namespace namespace = wsdlFile.getRootElement().getNamespace(elementRefParts[0]);

                return new SampleXmlBuilder(new Document(getSchema(namespace)), nsPrefix)
                        .createRandomElement(elementRefParts[1]);
            } else {
                throw new IllegalArgumentException("Don't know what to do with " + input);
            }
        }

        public Namespace getNamespace() {
            Element input = operationElement.find("input").single();
            if (input.attr("element") != null) {
                String[] inputRefParts = input.attr("element").split(":");
                return wsdlFile.getRootElement().getNamespace(inputRefParts[0]);
            } else if (input.attr("message") != null) {
                String[] messageRefParts = input.attr("message").split(":");
                ElementSet messageDef = wsdlFile.find("message[name=" + messageRefParts[1] + "]");
                String[] inputRefParts = messageDef.find("part[name=body]").single().attr("element").split(":");
                return wsdlFile.getRootElement().getNamespace(inputRefParts[0]);
            } else {
                throw new IllegalArgumentException("Don't know what to do with " + input);
            }
        }

        public Element targetSchema() {
            return getSchema(getNamespace());
        }

        public String getSoapAction() {
            return null;
        }

        public Element processRequest(Element soapRequest) {
            if (!soapRequest.getName().equals(SOAP.name("Envelope"))) {
                throw new NonMatchingPathException("Didn't find root element " + SOAP.name("Envelope"));
            }
            Element output = soapRequest.find(SOAP.name("Body"), "*").single();
            new Validator(targetSchema()).validate(output);
            return soapEnvelope(randomOutput("msg"));
        }

    }

    public class SoapServiceDefinition {

        private final Element itfElement;
        private Element binding;

        public SoapServiceDefinition(Element service) {
            if (service.hasAttr("interface")) {
                String itf = service.attr("interface");
                String[] parts = itf.split(":");
                String itfName = parts.length == 1 ? parts[0] : parts[1];
                itfElement = wsdlFile.find("interface[name=" + itfName + "]").single();
            } else if (service.find("port").isPresent()) {
                String itf = service.select("port").attr("binding");
                String[] parts = itf.split(":");
                String itfName = parts.length == 1 ? parts[0] : parts[1];
                binding = wsdlFile.find("binding[name=" + itfName + "]").single();
                itfElement = wsdlFile.find("portType[name=" + binding.type().split(":")[1] + "]").single();
            } else {
                throw new IllegalArgumentException("What to do with " + service);
            }
        }

        public SoapServiceDefinition(String name) {
            this(wsdlFile.find("service[name=" + name + "]").single());
        }

        public Element operationElement(String name) {
            return itfElement.find("operation[name=" + name + "]").single();
        }

        public SoapOperationDefinition operation(String name) {
            return new SoapOperationDefinition(operationElement(name));
        }

        public SoapOperationDefinition soapAction(String soapAction) {
            for (Element operationBinding : binding.find("operation")) {
                if (operationBinding.find(SOAP.name("operation")).single().attr("soapAction").equals(soapAction)) {
                    return operation(operationBinding.name());
                }
            }
            return null;
        }

    }

    private Document wsdlFile;
    private Map<String, Element> schemas = new HashMap<>();

    public SampleSoapXmlBuilder(String wsdlResource) throws IOException {
        this(Xml.readResource("/" + wsdlResource), null);
        for (Element schema : wsdlFile.find("types", XS.name("schema"))) {
            addSchema(new Namespace(schema.attr("targetNamespace")), schema);
        }
    }

    private Element getSchema(Namespace namespace) {
        if (schemas.containsKey(namespace.getUri())) {
            return schemas.get(namespace.getUri());
        } else {
            throw new IllegalArgumentException("No schema for " + namespace + " (actual " + schemas.keySet() + ")");
        }
    }

    private void addSchema(Namespace namespace, Element schema) {
        schemas.put(namespace.getUri(), schema);
    }

    public Element soapEnvelope(Element payload) {
        return SOAP.el("Envelope",
            SOAP.el("Header"),
            SOAP.el("Body", payload));
    }

    public SampleSoapXmlBuilder(URL resource) throws IOException {
        this(Xml.read(resource), resource);
    }

    // TODO: It would be so much nicer if Document had it's base resource!
    public SampleSoapXmlBuilder(Document wsdlFile, URL resource) throws IOException {
        this.wsdlFile = wsdlFile;
        for (Element schema : wsdlFile.find("types", "schema")) {
            Element importEl = schema.find("import").firstOrDefault();
            if (importEl != null) {
                addSchema(new Namespace(importEl.attr("namespace")),
                        Xml.read(new URL(resource, importEl.attr("schemaLocation"))).getRootElement());
            } else {
                addSchema(new Namespace(schema.attr("targetNamespace")), schema);
            }
        }
    }

    public SampleSoapXmlBuilder(Document wsdl) throws IOException {
        this(wsdl, null);
    }

    public SoapServiceDefinition service(String name) {
        return new SoapServiceDefinition(name);
    }

    public SoapServiceDefinition getService() {
        return new SoapServiceDefinition(wsdlFile.find("service").first());
    }

    public String getPortName() {
        return wsdlFile.find("service").first().name();
    }

    public Element processRequest(String soapAction, Element input) {
        return getService().soapAction(soapAction).processRequest(input);
    }

}
