package org.eaxy;

class Attribute implements Content {

    private final QualifiedName key;
    private final String value;

    Attribute(QualifiedName key, String value) {
        this.key = key;
        this.value = value;
    }

    public QualifiedName getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Attribute)) return false;
        Attribute other = ((Attribute) obj);
        return Objects.equals(key, other.key) &&
                Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(key, value);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{key=" + key + ",value=" + value + "}";
    }

    public String toXML() {
        return key.print() + (value != null ? ("=\"" + valueToXML() + "\"") : "");
    }

    private String valueToXML() {
        return value.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quot;");
    }

}
