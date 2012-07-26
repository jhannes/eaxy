package org.eaxy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class ElementSet implements Iterable<Element> {

    private static ElementSet NULL_ELEMENT_SET = new ElementSet(null, null) {
        @Override
        public List<Object> getPath() { return new ArrayList<Object>(); }
    };

    private List<Element> elements = new ArrayList<Element>();
    private ElementSet parent = NULL_ELEMENT_SET;
    private final Object filter;

    public ElementSet(Element element) {
        this.elements.add(element);
        this.filter = element.getName().print();
    }

    private ElementSet(ElementSet parent, Object filter) {
        this.parent = parent;
        this.filter = filter;
    }

    private ElementSet(ElementSet parent, ElementQuery filter, List<Element> elements) {
        this.parent = parent;
        this.filter = filter;
        this.elements = elements;
    }

    @Override
    public Iterator<Element> iterator() {
        return elements.iterator();
    }

    public ElementSet find(Object... path) {
        return ElementFilters.create(path).search(this);
    }

    public ElementSet nestedSet(ElementQuery filter, List<Element> elements) {
        return new ElementSet(this, filter, elements);
    }

    public ElementSet check() {
        if (!elements.isEmpty()) return this;
        parent.check();
        String message = "Can't find <" + filter + "> below " + parent.getPath() + ".";
        message += " Actual elements: " + parent.getActualChildren();
        throw new NonMatchingPathException(message);
    }

    public String getActualChildren() {
        List<Element> children = new ArrayList<Element>();
        for (Element element : elements) {
            children.addAll(element.elements());
        }
        return children.toString();
    }

    public List<Object> getPath() {
        List<Object> result = new ArrayList<Object>();
        result.addAll(parent.getPath());
        result.add(filter.toString());
        return result;
    }

    public List<String> texts() {
        List<String> result = new ArrayList<String>();
        for (Element element : elements) {
            result.add(element.text().toString());
        }
        return result;
    }

    public List<String> attrs(String attrName) {
        List<String> result = new ArrayList<String>();
        for (Element element : elements) {
            String attr = element.attr(attrName);
            if (attr != null) result.add(attr);
        }
        return result;
    }

    public Element first() {
        check();
        return elements.get(0);
    }

    public ElementSet attr(String key, String value) {
        for (Element element : elements) {
            element.attr(key, value);
        }
        return this;
    }

    public List<String> ids() {
        return attrs("id");
    }

    public List<String> values() {
        return attrs("value");
    }

    public List<String> names() {
        return attrs("name");
    }

    public List<String> tagNames() {
        List<String> result = new ArrayList<String>();
        for (Element element : elements) {
            result.add(element.getName().print());
        }
        return result;
    }

    public Element get(int pos) {
        return elements.get(pos);
    }

    public int size() {
        return elements.size();
    }

    public Collection<? extends Element> elements() {
        return elements;
    }
}
