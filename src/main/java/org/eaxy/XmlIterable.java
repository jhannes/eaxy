package org.eaxy;

public interface XmlIterable extends Iterable<Element> {

    @Override
    XmlIterator iterator();
}
