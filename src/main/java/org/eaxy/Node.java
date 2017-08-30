package org.eaxy;

import java.io.IOException;

public interface Node extends Content {

    CharSequence text();

    Node copy();

    void visit(XmlVisitor visitor) throws IOException;

}
