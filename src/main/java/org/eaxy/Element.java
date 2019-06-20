package org.eaxy;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class Element implements Node {

    private final QualifiedName name;
    @Nonnull
    private final List<Node> children = new ArrayList<>();
    private final Map<QualifiedName,Attribute> attributes = new LinkedHashMap<>();
    // TODO: Maybe namespaces should be part of the attributes - are namespaces attributes?
    private final List<Namespace> namespaces = new ArrayList<>();
    private Integer lineNumber;

    Element(QualifiedName name, Content... contents) {
        this(name, Objects.list(contents, Attribute.class),
                Objects.list(contents, Namespace.class), null);
        children.addAll(Objects.list(contents, Node.class));
    }

    Element(QualifiedName name, Iterable<Content> contents) {
        this(name, Objects.list(contents, Attribute.class),
                Objects.list(contents, Namespace.class), null);
        children.addAll(Objects.list(contents, Node.class));
    }

    Element(QualifiedName name, Collection<Attribute> attrs, Collection<Namespace> namespaces, @Nullable Integer lineNumber) {
        this.name = name;
		this.lineNumber = lineNumber;
        if (name.hasNamespace() && !namespaces.contains(name.getNamespace())) {
            namespace(name.getNamespace());
        }
        for (Namespace namespace : namespaces) {
            namespace(namespace);
        }
        attrs(attrs);
    }

    public Element(QualifiedName name, int lineNumber) {
    	this(name);
		this.lineNumber = lineNumber;
	}

    @Nonnull
	public String tagName() {
        return name.getName();
    }

    public List<Namespace> getNamespaces() {
        return namespaces;
    }

    public Namespace getNamespace(String prefix) {
        for (Namespace namespace : namespaces) {
            if (Objects.equals(prefix, namespace.getPrefix())) {
                return namespace;
            }
        }
        throw new IllegalArgumentException(prefix + " not found in " + namespaces);
    }

    public void extendNamespaces(List<Namespace> additionalNamespaces) {
        for (Namespace namespace : additionalNamespaces) {
            namespace(namespace);
        }
    }

    @SafeVarargs
    public final <T extends Node> Element addAll(T... content) {
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
    public void visit(XmlVisitor visitor) throws IOException {
        visitor.visitElement(this);
    }

    public String printTag() {
        return name.print();
    }

    public String printAttributes() {
        if (attributes.isEmpty()) return "";
        StringBuilder result = new StringBuilder();
        for (Attribute attr : attributes.values()) {
            result.append(" ").append(attr.toXML());
        }
        return result.toString();
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

    @Nonnull
    public Map<String, String> attrs() {
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        for (QualifiedName attrName : attributes.keySet()) {
            result.put(attrName.getName(), attr(attrName));
        }
        return result;
    }

    //@Nullable
    public String attr(String attrName) {
        return attr(new QualifiedName(attrName));
    }

    @Nullable
    public String attr(QualifiedName key) {
        if (!key.hasNamespace()) {
            for (Attribute attr : attributes.values()) {
                if (attr.getKey().matches(key)) return attr.getValue();
            }
        }
        Attribute attribute = attributes.get(key);
        return attribute != null ? attribute.getValue() : null;
    }

    public Element attr(String name, @Nullable String value) {
        return attr(new QualifiedName(name), value);
    }

    public Element attr(QualifiedName key, @Nullable String value) {
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

    public String toIndentedXML() {
        StringWriter result = new StringWriter();
        try {
            visit(new IntentedWriterXmlVisitor(result, "  "));
        } catch (IOException e) {
            throw new CanNeverHappenException("StringBuilder doesn't throw IOException", e);
        }
        return result.toString();
    }

    public String toXML() {
        try {
            StringWriter result = new StringWriter();
            this.visit(new WriterXmlVisitor(result));
            return result.toString();
        } catch (IOException e) {
            throw new CanNeverHappenException("StringWriter doesn't throw IOException", e);
        }
    }

    @Nonnull
    Element namespace(Namespace namespace) {
        if (namespace.getUri() == null) {
            throw new IllegalArgumentException("Invalid namespace " + namespace);
        }
        for (Namespace existingNamespace : namespaces) {
            if (Objects.equals(namespace.getPrefix(), existingNamespace.getPrefix())) {
                return this;
            }
        }
        namespaces.add(namespace);
        return this;
    }

    @Override
    public String toString() {
        if (children.isEmpty()) {
            return "<" + printTag() + printAttributes() + " />" + (lineNumber != null ? "@" + lineNumber : "");
        } else if (children.size() == 1 && !(children.get(0) instanceof Element)) {
            return "<" + printTag() + printAttributes() + ">" + children.get(0).toString().trim() + "</" + printTag() + ">" + (lineNumber != null ? "@" + lineNumber : "");
        } else {
            return "<" + printTag() + printAttributes() + ">...</" + printTag() + ">" + (lineNumber != null ? "@" + lineNumber : "");
        }
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

    @Nonnull
    public ElementSet find(Object... path) {
        return new ElementSet(this).find(path);
    }

    @Nonnull
    public Element select(Object filter) {
        return find("...", filter).single();
    }

    @Nonnull
    public Element take(Object selector) {
        Element result = select(selector);
        children.remove(result);
        return result;
    }

    @Nonnull
    public List<? extends Element> elements() {
        return Objects.list(children, Element.class);
    }

    @Nonnull
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

    //@Nullable
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
        Element element = copyElement();
        for (Node o : children) {
            element.children.add(o.copy());
        }
        return element;
    }

    public Element copyElement() {
        return new Element(this.name, attributes.values(), namespaces, lineNumber);
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

