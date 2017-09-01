package org.eaxy;

import java.io.IOException;
import java.io.Writer;

import org.eaxy.Xml.CommentElement;
import org.eaxy.Xml.TextElement;

public class IntentedWriterXmlVisitor extends WriterXmlVisitor implements XmlVisitor {

    private final String indentation;
    private String currentIndent = "";

    public IntentedWriterXmlVisitor(Writer writer, String indentation) {
        super(writer);
        this.indentation = indentation;
    }

    @Override
    public void visitElement(Element element) throws IOException {
        if (element.children().isEmpty()) {
            writer.write(currentIndent + "<" + element.printTag() + printNamespaces(element.getNamespaces()) + element.printAttributes() + " />" + Document.LINE_SEPARATOR);
        } else if (element.elements().isEmpty()) {
            writer.write(currentIndent + "<" + element.printTag() + printNamespaces(element.getNamespaces()) + element.printAttributes() + ">");
            visitChildren(element);
            writer.write("</" + element.printTag() + ">" + Document.LINE_SEPARATOR);
        } else {
            writer.write(currentIndent + "<" + element.printTag() + printNamespaces(element.getNamespaces()) + element.printAttributes() + ">" + Document.LINE_SEPARATOR);
            visitChildren(element);
            writer.write(currentIndent + "</" + element.printTag() + ">" + Document.LINE_SEPARATOR);
        }
    }

    @Override
    public void visitComment(CommentElement comment) throws IOException {
        writer.write(currentIndent + "<!--" + comment.text() + "-->" + Document.LINE_SEPARATOR);
    }

    @Override
    public void visitText(TextElement textElement) throws IOException {
        writer.write(textElement.text().trim().replaceAll("&", "&amp;")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;"));
    }

    private void visitChildren(Element element) throws IOException {
        printedNamespacesStack.push(element.getNamespaces());
        String oldIndent = currentIndent;
        currentIndent += indentation;
        for (Node child : element.children()) {
            child.visit(this);
        }
        currentIndent = oldIndent;
        printedNamespacesStack.pop();
    }
}
