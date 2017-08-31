package org.eaxy.experimental;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eaxy.Document;
import org.eaxy.Element;
import org.eaxy.Namespace;
import org.eaxy.Xml;

public class SampleSoapXmlBuilder {

    private static Namespace XS = new Namespace("http://www.w3.org/2001/XMLSchema");

    public class SoapOperationDefinition {

        private Element operationElement;

        public SoapOperationDefinition(Element operationElement) {
            this.operationElement = operationElement;
        }

        public Element randomOutput(String nsPrefix) {
            Element input = operationElement.find("output").single();
            if (input.attr("element") != null) {
                String[] inputRefParts = input.attr("element").split(":");
                return new SampleXmlBuilder(new Document(getSchema()), nsPrefix).createRandomElement(inputRefParts[1]);
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

        public Element randomInput(String nsPrefix) {
            Element input = operationElement.find("input").single();
            if (input.attr("element") != null) {
                String[] inputRefParts = input.attr("element").split(":");
                return new SampleXmlBuilder(new Document(getSchema()), nsPrefix).createRandomElement(inputRefParts[1]);
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

        private Element getSchema(Namespace namespace) {
            if (schemas.containsKey(namespace)) {
                return schemas.get(namespace);
            } else {
                throw new IllegalArgumentException("No schema for " + namespace + " (actual " + schemas.keySet() + ")");
            }
        }

        public Namespace getNamespace() {
            Element input = operationElement.find("input").single();
            if (input.attr("element") != null) {
                String[] inputRefParts = input.attr("element").split(":");
                return wsdlFile.getRootElement().getNamespace(inputRefParts[0]);
            } else {
                throw new IllegalArgumentException("Don't know what to do with " + input);
            }
        }

        public Element getSchema() {
            return schemas.get(getNamespace());
        }

    }

    public class SoapServiceDefinition {

        private final Element service;
        private final Element itfElement;

        public SoapServiceDefinition(String name) {
            service = wsdlFile.find("service[name=" + name + "]").single();
            if (service.hasAttr("interface")) {
                String itf = service.attr("interface");
                String[] parts = itf.split(":");
                String itfName = parts.length == 1 ? parts[0] : parts[1];
                itfElement = wsdlFile.find("interface[name=" + itfName + "]").single();
            } else if (service.find("port").isPresent()) {
                String itf = service.select("port").attr("binding");
                String[] parts = itf.split(":");
                String itfName = parts.length == 1 ? parts[0] : parts[1];
                Element binding = wsdlFile.find("binding[name=" + itfName + "]").single();
                itfElement = wsdlFile.find("portType[name=" + binding.type().split(":")[1] + "]").single();
            } else {
                throw new IllegalArgumentException("What to do with " + service);
            }
        }

        public Element operationElement(String name) {
            return itfElement.find("operation[name=" + name + "]").single();
        }

        public SoapOperationDefinition operation(String name) {
            return new SoapOperationDefinition(operationElement(name));
        }

    }

    private Document wsdlFile;
    private Map<Namespace, Element> schemas = new HashMap<>();

    public SampleSoapXmlBuilder(String wsdlResource) throws IOException {
        wsdlFile = Xml.readResource("/" + wsdlResource);
        for (Element schema : wsdlFile.find("types", XS.name("schema"))) {
            schemas.put(new Namespace(schema.attr("targetNamespace")), schema);
        }
    }

    public SampleSoapXmlBuilder(URL resource) throws IOException {
        wsdlFile = Xml.read(resource);
        for (Element schema : wsdlFile.find("types", "schema")) {
            if (schema.find("import").isPresent()) {
                schemas.put(new Namespace(schema.find("import").single().attr("namespace")),
                        Xml.read(new URL(resource, schema.find("import").single().attr("schemaLocation")))
                                .getRootElement());
            } else {
                schemas.put(new Namespace(schema.attr("targetNamespace")), schema);
            }
        }
    }

    public SoapServiceDefinition service(String name) {
        return new SoapServiceDefinition(name);
    }

}
