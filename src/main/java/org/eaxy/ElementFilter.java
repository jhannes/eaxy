package org.eaxy;

import java.util.ArrayList;

abstract class ElementFilter implements ElementQuery {

    private final String filterName;

    ElementFilter(String filterName) {
        this.filterName = filterName;
    }

    public abstract boolean matches(Element element);

    @Override
    public String toString() {
        return filterName;
    }

    @Override
    public final ElementSet search(ElementSet elements) {
        ArrayList<Element> matchingElements = new ArrayList<Element>();
        for (Element element : elements) {
            for (Element child : element.elements()) {
                if (this.matches(child)) {
                    matchingElements.add(child);
                }
            }
        }
        return elements.nestedSet(this, matchingElements);
    }
}
