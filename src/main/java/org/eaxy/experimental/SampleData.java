package org.eaxy.experimental;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Random;

import org.eaxy.Element;
import org.eaxy.ElementSet;
import org.eaxy.Namespace;
import org.eaxy.QualifiedName;

public class SampleData {

    private Random random = new Random();

    private static final String[] SAMPLE_WORDS = { "about", "all", "also", "and", "as", "at", "be", "because", "but",
        "by", "can", "come", "could", "day", "do", "even", "find", "first", "for", "from", "get", "give", "go",
        "have", "he", "her", "here", "him", "his", "how", "I", "if", "in", "into", "it", "its", "just", "know",
        "like", "look", "make", "man", "many", "me", "more", "my", "new", "no", "not", "now", "of", "on", "one",
        "only", "or", "other", "our", "out", "people", "say", "see", "she", "so", "some", "take", "tell", "than",
        "that", "the", "their", "them", "then", "there", "these", "they", "thing", "think", "this", "those", "time",
        "to", "two", "up", "use", "very", "want", "way", "we", "well", "what", "when", "which", "who", "will",
        "with", "would", "year", "you", "your", };


    /**
     * Override this method to create custom rules for specific attributes
     * @param attributeName The name of the attribute to write
     * @param attrDef The element that defines the attribute
     * @param xsNamespace The declaration of the XSD-namespace to find the prefix for simple types
     * @return Random attribute value that fulfills the definition
     */
    public String randomAttributeText(String attributeName, Element attrDef, Namespace xsNamespace) {
        return randomData(attrDef, xsNamespace);
    }

    /**
     * Override this method to create custom rules for specific elements
     * @param elementName The name of the element to write
     * @param typeDefinition The element that defines the attribute
     * @param xsNamespace The declaration of the XSD-namespace to find the prefix for simple types
     * @return Random attribute value that fulfills the definition
     */
    public String randomElementText(QualifiedName elementName, Element typeDefinition, Namespace xsNamespace) {
        return randomData(typeDefinition, xsNamespace);
    }


    public String randomData(Element typeDef, Namespace xsNamespace) {
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

        String[] parts = type.split(":");
        if (parts[0].equals(xsNamespace.getPrefix())) {
            return randomXsdString(typeDef, parts[1]);
        }
        throw new IllegalArgumentException("Unknown base type " + type);
    }

    protected String randomXsdString(Element typeDef, String typeName) {
        if (typeName.equals("date")) {
            return randomDate().toString();
        } else if (typeName.equals("dateTime")) {
            return randomDateTime().toString();
        } else if (typeName.equals("boolean")) {
            return String.valueOf(random.nextBoolean());
        } else if (typeName.equals("string")) {
            return randomString(10, 20);
        } else if (typeName.equals("int")) {
            return String.valueOf(random(-10, 10));
        } else if (typeName.equals("positiveInteger")) {
            return String.valueOf(random(1, 10));
        } else if (typeName.equals("decimal")) {
            return String.valueOf(random(-1000, 10000) / 100);
        } else if (typeName.equals("float")) {
            return String.valueOf(random(-1000, 10000) / 100.0);
        } else if (typeName.equals("double")) {
            return String.valueOf(random(-1000, 10000) / 100.0);
        } else if (typeName.equals("base64Binary")) {
            // data:image/svg+xml;base64,
            return "PD94bWwgdmVyc2lvbj0iMS4wIiA/PjxzdmcgZGF0YS1uYW1lPSJMYXllciAxIiBpZD0iTGF5ZXJfMSIgdmlld0JveD0iMCAwIDQ4IDQ4IiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciPjxkZWZzPjxzdHlsZT4uY2xzLTEsLmNscy0ye2ZpbGw6bm9uZTtzdHJva2U6IzIzMWYyMDtzdHJva2UtbWl0ZXJsaW1pdDoxMDtzdHJva2Utd2lkdGg6MnB4O30uY2xzLTJ7c3Ryb2tlLWxpbmVjYXA6cm91bmQ7fS5jbHMtM3tmaWxsOiMyMzFmMjA7fTwvc3R5bGU+PC9kZWZzPjx0aXRsZS8+PGNpcmNsZSBjbGFzcz0iY2xzLTEiIGN4PSIyNCIgY3k9IjI0IiByPSIyMyIvPjxwYXRoIGNsYXNzPSJjbHMtMiIgZD0iTTE0LDMzczguODMsOS4zMywyMCwwIi8+PGVsbGlwc2UgY2xhc3M9ImNscy0zIiBjeD0iMTciIGN5PSIxOSIgcng9IjMiIHJ5PSI0Ii8+PGVsbGlwc2UgY2xhc3M9ImNscy0zIiBjeD0iMzEiIGN5PSIxOSIgcng9IjMiIHJ5PSI0Ii8+PC9zdmc+";
        } else if (typeName.equals("NMTOKEN")) {
            return typeDef.attr("fixed");
        } else {
            throw new IllegalArgumentException("Unimplemented XSD type " + typeName);
        }
    }

    private LocalDate randomDate() {
        return LocalDate.now().minusDays(100).plusDays(new Random().nextInt(200));
    }

    private Instant randomDateTime() {
        return ZonedDateTime.now().minusDays(100).plusMinutes(random.nextInt(200 * 24 * 60)).toInstant();
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

    protected int random(int lowerBound, int upperBound) {
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

}
