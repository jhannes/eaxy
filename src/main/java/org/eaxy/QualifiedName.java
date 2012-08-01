package org.eaxy;


class QualifiedName {

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
        if (namespace == Namespace.NO_NAMESPACE) return Objects.equals(name, other.name);
        return Objects.equals(name, other.name) &&
                Objects.equals(namespace, other.namespace);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, namespace);
    }

    @Override
    public String toString() {
        if (namespace == Namespace.NO_NAMESPACE) return name;
        return "{" + namespace.getUri() + "}" + name;
    }

    public boolean matches(Object filter) {
        if (filter instanceof QualifiedName) {
            return filter.equals(this);
        }
        return this.name.equals(filter);
    }
}
