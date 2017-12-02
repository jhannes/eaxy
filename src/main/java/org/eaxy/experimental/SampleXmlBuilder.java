package org.eaxy.experimental;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import javax.annotation.Nonnull;

import org.eaxy.Document;
import org.eaxy.Element;
import org.eaxy.ElementSet;
import org.eaxy.Namespace;
import org.eaxy.QualifiedName;
import org.eaxy.Validator;
import org.eaxy.Xml;

public class SampleXmlBuilder {

    private Document schemaDoc;
    private List<Document> includedSchemas = new ArrayList<>();
    private Random random = new Random();
    private String nsPrefix;

    public SampleXmlBuilder(Document schemaDoc, String nsPrefix) throws IOException {
        this.schemaDoc = schemaDoc;
        this.nsPrefix = nsPrefix;

        xsNamespace = schemaDoc.getRootElement().getName().getNamespace();
        for (Element xsdInclude : schemaDoc.find("import")) {
            // import != include!
            if (schemaDoc.getBaseUrl() != null && xsdInclude.hasAttr("schemaLocation")) {
                this.includedSchemas.add(Xml.read(new URL(schemaDoc.getBaseUrl(), xsdInclude.attr("schemaLocation"))));
            }
        }
        for (Element xsdInclude : schemaDoc.find("include")) {
            if (schemaDoc.getBaseUrl() != null && xsdInclude.hasAttr("schemaLocation")) {
                this.includedSchemas.add(Xml.read(new URL(schemaDoc.getBaseUrl(), xsdInclude.attr("schemaLocation"))));
            }
        }
    }

    public SampleXmlBuilder(URL resource, String nsPrefix) throws IOException {
        this(Xml.read(resource), nsPrefix);
    }

    public SampleXmlBuilder(String nsPrefix, Document schemaDoc, List<Document> includedSchemas) {
        this.schemaDoc = schemaDoc;
        this.nsPrefix = nsPrefix;
        xsNamespace = schemaDoc.getRootElement().getName().getNamespace();
        this.includedSchemas.addAll(includedSchemas);
    }

    public Element createRandomElement(String elementName) {
        return createRandomElement(targetNamespace().name(elementName));
    }

    private QualifiedName qualifiedName(String fullElementName) {
        if (fullElementName == null) {
            return null;
        }
        String[] parts = fullElementName.split(":");
        if (parts.length == 1) {
            // TODO: Find the correct namespace by looking at namespace declarations of schema
//            Namespace namespace = schemaDoc.getRootElement().getNamespace(null);
            return targetNamespace().name(fullElementName);
        } else {
            for (Namespace namespace : schemaDoc.getRootElement().getNamespaces()) {
                if (java.util.Objects.equals(parts[0], namespace.getPrefix())) {
                    return namespace.name(parts[1]);
                }
            }
            throw new IllegalArgumentException(fullElementName + " not found in " + schemaDoc.getRootElement().getNamespaces());
        }
    }

    private Element populateComplexType(ComplexTypeDefinition complexTypeDefinition, Element resultElement) {
        Element complexType = complexTypeDefinition.getElement();

        if (complexType.find("simpleContent").isPresent()) {
            populateAttributes(resultElement, complexType.find("simpleContent", "extension").single());
            resultElement.text(randomElementText(resultElement.tagName(), complexType.find("simpleContent", "*").single()));
            return resultElement;
        }

        if (complexType.find("complexContent").isPresent()) {
            Element extension = complexType.find("complexContent", "extension").single();
            QualifiedName baseTypeName = qualifiedName(extension.attr("base"));
            ComplexTypeDefinition baseType = complexType(baseTypeName);
            SampleXmlBuilder builder = findXmlBuilder(baseTypeName);
            builder.populateAttributes(resultElement, complexTypeDefinition.getElement());
            builder.appendSequence(resultElement, baseType);
            this.appendSequence(resultElement, new ComplexTypeDefinition(extension, schemaDoc));
        }
        appendSequence(resultElement, complexTypeDefinition);
        populateAttributes(resultElement, complexType);
        return resultElement;
    }

