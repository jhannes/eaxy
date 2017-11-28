package org.eaxy;

import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
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

    @SuppressWarnings("null")
	@Override @Nonnull
    public final ElementSet search(ElementSet elements) {
        List<ElementPath> elementPaths = new ArrayList<ElementPath>();
        for (ElementPath element : elements.getPaths()) {
            for (@Nonnull Element child : element.leafElement().elements()) {
                if (this.matches(child)) {
                    elementPaths.add(new ElementPath(element, child));
                }
            }
        }
        return elements.nestedSet(this, elementPaths);
    }

    @Override @Nonnull
    public Iterable<Element> iterate(@Nonnull Reader reader) {
        return XmlIterator.iterate(this, reader);
    }

    @Override @Nonnull
    public Iterable<Element> iterate(@Nonnull URL url) {
        return XmlIterator.iterate(this, url);
    }
}
