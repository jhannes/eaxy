package org.eaxy;

import java.io.Reader;
import java.net.URL;
import java.util.List;

public interface ElementQuery {

    ElementSet search(ElementSet elements);

    boolean matches(List<Element> path, int position);

    Iterable<Element> iterate(Reader reader);

    Iterable<Element> iterate(URL url);


}
