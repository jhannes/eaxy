package org.eaxy;

import java.io.IOException;

import org.eaxy.Xml.CDataElement;
import org.eaxy.Xml.CommentElement;
import org.eaxy.Xml.TextElement;

public interface XmlVisitor {

    void visitDocument(Document document) throws IOException;

    void visitElement(Element element) throws IOException;

    void visitText(TextElement textElement) throws IOException;

    void visitComment(CommentElement comment) throws IOException;

    void visitCdata(CDataElement cDataElement) throws IOException;
}
