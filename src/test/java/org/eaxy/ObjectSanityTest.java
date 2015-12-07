package org.eaxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eaxy.Namespace.NO_NAMESPACE;

import org.junit.Test;

public class ObjectSanityTest {

    private final Namespace NS = new Namespace("http://foo.com", "a1");
    private final Namespace NS_OTHER_PREFIX = new Namespace("http://foo.com", "a2");
    private final Namespace OTHER_NS = new Namespace("uri:somethingElse", "a2");

    @Test
    public void elementsShouldHaveDecentToString() {
        assertThat(NS.el("tag-name").toString()).contains("tag-name").contains(NS.prefix());
    }

    @Test
    public void namespaceShouldHaveDecentToString() {
        assertThat(NS.toString()).contains(NS.getPrefix()).contains(NS.getUri());
    }

    @Test
    public void attributesShouldHaveDecentToString() {
        assertThat(NS.attr("attr-name", "the value").toString())
            .contains("attr-name").contains(NS.getUri()).contains("the value");
    }

    @Test
    public void qualifiedNamesShouldEqualWhenNamespaceAndNameEquals() {
        assertThat(new QualifiedName(NS, "foo"))
            .isEqualTo(new QualifiedName(NS, "foo"))
            .isEqualTo(new QualifiedName(NS_OTHER_PREFIX, "foo"))
            .isNotEqualTo(new QualifiedName(OTHER_NS, "foo"))
            .isNotEqualTo(new QualifiedName("foo"))
            .isNotEqualTo(null);
    }

    @Test
    public void qualifiedNamesShouldHaveSameHashcodeWhenNamespaceAndNameEquals() {
        assertThat(new QualifiedName(NS, "foo").hashCode())
            .isEqualTo(new QualifiedName(NS, "foo").hashCode())
            .isEqualTo(new QualifiedName(NS_OTHER_PREFIX, "foo").hashCode())
            .isNotEqualTo(new QualifiedName(OTHER_NS, "foo").hashCode())
            .isNotEqualTo(new QualifiedName("foo").hashCode());
    }

    @Test
    public void qualifiedNameShouldShowCanonicName() {
        assertThat(NS.name("test").toString()).isEqualTo("\"http://foo.com\":test");
        assertThat(NO_NAMESPACE.name("localName").toString()).isEqualTo("localName");
    }

    @Test
    public void attributesShouldMatchWhenNameAndValueMatches() {
        assertThat(NS.attr("href", "something"))
            .isEqualTo(NS.attr("href", "something"))
            .isEqualTo(NS_OTHER_PREFIX.attr("href", "something"))
            .isNotEqualTo(OTHER_NS.attr("href", "something"))
            .isNotEqualTo(NS.attr("src", "something"))
            .isNotEqualTo(NS.attr("href", "something else"))
            .isNotEqualTo(null);
    }

    @Test
    public void attributesShouldHaveSameHashCodeWhenNameAndValueMatches() {
        assertThat(NS.attr("href", "something").hashCode())
            .isEqualTo(NS.attr("href", "something").hashCode())
            .isEqualTo(NS_OTHER_PREFIX.attr("href", "something").hashCode())
            .isNotEqualTo(OTHER_NS.attr("href", "something").hashCode())
            .isNotEqualTo(NS.attr("src", "something").hashCode())
            .isNotEqualTo(NS.attr("href", "something else").hashCode());
    }

}
