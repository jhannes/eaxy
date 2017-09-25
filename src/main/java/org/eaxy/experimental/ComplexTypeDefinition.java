package org.eaxy.experimental;

import org.eaxy.Document;
import org.eaxy.Element;
import org.eaxy.Namespace;

public class ComplexTypeDefinition {

    private Element typeDefinition;
    private Document schemaDoc;

    public ComplexTypeDefinition(Element typeDefinition, Document schemaDoc) {
        this.typeDefinition = typeDefinition;
        this.schemaDoc = schemaDoc;
    }

    public Element getElement() {
        return typeDefinition;
    }

    public Namespace targetNamespace() {
        String tns = schemaDoc.getRootElement().attr("targetNamespace");
        return tns != null ? new Namespace(tns, "a") : Namespace.NO_NAMESPACE;
    }

}
