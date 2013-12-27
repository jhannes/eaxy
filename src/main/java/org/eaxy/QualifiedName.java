package org.eaxy;


public class QualifiedName {

    private final Namespace namespace;
    private final String name;

    public QualifiedName(Namespace namespace, String name) {
        this.namespace = namespace;
        this.name = name;
    }

    public QualifiedName(String name) {
        if (name == null || name.equals("")) {
            throw new IllegalArgumentException("name must be provided");
        }
        this.namespace = Namespace.NO_NAMESPACE;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Namespace getNamespace() {
        return namespace;
    }

    public String print() {
        return namespace.prefix() + name;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof QualifiedName)) return false;
        QualifiedName other = ((QualifiedName) obj);
        return Objects.equals(name, other.name) &&
                Objects.equals(namespace, other.namespace);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, namespace);
    }

    @Override
    public String toString() {
        if (namespace.isNoNamespace()) return name;
        return "{" + namespace.getUri() + "}" + name;
    }

    public boolean hasNoNamespace() {
        return namespace.isNoNamespace();
    }

    public boolean matches(QualifiedName filter) {
        if (filter.hasNoNamespace() || hasNoNamespace()) {
            return filter.name.equals(this.name);
        }
        return filter.equals(this);
    }

    public boolean matches(String tagName) {
        return name.equals(tagName);
    }
}
