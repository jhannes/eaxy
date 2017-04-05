package org.eaxy;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Element implements Node {

    private final QualifiedName name;
    private final List<Node> children;
    private final Map<QualifiedName,Attribute> attributes = new LinkedHashMap<QualifiedName, Attribute>();
    // TODO: Maybe namespaces should be part of the attributes - are namespaces attributes?
    private final List<Namespace> namespaces = new ArrayList<Namespace>();

    Element(QualifiedName name, Content... contents) {
        this(name, Objects.list(contents, Node.class),
                Objects.list(contents, Attribute.class),
                Objects.list(contents, Namespace.class));
    }

    public Element(QualifiedName name, List<Node> children, Collection<Attribute> attrs, Collection<Namespace> namespaces) {
        this.name = name;
        if (name.hasNamespace()) namespace(name.getNamespace());
        this.children = children;
        attrs(attrs);
        for (Namespace namespace : namespaces) {
            namespace(namespace);
        }
    }

    public String tagName() {
        return name.getName();
    }

    public List<Namespace> getNamespaces() {
        return namespaces;
    }

    public <T extends Node> Element addAll(@SuppressWarnings("unchecked") T... content) {
        for (T node : content) {
            add(node);
        }
        return this;
    }

    public <T extends Node> Element addAll(Collection<T> content) {
        for (T node : content) {
            add(node);
        }
        return this;
    }

    public Element add(Node node) {
        this.children.add(node);
        return this;
    }

    @Override
    public void writeTo(Writer writer, LinkedList<Namespace> printedNamespaces) throws IOException {
        if (children.isEmpty()) {
            writer.write("<" + printTag() + printNamespaces(printedNamespaces) + printAttributes() + " />");
        } else {
            writer.write("<" + printTag() + printNamespaces(printedNamespaces) + printAttributes() + ">");
            printContent(writer, printedNamespaces);
            writer.write("</" + printTag() + ">");
        }
    }

    @Override
    public void writeIndentedTo(Writer writer, LinkedList<Namespace> printedNamespaces, String indent, String currentIndent) throws IOException {
        if (children.isEmpty()) {
            writer.write(currentIndent + "<" + printTag() + printNamespaces(printedNamespaces) + printAttributes() + " />" + Document.LINE_SEPARATOR);
        } else if (elements().isEmpty()) {
            writer.write(currentIndent + "<" + printTag() + printNamespaces(printedNamespaces) + printAttributes() + ">");
            printContent(writer, printedNamespaces, indent, currentIndent + indent);
            writer.write("</" + printTag() + ">" + Document.LINE_SEPARATOR);
        } else {
            writer.write(currentIndent + "<" + printTag() + printNamespaces(printedNamespaces) + printAttributes() + ">" + Document.LINE_SEPARATOR);
            printContent(writer, printedNamespaces, indent, currentIndent + indent);
            writer.write(currentIndent + "</" + printTag() + ">" + Document.LINE_SEPARATOR);
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
            if (!namespace.isNamespace()) throw new IllegalStateException(name());
            if (printedNamespaces.contains(namespace)) continue;
            result.append(" ").append(namespace.print());
        }
        return result.toString();
    }

    private void printContent(Writer writer, List<Namespace> printedNamespaces2) throws IOException {
        LinkedList<Namespace> printedNamespaces = new LinkedList<Namespace>();
        printedNamespaces.addAll(printedNamespaces2);
        printedNamespaces.addAll(namespaces);
        for (Node element : children) {
            element.writeTo(writer, printedNamespaces);
        }
    }

    private void printContent(Writer writer, List<Namespace> printedNamespaces2, String indent, String currentIndent) throws IOException {
        LinkedList<Namespace> printedNamespaces = new LinkedList<Namespace>();
        printedNamespaces.addAll(printedNamespaces2);
        printedNamespaces.addAll(namespaces);
        for (Node element : children) {
            element.writeIndentedTo(writer, printedNamespaces, indent, currentIndent);
        }
    }

    @Override
    public String text() {
        StringBuilder result = new StringBuilder();
        for (Node element : children) {
            result.append(element.text());
        }
        return result.toString();
    }

    public Element text(String string) {
        this.children.clear();
        this.children.add(Xml.text(string));
        return this;
    }

    public Map<String, String> attrs() {
        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        for (QualifiedName attrName : attributes.keySet()) {
            result.put(attrName.getName(), attr(attrName));
        }
        return result;
    }

    public String attr(String attrName) {
        return attr(new QualifiedName(attrName));
    }

    public String attr(QualifiedName key) {
        if (!key.hasNamespace()) {
            for (Attribute attr : attributes.values()) {
                if (attr.getKey().matches(key)) return attr.getValue();
            }
        }
        Attribute attribute = attributes.get(key);
        return attribute != null ? attribute.getValue() : null;
    }

    public Element attr(String name, String value) {
        return attr(new QualifiedName(name), value);
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
        if (attribute.getKey().hasNamespace()) {
            namespace(attribute.getKey().getNamespace());
        }
        attributes.put(attribute.getKey(), attribute);
        return this;
    }

    public boolean hasAttr(String name) {
        return attributes.containsKey(new QualifiedName(name));
    }

    public void writeTo(Writer writer) throws IOException {
        writeTo(writer, new LinkedList<Namespace>());
    }

    public String toXML() {
        try {
            StringWriter result = new StringWriter();
            writeTo(result, new LinkedList<Namespace>());
            return result.toString();
        } catch (IOException e) {
            throw new CanNeverHappenException("StringWriter doesn't throw IOException", e);
        }
    }

    Element namespace(Namespace namespace) {
        if (namespace.getUri() == null) {
            throw new IllegalArgumentException("Invalid namespace " + namespace);
        }
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
    // TODO: Will the paths pretty much always be "...", something?
    public ElementSet find(Object... path) {
        return new ElementSet(this).find(path);
    }

    public Element select(Object filter) {
        return find("...", filter).first();
    }

    public Element take(Object selector) {
        Element result = select(selector);
        children.remove(result);
        return result;
    }

    public List<? extends Element> elements() {
        return Objects.list(children, Element.class);
    }

    public List<Node> children() {
        return children;
    }

    public String className() {
        return attr("class");
    }

    public Element addClass(String newClass) {
        return attr("class", className() != null ? className() + " " + newClass : newClass);
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

    @Override
    public Element copy() {
        return new Element(this.name, copyChildren(), attributes.values(), namespaces);
    }

    private List<Node> copyChildren() {
        List<Node> list = new ArrayList<Node>();
        for (Node o : children) {
            list.add(o.copy());
        }
        return list;
    }

    Element attrs(Collection<Attribute> attributes) {
        for (Attribute attribute : attributes) {
            attr(attribute);
        }
        return this;
    }

    public void delete(Element existingChild) {
        children.remove(existingChild);
    }

    public Set<QualifiedName> attrNames() {
        return attributes.keySet();
    }

}