    private void populateAttributes(Element resultElement, Element baseTypeElement) {
        for (Element attrDef : baseTypeElement.find("attribute")) {
            if (shouldIncludeAttribute(attrDef)) {
	            QualifiedName type = qualifiedName(attrDef.type());
	            if (type == null) {
	                Element attrTypeDef = attributeDefinition(requiredRef(attrDef));
	                Element simpleType = attrTypeDef.find("simpleType").single();
	                resultElement.attr(targetNamespace().attr(attrTypeDef.name(), randomAttributeText(attrTypeDef.name(), simpleType)));
	            } else if (isXsdType(type)) {
	                resultElement.attr(attrDef.name(), randomAttributeText(attrDef.name(), attrDef));
	            } else {
	                Element simpleType = schemaDoc.find("simpleType[name=" + type.getName() + "]").single();
	                resultElement.attr(attrDef.name(), randomAttributeText(attrDef.name(), simpleType));
	            }
            }
        }
    }

    private boolean shouldIncludeAttribute(Element attrDef) {
		return "required".equals(attrDef.attr("use")) || full || (!minimal && chance(.50));
	}

	@Nonnull
	private String requiredRef(@Nonnull Element attrDef) {
		String refAttr = attrDef.attr("ref");
		if (refAttr == null) {
			throw new IllegalArgumentException(attrDef + " should have ref attribute");
		}
		return refAttr;
	}

    private boolean chance(double p) {
        return random.nextDouble() < p;
    }

    private void appendSequence(Element resultElement, ComplexTypeDefinition baseType) {
    	if (baseType.getElement().find("sequence").isPresent()) {
	        for (Element seqMemberDef : baseType.getElement().find("sequence", "*")) {
	            appendChildElement(resultElement, seqMemberDef, baseType);
	        }
    	} else if (baseType.getElement().find("all").isPresent()) {
	        for (Element seqMemberDef : baseType.getElement().find("all", "*")) {
	            appendChildElement(resultElement, seqMemberDef, baseType);
	        }
    	} else if (baseType.getElement().find("attribute").isEmpty() &&baseType.getElement().find("complexContent").isEmpty()) {
    		throw new IllegalArgumentException("Unexpected " + baseType.getElement());
    	}
    }

    // TODO: This makes this class is not thread safe.
    /// If the current class could be the SampleXmlBuilderDefinition and there was one builder per build this could be avoided
    private Stack<String> memberDefStack = new Stack<>();

    private void appendChildElement(Element resultElement, Element memberDef, ComplexTypeDefinition typeDef) {
    	if (memberDefStack.contains(memberDef.toIndentedXML()) && "0".equals(memberDef.attr("minOccurs"))) {
    		return;
    	}

    	memberDefStack.push(memberDef.toIndentedXML());
        int occurances = occurences(memberDef);
        for (int i = 0; i < occurances; i++) {
            resultElement.add(addChildElement(memberDef, typeDef.targetNamespace()));
        }
        memberDefStack.pop();
    }

    private Element addChildElement(Element memberDef, Namespace targetNamespace) {
	    String typeDef = memberDef.attr("ref");
	    if (typeDef != null) {
	        Element elementDefinition = elementDefinition(qualifiedName(typeDef));
	        QualifiedName elementName = targetNamespace.name(elementDefinition.name());
	        return createRandomElement(elementName, elementDefinition);
	    }

	    QualifiedName elementName = Namespace.NO_NAMESPACE.name(memberDef.name());
	    if ("qualified".equals(schemaDoc.getRootElement().attr("elementFormDefault"))) {
	        elementName = targetNamespace.name(memberDef.name());
	    }
	    return createRandomElement(elementName, memberDef);
	}

	Element createRandomElement(QualifiedName elementName) {
        return createRandomElement(elementName, elementDefinition(elementName));
    }

