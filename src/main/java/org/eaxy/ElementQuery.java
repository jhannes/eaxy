package org.eaxy;

import java.util.List;

public interface ElementQuery {

    ElementSet search(ElementSet elements);

    boolean matches(List<Element> path, int position);

}
