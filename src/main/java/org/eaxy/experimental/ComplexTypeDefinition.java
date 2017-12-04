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
        if (tns == null) {
        	return Namespace.NO_NAMESPACE;
        }
        for (Namespace namespace : schemaDoc.getRootElement().getNamespaces()) {
			if (namespace.getUri().equals(tns)) {
				return namespace;
			}
		}
        return new Namespace(tns, "a");
    }

    public Document getSchemaDoc() {
		return schemaDoc;
	}

}