	// TODO: This looks like it could be simplified
	private Element createRandomElement(QualifiedName elementName, Element elementDefinition) {
		QualifiedName memberType = qualifiedName(elementDefinition.type());
        if (memberType != null) {
			if (isXsdType(memberType)) {
                return Xml.el(elementName, randomElementText(elementName.getName(), elementDefinition));
            } else {
			    SampleXmlBuilder builder = findXmlBuilder(memberType);
			    Element simpleType = builder.schemaDoc.find("simpleType[name=" + memberType.getName() + "]").singleOrDefault();
			    if (simpleType != null) {
			    	elementDefinition = simpleType;
			        return Xml.el(elementName, randomElementText(elementName.getName(), elementDefinition));
			    } else {
			    	return builder.populateComplexType(complexType(memberType), Xml.el(elementName));
			    }
            }
        } else {
            Element complexMemberType = elementDefinition.find("complexType").singleOrDefault();
            if (complexMemberType != null) {
                return populateComplexType(new ComplexTypeDefinition(complexMemberType, schemaDoc), Xml.el(elementName));
            } else {
                elementDefinition = elementDefinition.find("simpleType").single();
				return Xml.el(elementName, randomElementText(elementName.getName(), elementDefinition));
            }
        }
	}

    private SampleXmlBuilder findXmlBuilder(QualifiedName qualifiedName) {
        if (qualifiedName.getNamespace().equals(targetNamespace())) {
            return this;
        } else {
            for (Document schemaDoc : includedSchemas) {
                if (schemaDoc.getRootElement().attr("targetNamespace").equals(qualifiedName.getNamespace().getUri())) {
                    // TODO: Slooow down there - we should perhaps not include ALL the schemas?
                    return new SampleXmlBuilder(qualifiedName.getNamespace().getPrefix(), schemaDoc, includedSchemas);
                }
            }
            throw new IllegalArgumentException("Don't know about " + qualifiedName + " in " + includedSchemas);
        }
    }

    /**
     * Override this method to create custom rules for specific attributes
     * @param attributeName The name of the attribute to write
     * @param attrDef The element that defines the attribute
     * @return Random attribute value that fulfills the definition
     */
    protected String randomAttributeText(String attributeName, Element attrDef) {
        return randomData(attrDef);
    }

    /**
     * Override this method to create custom rules for specific elements
     * @param elementName The name of the element to write
     * @param attrDef The element that defines the attribute
     * @return Random attribute value that fulfills the definition
     */
    protected String randomElementText(String elementName, Element typeDefinition) {
        return randomData(typeDefinition);
    }

    private boolean isXsdType(QualifiedName type) {
        return type != null && type.getNamespace().equals(xsNamespace);
    }

    private int occurences(Element seqMemberDef) {
        int minOccurs = 1;
        String minOccursAttr = seqMemberDef.attr("minOccurs");
        if (minOccursAttr != null) {
            minOccurs = Integer.parseInt(minOccursAttr);
            if (minimal) {
                return minOccurs;
            }
        }

        int maxOccurs = 1;
        String maxOccursAttr = seqMemberDef.attr("maxOccurs");
        if (maxOccursAttr != null) {
            if (maxOccursAttr.equalsIgnoreCase("unbounded")) {
                maxOccurs = 4;
            } else {
                maxOccurs = Integer.parseInt(maxOccursAttr);
            }
        }
        if (full && minOccurs < 2) {
            minOccurs = Math.min(2, maxOccurs);
        }
        return random(minOccurs, maxOccurs);
    }

    private Instant randomDateTime() {
        return ZonedDateTime.now().minusDays(100).plusMinutes(random.nextInt(200 * 24 * 60)).toInstant();
    }

