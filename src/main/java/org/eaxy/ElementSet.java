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

    private List<ElementPath> elementPaths = new ArrayList<>();
    private ElementSet parentSet = NULL_ELEMENT_SET;
    private final Object filter;

    public ElementSet(Element element) {
        this.elementPaths.add(new ElementPath(null, element));
        this.filter = element.getName().print();
    }

    private ElementSet(ElementSet parent, Object filter) {
        this.parentSet = parent;
        this.filter = filter;
    }

    private ElementSet(ElementSet parent, ElementQuery filter, List<ElementPath> elementPaths) {
        this.parentSet = parent;
        this.filter = filter;
        this.elementPaths = elementPaths;
    }

    @Override
    public Iterator<Element> iterator() {
        return elements().iterator();
    }

    public ElementSet find(Object... path) {
        return ElementFilters.create(path).search(this);
    }

    public ElementSet nestedSet(ElementQuery filter, List<ElementPath> elementPaths) {
        return new ElementSet(this, filter, elementPaths);
    }

    public ElementSet check() {
        if (!elementPaths.isEmpty()) return this;
        parentSet.check();
        String message = "Can't find <" + filter + "> below " + parentSet.getPath() + ".";
        message += " Actual elements: " + parentSet.printActualChildren();
        throw new NonMatchingPathException(message);
    }

    private String printActualChildren() {
        List<String> children = new ArrayList<String>();
        for (ElementPath path : elementPaths) {
            for (Element subElement : path.leafElement().elements()) {
                children.add(subElement.getName().toString());
            }
        }
        return children.toString();
    }

    public List<Object> getPath() {
        List<Object> result = new ArrayList<Object>();
        result.addAll(parentSet.getPath());
        result.add(filter.toString());
        return result;
    }

    public List<String> texts() {
        List<String> result = new ArrayList<String>();
        for (ElementPath element : elementPaths) {
            result.add(element.leafElement().text().toString());
        }
        return result;
    }

    public List<String> attrs(String attrName) {
        List<String> result = new ArrayList<String>();
        for (ElementPath element : elementPaths) {
            String attr = element.leafElement().attr(attrName);
            if (attr != null) result.add(attr);
        }
        return result;
    }

    public Element first() {
        return firstPath().leafElement();
    }

    public Element single() {
        checkMaxOneMatch();
        return first();
    }

    public Element singleOrDefault() {
        checkMaxOneMatch();
        return firstOrDefault();
    }

    private void checkMaxOneMatch() {
        if (size() <= 1) return;
        String message = "Too many matches for <" + filter + ">: " + elementPaths;
        throw new IllegalArgumentException(message);
    }

    public ElementPath firstPath() {
        check();
        return elementPaths.get(0);
    }

    public List<ElementPath> getPaths() {
        return elementPaths;
    }

    public ElementSet attr(String key, String value) {
        for (ElementPath element : elementPaths) {
            element.leafElement().attr(key, value);
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
        for (ElementPath element : elementPaths) {
            result.add(element.leafElement().getName().print());
        }
        return result;
    }

    public Element get(int pos) {
        check();
        return elementPaths.get(pos).leafElement();
    }

    public int size() {
        return elementPaths.size();
    }

    public Collection<Element> elements() {
        List<Element> elements = new ArrayList<>();
        for (ElementPath path : elementPaths) {
            elements.add(path.leafElement());
        }
        return elements;
    }

    public Element firstOrDefault() {
        return isEmpty() ? null : first();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean isPresent() {
        return !isEmpty();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{size=" + size() + "}";
    }

}
