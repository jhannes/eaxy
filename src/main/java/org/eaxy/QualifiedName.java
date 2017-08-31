package org.eaxy;


public class QualifiedName {

    private final Namespace namespace;
    private final String name;


    public QualifiedName(String uri, String localPart, String prefix) {
        Objects.validatePresent(localPart, "localPart");
        if (uri == null || uri.isEmpty()) {
            this.namespace = Namespace.NO_NAMESPACE;
            if (prefix != null && !prefix.isEmpty()) {
                throw new IllegalArgumentException(prefix);
            }
            this.name = localPart;
        } else {
            this.namespace = new Namespace(uri, prefix);
            this.name = localPart;
        }
    }

    public QualifiedName(String uri, String fullyQualifiedName) {
        Objects.validatePresent(fullyQualifiedName, "name");
        if (uri == null || uri.isEmpty()) {
            this.namespace = Namespace.NO_NAMESPACE;
            if (fullyQualifiedName.contains(":")) {
                throw new IllegalArgumentException(fullyQualifiedName);
            }
            this.name = fullyQualifiedName;
        } else {
            int colonPos = fullyQualifiedName.indexOf(":");
            if (colonPos == -1) {
                this.namespace = new Namespace(uri);
                this.name = fullyQualifiedName;
            } else {
                this.namespace = new Namespace(uri, fullyQualifiedName.substring(0, colonPos));
                this.name = fullyQualifiedName.substring(colonPos+1);
            }
        }
    }

    public QualifiedName(String name) {
        this(Namespace.NO_NAMESPACE, name);
    }

    public QualifiedName(Namespace namespace, String name) {
        this.namespace = namespace;
        this.name = Objects.validatePresent(name, "name");
        if (!namespace.isNamespace() && name.contains(":")) {
            throw new IllegalArgumentException(name);
        }
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
        if (!namespace.isNamespace()) return name;
        return "\"" + namespace.getUri() + "\":" + name;
    }

    public boolean hasNamespace() {
        return namespace.isNamespace();
    }

    public boolean matches(QualifiedName filter) {
        if (!filter.hasNamespace() || !hasNamespace()) {
            return filter.name.equals(this.name);
        }
        return filter.equals(this);
    }

    public boolean matches(String tagName) {
        return name.equals(tagName);
    }

}