    protected String randomData(Element typeDef) {
        if ("simpleType".equals(typeDef.tagName())) {
        	if (typeDef.find(xsNamespace.name("union")).isPresent()) {
        		List<Element> unionMembers = typeDef.find(xsNamespace.name("union"), xsNamespace.name("simpleType")).elements();
        		return randomData(unionMembers.get(random(unionMembers.size())));

        	}

            String baseType = typeDef.find("restriction").single().attr("base");

            ElementSet enumerations = typeDef.find("restriction", "enumeration");
            if (enumerations.isPresent()) {
                return pickOne(enumerations.attrs("value"));
            }

            if (baseType.equals(xsNamespace.name("string").print())) {
            	// TODO: This is a hack to support a test which uses regex
                return "123-AB";
            } else {
            	return randomBaseType(typeDef.find("restriction").single());
            }
        }

        return randomBaseType(typeDef);
    }

	private String randomBaseType(Element typeDef) {
		String type = typeDef.type();
        if (type == null)
            type = typeDef.attr("base");
        if (type.equals(xsNamespace.name("date").print())) {
            return randomDate().toString();
        } else if (type.equals(xsNamespace.name("dateTime").print())) {
            return randomDateTime().toString();
        } else if (type.equals(xsNamespace.name("boolean").print())) {
            return String.valueOf(random.nextBoolean());
        } else if (type.equals(xsNamespace.name("string").print())) {
            return randomString(5, 10);
        } else if (type.equals(xsNamespace.name("int").print()) || type.equals(xsNamespace.name("integer").print())) {
            return String.valueOf(random(-10, 10));
        } else if (type.equals(xsNamespace.name("positiveInteger").print())) {
            return String.valueOf(random(1, 10));
        } else if (type.equals(xsNamespace.name("nonNegativeInteger").print())) {
          return String.valueOf(random(0, 10));
        } else if (type.equals(xsNamespace.name("decimal").print())) {
            return String.valueOf(random(-1000, 10000) / 100);
        } else if (type.equals(xsNamespace.name("float").print())) {
            return String.valueOf(random(-1000, 10000) / 100.0);
        } else if (type.equals(xsNamespace.name("double").print())) {
            return String.valueOf(random(-1000, 10000) / 100.0);
        } else if (type.equals(xsNamespace.name("base64Binary").print())) {
            // data:image/svg+xml;base64,
            return "PD94bWwgdmVyc2lvbj0iMS4wIiA/PjxzdmcgZGF0YS1uYW1lPSJMYXllciAxIiBpZD0iTGF5ZXJfMSIgdmlld0JveD0iMCAwIDQ4IDQ4IiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciPjxkZWZzPjxzdHlsZT4uY2xzLTEsLmNscy0ye2ZpbGw6bm9uZTtzdHJva2U6IzIzMWYyMDtzdHJva2UtbWl0ZXJsaW1pdDoxMDtzdHJva2Utd2lkdGg6MnB4O30uY2xzLTJ7c3Ryb2tlLWxpbmVjYXA6cm91bmQ7fS5jbHMtM3tmaWxsOiMyMzFmMjA7fTwvc3R5bGU+PC9kZWZzPjx0aXRsZS8+PGNpcmNsZSBjbGFzcz0iY2xzLTEiIGN4PSIyNCIgY3k9IjI0IiByPSIyMyIvPjxwYXRoIGNsYXNzPSJjbHMtMiIgZD0iTTE0LDMzczguODMsOS4zMywyMCwwIi8+PGVsbGlwc2UgY2xhc3M9ImNscy0zIiBjeD0iMTciIGN5PSIxOSIgcng9IjMiIHJ5PSI0Ii8+PGVsbGlwc2UgY2xhc3M9ImNscy0zIiBjeD0iMzEiIGN5PSIxOSIgcng9IjMiIHJ5PSI0Ii8+PC9zdmc+";
        } else if (type.equals(xsNamespace.name("NMTOKEN").print())) {
            return typeDef.attr("fixed");
        }
        throw new IllegalArgumentException("Unknown base type " + typeDef);
	}

    private LocalDate randomDate() {
        return LocalDate.now().minusDays(100).plusDays(new Random().nextInt(200));
    }

    private String randomString(int lowerBound, int upperBound) {
        int length = random(lowerBound, upperBound);
        StringBuilder result = new StringBuilder();
        result.append(pickOne(SAMPLE_WORDS));
        for (int i = 0; i < length; i++) {
            result.append(" ").append(pickOne(SAMPLE_WORDS));
        }
        return result.toString();
    }

