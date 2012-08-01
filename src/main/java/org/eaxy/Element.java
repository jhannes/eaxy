package org.eaxy;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Element implements Node {

    private final QualifiedName name;
    private final List<Node> content;
    private final Map<QualifiedName,Attribute> attributes = new LinkedHashMap<QualifiedName, Attribute>();
    // TODO: Maybe namespaces should be part of the attributes - are namespaces attributes?
    private final List<Namespace> namespaces = new ArrayList<Namespace>();

    Element(QualifiedName name, List<Node> content) {
        this.name = name;
        this.content = content;
        this.namespaces.add(name.getNamespace());
    }

    public String tagName() {
        return name.getName();
    }

    public Element addAll(Node... content) {
        for (Node node : content) {
            add(node);
        }
        return this;
    }

    public Element add(Node node) {
        this.content.add(node);
        return this;
    }

    Element attrs(Attributes attributes) {
        for (int i = 0; i < attributes.getLength(); i++) {
            attr(attributes.getLocalName(i), attributes.getValue(i));
        }
        return this;
    }

    @Override
    public void print(Writer writer, LinkedList<Namespace> printedNamespaces) throws IOException {
        if (content.isEmpty()) {
            writer.write("<" + printTag() + printNamespaces(printedNamespaces) + printAttributes() + " />");
        } else {
            writer.write("<" + printTag() + printNamespaces(printedNamespaces) + printAttributes() + ">");
            printContent(writer, printedNamespaces);
            writer.write("</" + printTag() + ">");
        }
    }

    private String printTag() {
        return name.print();
    }

    private String printAttributes() {
        if (attributes.isEmpty()) return "";
        StringBuilder result = new StringBuilder();
        for (Attribute attr : attributes.values()) {
            result.append(" ").append(attr.toXML());
        }
        return result.toString();
    }

    private String printNamespaces(LinkedList<Namespace> printedNamespaces) {
        StringBuilder result = new StringBuilder();
        for (Namespace namespace : namespaces) {
            if (printedNamespaces.contains(namespace) || namespace.isNoNamespace()) continue;
            result.append(" ").append(namespace.print());
        }
        return result.toString();
    }

    private void printContent(Writer writer, List<Namespace> printedNamespaces2) throws IOException {
        LinkedList<Namespace> printedNamespaces = new LinkedList<Namespace>();
        printedNamespaces.addAll(printedNamespaces2);
        printedNamespaces.addAll(namespaces);
        for (Node element : content) {
            element.print(writer, printedNamespaces);
        }
    }

    @Override
    public String text() {
        StringBuilder result = new StringBuilder();
        for (Node element : content) {
            result.append(element.text());
        }
        return result.toString();
    }

    public Element text(String string) {
        this.content.clear();
        this.content.add(Xml.text(string));
        return this;
    }

    public String attr(String attrName) {
        return attr(new QualifiedName(attrName));
    }

    public String attr(QualifiedName key) {
        if (key.getNamespace() == Namespace.NO_NAMESPACE) {
            for (Attribute attr : attributes.values()) {
                if (attr.getKey().matches(key)) return attr.getValue();
            }
        }
        Attribute attribute = attributes.get(key);
        return attribute != null ? attribute.getValue() : null;
    }

    public Element attr(String key, String value) {
        return attr(new QualifiedName(key), value);
    }

    public Element attr(QualifiedName key, String value) {
        if (value == null) {
            attributes.remove(key);
        } else {
            attr(new Attribute(key, value));
        }
        return this;
    }

    public Element attr(Attribute attribute) {
        xmlns(attribute.getKey().getNamespace());
        attributes.put(attribute.getKey(), attribute);
        return this;
    }

    public boolean hasAttr(String name) {
        return attributes.containsKey(new QualifiedName(name));
    }

    public void writeTo(Writer writer) throws IOException {
        print(writer, new LinkedList<Namespace>());
    }

    public String toXML() {
        try {
            StringWriter result = new StringWriter();
            print(result, new LinkedList<Namespace>());
            return result.toString();
        } catch (IOException e) {
            throw new CanNeverHappenException("StringWriter doesn't throw IOException", e);
        }
    }

    public Element xmlns(Namespace namespace) {
        if (!namespaces.contains(namespace)) {
            namespaces.add(namespace);
        }
        return this;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + printTag() + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Element)) return false;
        return Objects.equals(name, ((Element)obj).name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    // TODO: Would an event based finder (instead of returning a set) be more efficient?
    public ElementSet find(Object... path) {
        return new ElementSet(this).find(path);
    }

    public Element select(String element) {
        return find("...", element).first();
    }

    public Collection<? extends Element> elements() {
        ArrayList<Element> result = new ArrayList<Element>();
        for (Node node : content) {
            if (node instanceof Element) {
                result.add((Element)node);
            }
        }
        return result;
    }

    public String className() {
        return attr("class");
    }

    public Element addClass(String newClass) {
        attr("class", className() != null ? className() + " " + newClass : newClass);
        return this;
    }

    public Element removeClass(String classToRemove) {
        if (hasClass(classToRemove)) {
            attr("class", className().replaceAll("\\s*\\b" + classToRemove + "\\b", ""));
        }
        return this;
    }

    public boolean hasClass(String className) {
        return className().matches(".*\\b" + className + "\\b.*");
    }

    public String name() {
        return attr("name");
    }

    public Element name(String name) {
        return attr("name", name);
    }

    public String id() {
        return attr("id");
    }

    public Element id(String id) {
        return attr("id", id);
    }

    public String type() {
        return attr("type");
    }

    public Element type(String type) {
        return attr("type", type);
    }

    public String val() {
        return attr("value");
    }

    public Element val(String value) {
        return attr("value", value);
    }

    public QualifiedName getName() {
        return name;
    }

    public boolean checked() {
        return attr("checked") != null;
    }

    public Element checked(boolean checked) {
        if (checked) {
            attr("checked", "checked");
        } else {
            attributes.remove(new QualifiedName("checked"));
        }
        return this;
    }

    public boolean selected() {
        return attr("selected") != null;
    }

    public Element selected(boolean selected) {
        if (selected) {
            attr("selected", "selected");
        } else {
            attributes.remove(new QualifiedName("selected"));
        }
        return this;
    }

    public org.w3c.dom.Document toDom() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            String xml = new org.eaxy.Document(this).toXML();
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

