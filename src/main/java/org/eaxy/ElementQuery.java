package org.eaxy;

import java.io.Reader;
import java.net.URL;
import java.util.List;

public interface ElementQuery {

    ElementSet search(ElementSet elements);

    boolean matches(List<Element> path, int position);

    default Iterable<Element> iterate(Reader reader) {
        return XmlIterator.iterate(this, reader);
    }

    default Iterable<Element> iterate(URL url) {
        return XmlIterator.iterate(this, url);
    }

}
