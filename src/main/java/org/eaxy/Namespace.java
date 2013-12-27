package org.eaxy;


public class Namespace implements Content {

    static final Namespace NO_NAMESPACE = new Namespace(null) {
        @Override
        public boolean isNoNamespace() { return true; }
    };

    private final String uri;
    private final String prefix;

    public Namespace(String uri, String prefix) {
        this.uri = uri;
        this.prefix = prefix;
    }

    public Namespace(String uri) {
        this.uri = uri;
        this.prefix = null;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Namespace)) return false;
        Namespace other = ((Namespace) obj);
        return Objects.equals(uri, other.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uri);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{uri=" + uri + ",prefix=" + prefix + "}";
    }

    public Element el(String tagName, Content... contents) {
        return new Element(name(tagName), contents);
    }

    public Element el(String tagName, String stringContent) {
        return el(tagName, Xml.text(stringContent));
    }

    public boolean isNoNamespace() {
        return false;
    }

    public String print() {
        return "xmlns" + (prefix == null ? "" : ":" + prefix) + "=\"" + uri + "\"";
    }

    public String prefix() {
        return prefix == null ? "" : prefix + ":";
    }

    public String getUri() {
        return uri;
    }

    public String getPrefix() {
        return prefix;
    }

    public QualifiedName name(String name) {
        return new QualifiedName(this, name);
    }

    public Attribute attr(String localName, String value) {
        return new Attribute(name(localName), value);
    }

}
