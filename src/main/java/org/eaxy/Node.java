package org.eaxy;

import java.util.LinkedList;

public interface Node {

    CharSequence print(LinkedList<Namespace> printedNamespaces);

    CharSequence text();

}
