package org.eaxy;

import java.util.ArrayList;
import java.util.List;

abstract class ElementFilter implements ElementQuery {

    private final String filterName;

    ElementFilter(String filterName) {
        this.filterName = filterName;
    }

    @Override
    public boolean matches(List<Element> path, int position) {
        return position < path.size() && matches(path.get(position));
    }

    public abstract boolean matches(Element element);

    @Override
    public String toString() {
        return filterName;
    }

    @Override
    public final ElementSet search(ElementSet elements) {
        List<ElementPath> elementPaths = new ArrayList<ElementPath>();
        for (ElementPath element : elements.getPaths()) {
            for (Element child : element.leafElement().elements()) {
                if (this.matches(child)) {
                    elementPaths.add(new ElementPath(element, child));
                }
            }
        }
        return elements.nestedSet(this, elementPaths);
    }
}
