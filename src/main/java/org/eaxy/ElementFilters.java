package org.eaxy;

import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ElementFilters {

    private static class ChildQuery implements ElementQuery {

        private final ElementQuery parent;
        private final ElementQuery child;

        private ChildQuery(ElementQuery parent, ElementQuery child) {
            this.parent = parent;
            this.child = child;
        }

        @Override @Nonnull
        public ElementSet search(@Nonnull ElementSet elements) {
            return child.search(parent.search(elements));
        }

        @Override
        public boolean matches(List<Element> path, int position) {
            return position < path.size()
                    && ((ElementFilter)parent).matches(path.get(position))
                    && child.matches(path, position + 1);
        }

        @Override
        public String toString() {
            return parent + "/" + child;
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

    private static final class ElementDescendantQuery implements ElementQuery {
        private final ElementFilter filter;
        private ElementQuery next;

        private ElementDescendantQuery(ElementQuery filter) {
            if (filter instanceof ChildQuery) {
                this.filter = (ElementFilter) ((ChildQuery)filter).parent;
                this.next = ((ChildQuery)filter).child;
            } else {
                this.filter = (ElementFilter)filter;
                this.next = new Identity();
            }
        }

        @Override @Nonnull
        public ElementSet search(@Nonnull ElementSet elements) {
            List<ElementPath> elementPaths = new ArrayList<>();
            for (ElementPath element : elements.getPaths()) {
                findDescendants(element, elementPaths);
            }
            return elements.nestedSet(this, elementPaths);
        }

        @Override
        public boolean matches(List<Element> path, int position) {
            return position < path.size() && filter.matches(path.get(path.size()-1));
        }

        @SuppressWarnings("null")
		private void findDescendants(ElementPath element, List<ElementPath> elementPaths) {
            for (Element child : element.leafElement().elements()) {
                if (filter.matches(child)) {
                    ElementSet search = next.search(new ElementSet(child));
                    for (ElementPath elementPath : search.getPaths()) {
                        elementPaths.add(new ElementPath(element, elementPath.leafElement()));
                    }
                }
                findDescendants(new ElementPath(element, child), elementPaths);
            }
        }

        @Override
        public String toString() {
            return "...//" + filter + "/" + next;
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

    private static final class ElementPositionFilter implements ElementQuery {
        private final Number position;

        private ElementPositionFilter(Number position) {
            this.position = position;
        }

        @Override @Nonnull
        public ElementSet search(@Nonnull ElementSet elements) {
            if (intValue() < elements.size()) {
                ElementPath path = elements.getPaths().get(intValue());
                return elements.nestedSet(this, Arrays.asList(path));
            } else {
                return elements.nestedSet(this, new ArrayList<ElementPath>());
            }
        }

        private int intValue() {
            return position.intValue();
        }

        @Override @Nonnull
        public Iterable<Element> iterate(@Nonnull Reader reader) {
            throw new UnsupportedOperationException();
        }

        @Override @Nonnull
        public Iterable<Element> iterate(@Nonnull URL url) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean matches(List<Element> path, int position) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return position.toString();
        }
    }

    private final static Pattern ATTRIBUTE_PATTERN = Pattern.compile("(.*)\\[(.+)=(.+)\\]");
    private final static Pattern ID_PATTERN = Pattern.compile("(.*)#(.+)");
    private final static Pattern CLASS_NAME_PATTERN = Pattern.compile("(.*)\\.(.+)");

    public static ElementQuery stringFilter(String filter) {
        if (filter.isEmpty() || filter.equals("*")) {
            return any();
        }
        if (filter.equals("...")) {
            return new ElementDescendantQuery(any());
        }
        ElementFilter elementFilter;
        elementFilter = attrFilter(filter);
        if (elementFilter != null) return elementFilter;
        elementFilter = idFilter(filter);
        if (elementFilter != null) return elementFilter;
        elementFilter = classNameFilter(filter);
        if (elementFilter != null) return elementFilter;
        return tagName(filter);
    }

    public static ElementQuery create(Object... path) {
        ElementQuery query = filter(path[path.length-1]);
        for (int i = path.length-2; i >= 0 ; i--) {
            Object filter = path[i];
            if (filter.equals("...")) {
                query = new ElementDescendantQuery(query);
            } else {
                query = new ChildQuery(filter(filter), query);
            }
        }
        return query;
    }

    public static class Identity implements ElementQuery {
        @Override @Nonnull
        public ElementSet search(@Nonnull ElementSet elements) {
            return elements;
        }

        @Override
        public boolean matches(List<Element> path, int position) {
            return true;
        }

        @Override
        public String toString() {
            return ".";
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

    public static ElementQuery filter(Object filter) {
        if (filter instanceof Attribute) {
            return attrFilter((Attribute)filter);
        } else if (filter instanceof CharSequence) {
            return stringFilter(filter.toString());
        } else if (filter instanceof QualifiedName) {
            return tagName((QualifiedName)filter);
        } else if (filter instanceof Number) {
            return position((Number)filter);
        } else {
            return (ElementQuery)filter;
        }
    }

    public static ElementQuery position(Number filter) {
        return new ElementPositionFilter(filter);
    }

    @Nullable
    public static ElementFilter idFilter(String filter) {
        Matcher matcher = ID_PATTERN.matcher(filter);
        if (matcher.matches()) {
            return and(filter,
                    tagName(matcher.group(1)),
                    attrFilter("id", matcher.group(2)));
        }
        return null;
    }

    @Nullable
    public static ElementFilter classNameFilter(String filter) {
        Matcher matcher = CLASS_NAME_PATTERN.matcher(filter);
        if (matcher.matches()) {
            return and(filter,
                    tagName(matcher.group(1)),
                    attrFilter("class", matcher.group(2)));
        }
        return null;
    }

    @Nullable
    public static ElementFilter attrFilter(String filter) {
        Matcher matcher = ATTRIBUTE_PATTERN.matcher(filter);
        if (matcher.matches()) {
            return and(filter,
                    tagName(matcher.group(1)),
                    attrFilter(matcher.group(2), matcher.group(3)));
        }
        return null;
    }

    public static ElementFilter attrFilter(String attributeName, String value) {
        return attrFilter(Namespace.NO_NAMESPACE.attr(attributeName, value));
    }

    public static ElementFilter attrFilter(final Attribute attr) {
        return new ElementFilter(attr.toString()) {
            @Override
            public boolean matches(Element element) {
                return attr.getValue().equals(element.attr(attr.getKey()));
            }
        };
    }

    public static ElementFilter and(String name, final ElementFilter... filters) {
        return new ElementFilter(name) {
            @Override
            public boolean matches(Element element) {
                for (ElementFilter filter : filters) {
                    if (!filter.matches(element)) return false;
                }
                return true;
            }
        };
    }

    public static ElementFilter tagName(final String tagName) {
        if (tagName.isEmpty() || tagName.equals("*")) return any();
        return new ElementFilter(tagName) {
            @Override
            public boolean matches(Element element) {
                return element.getName().matches(tagName);
            }
        };
    }

    public static ElementFilter tagName(final QualifiedName tagName) {
        return new ElementFilter(tagName.toString()) {
            @Override
            public boolean matches(Element element) {
                return tagName.matches(element.getName());
            }
        };
    }

    public static ElementFilter any() {
        return new ElementFilter("*") {
            @Override
            public boolean matches(Element element) {
                return true;
            }
        };
    }

}
