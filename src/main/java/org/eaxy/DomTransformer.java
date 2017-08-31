package org.eaxy;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eaxy.Xml.CDataElement;
import org.eaxy.Xml.CommentElement;
import org.eaxy.Xml.TextElement;

public class DomTransformer {

    public static Document fromDom(org.w3c.dom.Document document) {
        Document doc = new Document();
        if (document.getXmlEncoding() != null) {
            doc.setEncoding(document.getXmlEncoding());
        }
        doc.setVersion(document.getXmlVersion());

        if (document.getDoctype() != null) doc.addDTD(document.getDoctype().getBaseURI());
        doc.setRootElement((Element)createNode(document.getDocumentElement()));

        return doc;
    }

    private static Node createNode(org.w3c.dom.Node node) {
        if (node instanceof org.w3c.dom.CDATASection) {
            return Xml.cdata(node.getTextContent());
        } else if (node instanceof org.w3c.dom.Text) {
            return Xml.text(node.getTextContent());
        } else if (node instanceof org.w3c.dom.Comment) {
            return Xml.comment(node.getTextContent());
        } else if (node instanceof org.w3c.dom.Element) {
            return element(node);
        } else {
            throw new IllegalArgumentException(node.toString());
        }

    }

    private static Element element(org.w3c.dom.Node node) {
        Element element = new Element(createName(node));
        org.w3c.dom.NamedNodeMap attributes = node.getAttributes();
        if (attributes != null) {
            for (int i=0; i<attributes.getLength(); i++) {
                org.w3c.dom.Node attr = attributes.item(i);
                if ("xmlns".equals(attr.getPrefix())) {
                    element.namespace(new Namespace(attr.getTextContent(), attr.getLocalName()));
                } else {
                    element.attr(createName(attr), attr.getTextContent());
                }
            }
        }

        org.w3c.dom.NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            element.add(createNode(childNodes.item(i)));
        }
        return element;
    }

    private static QualifiedName createName(org.w3c.dom.Node node) {
        return new QualifiedName(node.getNamespaceURI(), node.getNodeName());
    }

    public static org.w3c.dom.Document toDom(Document document) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            org.w3c.dom.Document doc = factory.newDocumentBuilder().newDocument();
            doc.setXmlVersion(document.getVersion());
            doc.appendChild(createElement(doc, document.getRootElement()));
            return doc;
        } catch (ParserConfigurationException e) {
            throw new CanNeverHappenException("Oh, just shut up!", e);
        }
    }

    private static org.w3c.dom.Node createElement(org.w3c.dom.Document doc, Element element) {
        org.w3c.dom.Element domElement = createElement(doc, element.getName());

        for (Namespace namespace : element.getNamespaces()) {
            addNamespace(domElement, namespace);
        }

        for (QualifiedName attrName : element.attrNames()) {
            addAttribute(domElement, attrName, element.attr(attrName));
        }

        for (org.eaxy.Node child : element.children()) {
            if (child instanceof Element) {
                domElement.appendChild(createElement(doc, (Element)child));
            } else if (child instanceof TextElement) {
                domElement.appendChild(doc.createTextNode(child.text().toString()));
            } else if (child instanceof CDataElement) {
                domElement.appendChild(doc.createCDATASection(child.text().toString()));
            } else if (child instanceof CommentElement) {
                domElement.appendChild(doc.createComment(child.text().toString()));
            } else {
                throw new IllegalArgumentException(child.toString());
            }
        }

        return domElement;
    }

    private static void addAttribute(org.w3c.dom.Element domElement, QualifiedName attrName, String value) {
        domElement.setAttributeNS(attrName.getNamespace().getUri(), attrName.print(), value);
    }

    private static void addNamespace(org.w3c.dom.Element domElement, Namespace namespace) {
        domElement.setAttributeNS("http://www.w3.org/2000/xmlns/", namespace.xmlns(), namespace.getUri());
    }

    private static org.w3c.dom.Element createElement(org.w3c.dom.Document doc, QualifiedName name) {
        return doc.createElementNS(name.getNamespace().getUri(), name.print());
    }

}
