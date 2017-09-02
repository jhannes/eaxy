package org.eaxy.experimental;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.eaxy.Document;
import org.eaxy.Element;
import org.eaxy.ElementSet;
import org.eaxy.Namespace;
import org.eaxy.QualifiedName;
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
        for (Element xsdInclude : schemaDoc.find("include")) {
            this.includedSchemas.add(Xml.read(new URL(schemaDoc.getBaseUrl(), xsdInclude.attr("schemaLocation"))));
        }
    }

    public SampleXmlBuilder(URL resource, String nsPrefix) throws IOException {
        this(Xml.read(resource), nsPrefix);
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
            Namespace namespace = schemaDoc.getRootElement().getNamespace(parts[0]);
            return namespace.name(parts[1]);
        }
    }

    private Element populateComplexType(Element complexType, Element resultElement) {
        if (complexType.find("complexContent").isPresent()) {
            Element extension = complexType.find("complexContent", "extension").single();
            Element baseType = complexType(extension.attr("base"));
            populateAttributes(resultElement, baseType);
            appendSequence(resultElement, baseType);
            appendSequence(resultElement, extension);
        }
        appendSequence(resultElement, complexType);
        populateAttributes(resultElement, complexType);
        return resultElement;
    }

    private void populateAttributes(Element resultElement, Element complexType) {
        for (Element attrDef : complexType.find("attribute")) {
            if (!"required".equals(attrDef.attr("use")) && minimal) {
                continue;
            } else if (!"required".equals(attrDef.attr("use")) && !full && chance(.50)) {
                continue;
            }
            QualifiedName type = qualifiedName(attrDef.type());
            if (type == null) {
                Element attrTypeDef = attributeDefinition(attrDef.attr("ref"));
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

    private boolean chance(double p) {
        return random.nextDouble() < p;
    }

    private void appendSequence(Element resultElement, Element complexType) {
        for (Element seqMemberDef : complexType.find("sequence", "*")) {
            appendChildElement(resultElement, seqMemberDef);
        }
        for (Element seqMemberDef : complexType.find("all", "*")) {
            appendChildElement(resultElement, seqMemberDef);
        }
    }

    private void appendChildElement(Element resultElement, Element memberDef) {
        int occurances = occurences(memberDef);
        for (int i = 0; i < occurances; i++) {
            resultElement.add(addChildElement(memberDef));
        }
    }

    Element createRandomElement(QualifiedName elementName) {
        Element elementDefinition = elementDefinition(elementName);
        if (elementDefinition.type() != null) {
            if (isXsdType(qualifiedName(elementDefinition.type()))) {
                return Xml.el(elementName,
                    randomElementText(elementName.getName(), elementDefinition));
            } else {
                return createComplexType(elementName, complexType(elementDefinition.type()));
            }
        } else {
            return createComplexType(elementName, elementDefinition.find("complexType").single());
        }
    }

    private Element createComplexType(QualifiedName elementName, Element complexType) {
        return populateComplexType(complexType, Xml.el(elementName));
    }

    private Element addChildElement(Element memberDef) {
        String typeDef = memberDef.attr("ref");
        if (typeDef != null) {
            Element elementDef = elementDefinition(qualifiedName(typeDef));
            QualifiedName memberType = qualifiedName(elementDef.type());
            if (isXsdType(memberType)) {
                return targetNamespace().el(elementDef.name(),
                        randomElementText(elementDef.text(), elementDef));
            } else {
                return createRandomElement(memberType);
            }
        } else if (memberDef.type() == null) {
            Element complexMemberType = memberDef.find("complexType").singleOrDefault();
            if (complexMemberType != null) {
                return createComplexType(Namespace.NO_NAMESPACE.name(memberDef.name()), complexMemberType);
            } else {
                // TODO: This looks wrong
                Element simpleMemberType = memberDef.find("simpleType", "restriction").single();
                return Xml.el(Namespace.NO_NAMESPACE.name(memberDef.name()), randomElementText(memberDef.name(), simpleMemberType));
            }
        } else if (isXsdType(qualifiedName(memberDef.type()))) {
            return Xml.el(Namespace.NO_NAMESPACE.name(memberDef.name()), randomElementText(memberDef.name(), memberDef));
        } else {
            Element simpleType = schemaDoc.find("simpleType[name=" + qualifiedName(memberDef.type()).getName() + "]").singleOrDefault();
            if (simpleType != null) {
                return Xml.el(Namespace.NO_NAMESPACE.name(memberDef.name()), randomElementText(memberDef.name(), simpleType));
            } else {
                return createComplexType(Namespace.NO_NAMESPACE.name(memberDef.name()),
                    complexType(memberDef.type()));
            }
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
                maxOccurs = 10;
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
            String baseType = typeDef.find("restriction").single().attr("base");

            ElementSet enumerations = typeDef.find("restriction", "enumeration");
            if (enumerations.isPresent()) {
                return pickOne(enumerations.attrs("value"));
            }

            if (baseType.matches(xsNamespace.name("string").print())) {
                return "123-AB";
            } else {
                throw new RuntimeException("Don't know what to do with " + baseType);
            }
        }

        String type = typeDef.type();
        if (type == null)
            type = typeDef.attr("base");
        if (type.matches(xsNamespace.name("date").print())) {
            return randomDate().toString();
        } else if (type.matches(xsNamespace.name("dateTime").print())) {
            return randomDateTime().toString();
        } else if (type.matches(xsNamespace.name("boolean").print())) {
            return String.valueOf(random.nextBoolean());
        } else if (type.matches(xsNamespace.name("string").print())) {
            return randomString(10, 20);
        } else if (type.matches(xsNamespace.name("int").print())) {
            return String.valueOf(random(-10, 10));
        } else if (type.matches(xsNamespace.name("positiveInteger").print())) {
            return String.valueOf(random(1, 10));
        } else if (type.matches(xsNamespace.name("decimal").print())) {
            return String.valueOf(random(-1000, 10000) / 100);
        } else if (type.matches(xsNamespace.name("float").print())) {
            return String.valueOf(random(-1000, 10000) / 100.0);
        } else if (type.matches(xsNamespace.name("double").print())) {
            return String.valueOf(random(-1000, 10000) / 100.0);
        } else if (type.matches(xsNamespace.name("base64Binary").print())) {
            // data:image/svg+xml;base64,
            return "PD94bWwgdmVyc2lvbj0iMS4wIiA/PjxzdmcgZGF0YS1uYW1lPSJMYXllciAxIiBpZD0iTGF5ZXJfMSIgdmlld0JveD0iMCAwIDQ4IDQ4IiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciPjxkZWZzPjxzdHlsZT4uY2xzLTEsLmNscy0ye2ZpbGw6bm9uZTtzdHJva2U6IzIzMWYyMDtzdHJva2UtbWl0ZXJsaW1pdDoxMDtzdHJva2Utd2lkdGg6MnB4O30uY2xzLTJ7c3Ryb2tlLWxpbmVjYXA6cm91bmQ7fS5jbHMtM3tmaWxsOiMyMzFmMjA7fTwvc3R5bGU+PC9kZWZzPjx0aXRsZS8+PGNpcmNsZSBjbGFzcz0iY2xzLTEiIGN4PSIyNCIgY3k9IjI0IiByPSIyMyIvPjxwYXRoIGNsYXNzPSJjbHMtMiIgZD0iTTE0LDMzczguODMsOS4zMywyMCwwIi8+PGVsbGlwc2UgY2xhc3M9ImNscy0zIiBjeD0iMTciIGN5PSIxOSIgcng9IjMiIHJ5PSI0Ii8+PGVsbGlwc2UgY2xhc3M9ImNscy0zIiBjeD0iMzEiIGN5PSIxOSIgcng9IjMiIHJ5PSI0Ii8+PC9zdmc+";
        } else if (type.matches(xsNamespace.name("NMTOKEN").print())) {
            return typeDef.attr("fixed");
        }
        throw new IllegalArgumentException("Unknown base type " + type);
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
        return lowerBound + random(upperBound - lowerBound);
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

    private Element attributeDefinition(String typeNameFull) {
        String typeName = qualifiedName(typeNameFull).getName();
        return schemaDoc.find("attribute[name=" + typeName + "]").single();
    }

    private Element complexType(String typeNameFull) {
        QualifiedName qualifiedName = qualifiedName(typeNameFull);
        Element typeDefinition = schemaDoc.find("complexType[name=" + qualifiedName.getName() + "]").singleOrDefault();
        if (typeDefinition != null) {
            return typeDefinition;
        }
        for (Document schemaDoc : includedSchemas) {
            typeDefinition = schemaDoc.find("complexType[name=" + qualifiedName.getName() + "]").singleOrDefault();
            if (typeDefinition != null) {
                return typeDefinition;
            }
        }
        throw new IllegalArgumentException("Can't find type definition of " + qualifiedName);
    }

    private Element elementDefinition(QualifiedName elementName) {
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
    private Namespace xsNamespace;
    private boolean minimal;
    private boolean full;

    public void setMinimal(boolean minimal) {
        this.minimal = minimal;
    }

    public void setFull(boolean full) {
        this.full = full;
    }
}
