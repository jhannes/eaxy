package org.eaxy;


public class Namespace implements Content {

    public static final Namespace NO_NAMESPACE = new Namespace(null) {
        @Override
        public boolean isNamespace() { return false; }
    };

    private final String uri;
    private final String prefix;

    public Namespace(String uri, String prefix) {
        this.uri = Objects.validatePresent(uri, "uri");
        this.prefix = "".equals(prefix) ? null : prefix;

        if (prefix != null && (prefix.equals("xmlns") || prefix.startsWith("xmlns:"))) {
            throw new IllegalArgumentException("Namespace declarations can't be prefixes: " + prefix);
        }
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

    public boolean isNamespace() {
        return true;
    }

    public String print() {
        return xmlns() + "=\"" + uri + "\"";
    }

    String xmlns() {
        return "xmlns" + (prefix == null ? "" : ":" + prefix);
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
