package org.eaxy;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;

public interface Node extends Content {

    void writeTo(Writer writer, LinkedList<Namespace> printedNamespaces) throws IOException;

    CharSequence text();

    Node copy();

}
