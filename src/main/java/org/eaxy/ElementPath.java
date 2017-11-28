package org.eaxy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import javax.annotation.Nonnull;


public class ElementPath {

	@Nonnull
    private final Element element;
    private ElementPath parent;

    public ElementPath(ElementPath parent, @Nonnull Element element) {
        this.element = element;
        this.parent = parent;
    }

    @Nonnull
    public Element leafElement() {
        return element;
    }

    @Nonnull
    public List<Element> getPath() {
        ArrayList<Element> path = new ArrayList<Element>();
        ElementPath current = this;
        do {
            path.add(current.element);
            current = current.parent;
        } while (current != null);
        Collections.reverse(path);
        return path;
    }

	@Nonnull
    public Collection<Namespace> namespaces() {
        LinkedHashMap<String,Namespace> namespaces = new LinkedHashMap<>();

        for (Element hierarchy : getPath()) {
            for (Namespace namespace : hierarchy.getNamespaces()) {
                namespaces.put(namespace.getPrefix(), namespace);
            }
        }
        return namespaces.values();
    }

    @Override
    public String toString() {
        return "..." + element;
    }

}