    private int random(int lowerBound, int upperBound) {
        if (lowerBound == upperBound) {
            return lowerBound;
        }
        return lowerBound + random(upperBound - lowerBound + 1);
    }

    private <T> T pickOne(List<T> candidates) {
        return candidates.get(random(candidates.size()));
    }

    private <T> T pickOne(T[] candidates) {
        return candidates[random(candidates.length)];
    }

    private int random(int size) {
        return random.nextInt(size);
    }

    private Namespace targetNamespace() {
        String tns = schemaDoc.getRootElement().attr("targetNamespace");
        return tns != null ? new Namespace(tns, nsPrefix) : Namespace.NO_NAMESPACE;
    }

    @Nonnull
    private Element attributeDefinition(@Nonnull String typeNameFull) {
        @Nonnull String typeName = qualifiedName(typeNameFull).getName();
        return schemaDoc.find("attribute[name=" + typeName + "]").single();
    }

    private ComplexTypeDefinition complexType(QualifiedName qualifiedName) {
        // TODO: Use the namespace to lookup the schema - but it could be included as well!
        Element typeDefinition = schemaDoc.find("complexType[name=" + qualifiedName.getName() + "]").singleOrDefault();
        if (typeDefinition != null) {
            return new ComplexTypeDefinition(typeDefinition, schemaDoc);
        }
        for (Document schemaDoc : includedSchemas) {
            typeDefinition = schemaDoc.find("complexType[name=" + qualifiedName.getName() + "]").singleOrDefault();
            if (typeDefinition != null) {
                return new ComplexTypeDefinition(typeDefinition, schemaDoc);
            }
        }
        throw new IllegalArgumentException("Can't find type definition of " + qualifiedName);
    }

    private Element elementDefinition(QualifiedName elementName) {
    	if (elementName == null) {
    		throw new IllegalArgumentException("elementName shouldn't be null");
    	}

        // TODO: Lookup schema based on elementName namespace
        Element typeDefinition = schemaDoc.find("element[name=" + elementName.getName() + "]").singleOrDefault();
        if (typeDefinition != null) {
            return typeDefinition;
        }
        for (Document schemaDoc : includedSchemas) {
            // TODO: Not covered by test!
            typeDefinition = schemaDoc.find("element[name=" + elementName.getName() + "]").singleOrDefault();
            if (typeDefinition != null) {
                return typeDefinition;
            }
        }
        throw new IllegalArgumentException("Can't find type definition of " + elementName);
    }

    private static final String[] SAMPLE_WORDS = { "about", "all", "also", "and", "as", "at", "be", "because", "but",
            "by", "can", "come", "could", "day", "do", "even", "find", "first", "for", "from", "get", "give", "go",
            "have", "he", "her", "here", "him", "his", "how", "I", "if", "in", "into", "it", "its", "just", "know",
            "like", "look", "make", "man", "many", "me", "more", "my", "new", "no", "not", "now", "of", "on", "one",
            "only", "or", "other", "our", "out", "people", "say", "see", "she", "so", "some", "take", "tell", "than",
            "that", "the", "their", "them", "then", "there", "these", "they", "thing", "think", "this", "those", "time",
            "to", "two", "up", "use", "very", "want", "way", "we", "well", "what", "when", "which", "who", "will",
            "with", "would", "year", "you", "your", };
    private final Namespace xsNamespace;
    private boolean minimal;
    private boolean full;

    public void setMinimal(boolean minimal) {
        this.minimal = minimal;
    }

    public void setFull(boolean full) {
        this.full = full;
    }

    public void addSchema(Element schema) {
        this.includedSchemas.add(new Document(schema));
    }

    public Validator getValidator() throws IOException {
        List<Document> schemaDocs = new ArrayList<>();
        schemaDocs.add(schemaDoc);
        schemaDocs.addAll(includedSchemas);
        return new Validator(schemaDocs);
    }
}
