package org.eaxy.experimental;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;

import org.eaxy.CanNeverHappenException;
import org.eaxy.Element;
import org.eaxy.ElementPath;
import org.eaxy.Namespace;
import org.eaxy.Node;
import org.eaxy.WriterXmlVisitor;

public interface XmlFormatter {

    static class CanonicalInclusiveVisitor extends WriterXmlVisitor {

        private Collection<Namespace> rootNamespaces;

        public CanonicalInclusiveVisitor(Writer writer, Collection<Namespace> rootNamespaces) {
            super(writer);
            this.rootNamespaces = rootNamespaces;
        }

        @Override
        public void visitElement(Element element) throws IOException {
            writer.write("<" + element.printTag() + printNamespaces(element.getNamespaces()) + element.printAttributes() + ">");
            printedNamespacesStack.push(element.getNamespaces());
            for (Node child : element.children()) {
                child.visit(this);
            }
            printedNamespacesStack.pop();
            writer.write("</" + element.printTag() + ">");
        }

        public void write(ElementPath elementPath) throws IOException {
            Element element = elementPath.leafElement();

            writer.write("<" + element.printTag() + printNamespaces(rootNamespaces) + element.printAttributes() + ">");
            printedNamespacesStack.push(rootNamespaces);
            for (Node child : element.children()) {
                child.visit(this);
            }
            printedNamespacesStack.pop();
            writer.write("</" + element.printTag() + ">");
        }
    }

    static class CanonicalInclusive implements XmlFormatter {
        private static String NAME = "http://www.w3.org/TR/2001/REC-xml-c14n-20010315";

        @Override
        public void format(Writer writer, ElementPath elementPath) throws IOException {
            new CanonicalInclusiveVisitor(writer, elementPath.namespaces()).write(elementPath);
        }
    }

    static XmlFormatter canonical(String algorithm) {
        if (algorithm.equals(CanonicalInclusive.NAME)) {
            return new CanonicalInclusive();
        } else {
            throw new IllegalArgumentException(algorithm + " not implemented. Pull requests are welcome");
        }
    }

    default String toXML(ElementPath elementPath) {
        try {
            StringWriter writer = new StringWriter();
            format(writer, elementPath);
            return writer.toString();
        } catch (IOException e) {
            throw new CanNeverHappenException("StringWriter doesn't throw IOException", e);
        }
    }

    void format(Writer writer, ElementPath elementPath) throws IOException;

}
