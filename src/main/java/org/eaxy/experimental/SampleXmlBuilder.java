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
import org.eaxy.Xml;

public class SampleXmlBuilder {

    private Document schemaDoc;
    private List<Document> includedSchemas = new ArrayList<>();
    private Random random = new Random();
    private String nsPrefix;

    public SampleXmlBuilder(Document schemaDoc, String nsPrefix) {
        this.schemaDoc = schemaDoc;
        this.nsPrefix = nsPrefix;

        xsNamespace = schemaDoc.getRootElement().getName().getNamespace();
    }

    public SampleXmlBuilder(URL resource, String nsPrefix) throws IOException {
        this.nsPrefix = nsPrefix;
        this.schemaDoc = Xml.read(resource);
        xsNamespace = schemaDoc.getRootElement().getNamespaceByUri("http://www.w3.org/2001/XMLSchema");
        for (Element xsdInclude : schemaDoc.find("include")) {
            this.includedSchemas.add(Xml.read(new URL(resource, xsdInclude.attr("schemaLocation"))));
        }
    }

    public Element createRandomElement(String elementName) {
        Element elementDefinition = elementDefinition(elementName);
        if (elementDefinition.type() != null) {
            return createElement(elementName, complexType(elementDefinition.type()));
        } else {
            Element complexMemberType = elementDefinition.find("complexType").single();
            return populateComplexType(complexMemberType, Xml.el(elementDefinition.name()));
        }
    }

