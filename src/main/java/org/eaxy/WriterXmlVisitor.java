package org.eaxy;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Stack;

import org.eaxy.Xml.CDataElement;
import org.eaxy.Xml.CommentElement;
import org.eaxy.Xml.TextElement;

public class WriterXmlVisitor implements XmlVisitor {

    protected final Writer writer;
    protected final Stack<Collection<Namespace>> printedNamespacesStack = new Stack<>();

    public WriterXmlVisitor(Writer writer) {
        this.writer = writer;
    }

    @Override
    public void visitDocument(Document document) throws IOException {
        document.writeHeader(writer);
        document.getRootElement().visit(this);
    }

    @Override
    public void visitCdata(CDataElement cDataElement) throws IOException {
        writer.write("<![CDATA[" + cDataElement.text() + "]]>");
    }

    @Override
    public void visitComment(CommentElement comment) throws IOException {
        writer.write("<!--" + comment.text() + "-->");
    }

    @Override
    public void visitText(TextElement textElement) throws IOException {
        writer.write(textElement.text().replaceAll("&", "&amp;")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;"));
    }

    @Override
    public void visitElement(Element element) throws IOException {
        if (element.children().isEmpty()) {
            writer.write("<" + element.printTag() + printNamespaces(element.getNamespaces()) + element.printAttributes() + " />");
        } else {
            writer.write("<" + element.printTag() + printNamespaces(element.getNamespaces()) + element.printAttributes() + ">");
            printedNamespacesStack.push(element.getNamespaces());
            for (Node child : element.children()) {
                child.visit(this);
            }
            printedNamespacesStack.pop();
            writer.write("</" + element.printTag() + ">");
        }
    }

    protected String printNamespaces(Collection<Namespace> namespaces) {
        StringBuilder result = new StringBuilder();
        for (Namespace namespace : namespaces) {
            if (!namespace.isNamespace()) throw new IllegalStateException("Can't print " + namespace.toString());
            if (isNamespaceAlreadyPrinted(namespace)) continue;
            result.append(" ").append(namespace.print());
        }
        return result.toString();
    }

    private boolean isNamespaceAlreadyPrinted(Namespace namespace) {
        for (Collection<Namespace> linkedList : printedNamespacesStack) {
            if (linkedList.contains(namespace)) {
                return true;
            }
        }
        return false;
    }

}