    private Element createElement(String fullElementName, Element complexType) {
        String[] parts = fullElementName.split(":");
        String name = parts.length > 1 ? parts[1] : parts[0];
        Element resultElement = targetNamespace().el(name);
        populateComplexType(complexType, resultElement);
        return resultElement;
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
            String typeDef = attrDef.attr("ref");
            if (typeDef != null) {
                Element attrTypeDef = attributeDefinition(typeDef);

                ElementSet enumerations = attrTypeDef.find("simpleType", "restriction", "enumeration");
                if (enumerations.isPresent()) {
                    resultElement
                            .attr(targetNamespace().attr(attrTypeDef.name(), pickOne(enumerations.attrs("value"))));
                }
            } else if (isXsdType(attrDef.type())) {
                resultElement.attr(attrDef.name(), randomData(attrDef));
            } else {
                String typeNameFull = attrDef.type();
                String[] nameParts = typeNameFull.split(":");
                String typeName = nameParts.length > 1 ? nameParts[1] : typeNameFull;
                Element simpleType = schemaDoc.find("simpleType[name=" + typeName + "]").single();

                String baseType = simpleType.find("restriction").single().attr("base");
                if (baseType.matches(xsNamespace.name("string").print())) {
                    resultElement.attr(attrDef.name(), "123-AB");
                } else {
                    throw new RuntimeException("Don't know what to do with " + baseType);
                }
            }
        }
    }

    private boolean chance(double p) {
        return random.nextDouble() < p;
    }

    private Instant randomDateTime() {
        return ZonedDateTime.now().minusDays(100).plusMinutes(new Random().nextInt(200 * 24 * 60)).toInstant();
    }

    private void appendSequence(Element resultElement, Element complexType) {
        for (Element seqMemberDef : complexType.find("sequence", "*")) {
            appendChildElements(resultElement, seqMemberDef);
        }
        for (Element seqMemberDef : complexType.find("all", "*")) {
            appendChildElements(resultElement, seqMemberDef);
        }
    }

    private void appendChildElements(Element resultElement, Element memberDef) {
        int occurances = occurences(memberDef);
        for (int i = 0; i < occurances; i++) {
            String typeDef = memberDef.attr("ref");
            if (typeDef != null) {
                Element elementDef = elementDefinition(typeDef);
                if (isXsdType(elementDef.type())) {
                    resultElement.add(targetNamespace().el(elementDef.name(), randomData(elementDef)));
                } else {
                    resultElement.add(createRandomElement(elementDef.type()));
                }
                continue;
            }

            if (memberDef.type() == null) {
                // TODO: Can be any nested type
                Element complexMemberType = memberDef.find("complexType").singleOrDefault();
                if (complexMemberType != null) {
                    Element element = Xml.el(memberDef.name());
                    populateComplexType(complexMemberType, element);
                    resultElement.add(element);
                } else {
                    Element el = Xml.el(memberDef.name());
                    Element simpleMemberType = memberDef.find("simpleType", "restriction").single();
                    el.text(randomData(simpleMemberType));
                    resultElement.add(el);
                }
            } else if (isXsdType(memberDef.type())) {
                Element el = Xml.el(memberDef.attr("name"));
                el.text(randomData(memberDef));
                resultElement.add(el);
            } else {
                Element element = Xml.el(memberDef.name());
                populateComplexType(complexType(memberDef.type()), element);
                resultElement.add(element);
            }
        }
    }

    private int occurences(Element seqMemberDef) {
        int occurences = 1;
        if (seqMemberDef.hasAttr("maxOccurs") && !seqMemberDef.attr("maxOccurs").equals("1")) {
            int lowerBound = full ? 2 : 1;
            occurences = random(lowerBound, 10);
        }
        if ("0".equals(seqMemberDef.attr("minOccurs"))) {
            if (minimal || (!full && chance(.50))) {
                occurences = 0;
            }
        }
        return occurences;
    }

    private String randomData(Element typeDef) {
        String type = typeDef.type();
        if (type == null)
            type = typeDef.attr("base");
        if (type.matches(xsNamespace.name("date").print())) {
            return randomDate().toString();
        } else if (type.matches(xsNamespace.name("dateTime").print())) {
            return randomDateTime().toString();
        } else if (type.matches(xsNamespace.name("string").print())) {
            return randomString(10, 20);
        } else if (type.matches(xsNamespace.name("int").print())) {
            return String.valueOf(random(-1000, 10000));
        } else if (type.matches(xsNamespace.name("positiveInteger").print())) {
            return String.valueOf(random(0, 100));
        } else if (type.matches(xsNamespace.name("decimal").print())) {
            return String.valueOf(random(-1000, 10000) / 100);
        } else if (type.matches(xsNamespace.name("float").print())) {
            return String.valueOf(random(-1000, 10000) / 100);
        } else if (type.matches(xsNamespace.name("base64Binary").print())) {
            return "sdfmsdlgnsd";
        } else if (type.matches(xsNamespace.name("NMTOKEN").print())) {
            return typeDef.attr("fixed");
        }
        throw new IllegalArgumentException("Unknown base type " + type);
    }

    private boolean isXsdType(String type) {
        if (type.contains(":")) {
            return type.split(":")[0].equals(xsNamespace.getPrefix());
        } else {
            return xsNamespace.getPrefix() == null;
        }
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
        String[] nameParts = typeNameFull.split(":");
        String typeName = nameParts.length > 1 ? nameParts[1] : typeNameFull;
        return schemaDoc.find("attribute[name=" + typeName + "]").single();
    }

    private Element complexType(String typeNameFull) {
        String[] nameParts = typeNameFull.split(":");
        String typeName = nameParts.length > 1 ? nameParts[1] : typeNameFull;
        Element typeDefinition = schemaDoc.find("complexType[name=" + typeName + "]").singleOrDefault();
        if (typeDefinition != null) {
            return typeDefinition;
        }
        for (Document schemaDoc : includedSchemas) {
            typeDefinition = schemaDoc.find("complexType[name=" + typeName + "]").singleOrDefault();
            if (typeDefinition != null) {
                return typeDefinition;
            }
        }
        throw new IllegalArgumentException("Can't find type definition of " + typeName);
    }

    private Element elementDefinition(String elementNameFull) {
        String[] nameParts = elementNameFull.split(":");
        String elementName = nameParts.length > 1 ? nameParts[1] : elementNameFull;
        Element typeDefinition = schemaDoc.find("element[name=" + elementName + "]").singleOrDefault();
        if (typeDefinition != null) {
            return typeDefinition;
        }
        for (Document schemaDoc : includedSchemas) {
            typeDefinition = schemaDoc.find("element[name=" + elementName + "]").singleOrDefault();
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
