package org.eaxy;

import static org.eaxy.Xml.el;
import static org.eaxy.Xml.text;
import static org.fest.assertions.Assertions.assertThat;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import org.junit.Test;

public class LargeDocumentTest {

    public static void main(String[] args) throws IOException {

        int elementCount = 1000;
        while (true) {
            System.out.println("buildDocument\t" + elementCount + "\t" + buildDocument(elementCount));
            System.out.println("writeLargeDocument\t" + elementCount + "\t" + writeLargeDocument(elementCount));
            System.out.println("searchInLargeDocument\t" + elementCount + "\t" + searchInLargeDocument(elementCount));
            elementCount *= 2;
        }

    }

    private static Random random = new Random();

    private int elementCount = getProperty("elementCount", 1000);

    @Test
    public void shouldBuildLargeDocument() {
        long duration = buildDocument(elementCount);
        assertThat(duration).isLessThan(elementCount/40+100).as("duration");
    }

    private static long buildDocument(int elementCount) {
        long start = System.currentTimeMillis();
        Element root = el("root");
        for (int i=0; i<elementCount; i++) {
            root.add(el("some_element").text("with text").id(String.valueOf(random.nextInt())));
            root.add(text("\n"));
        }
        long duration = System.currentTimeMillis()-start;
        return duration;
    }

    @Test
    public void shouldWriteHugeXmlDocument() throws IOException {
        long duration = writeLargeDocument(elementCount);
        assertThat(duration).isLessThan(elementCount/10+400).as("duration");
    }

    private static long writeLargeDocument(int elementCount) throws IOException {
        Element root = el("root");
        for (int i=0; i<elementCount; i++) {
            root.add(el("some_element").text("with text").id(String.valueOf(random.nextInt())));
            root.add(text("\n"));
        }
        FileWriter writer = new FileWriter(getTmpFile());
        long start = System.currentTimeMillis();
        new Document(root).write(writer);
        writer.close();
        long duration = System.currentTimeMillis()-start;
        return duration;
    }

    @Test
    public void shouldFindContentsInLargeDocument() {
        long duration = searchInLargeDocument(elementCount);
        assertThat(duration).isLessThan(elementCount/25+250).as("duration");
    }

    private static long searchInLargeDocument(int elementCount) {
        Element root = el("root");
        for (int i=0; i<elementCount; i++) {
            int nextInt = random.nextInt(10);
            String className = "class-" + nextInt;
            root.add(el("some_element",
                    el("child").addClass(className).text(nextInt == 0 ? "yes" : "no")));
            root.add(text("\n"));
        }
        long start = System.currentTimeMillis();
        ElementSet matches = root.find("some_element", "child.class-0");
        assertThat(matches.texts()).contains("yes").excludes("no");
        assertThat(matches.size()).isGreaterThan(elementCount/15).isLessThan(elementCount/8);
        long duration = System.currentTimeMillis()-start;
        return duration;
    }

    private static String getTmpFile() {
        return System.getProperty("java.io.tmpdir") + "/example-xml-" + random.nextInt() + ".xml";
    }

    private int getProperty(String parameter, int defaultValue) {
        return Integer.parseInt(getProperty(parameter, String.valueOf(defaultValue)));
    }

    private String getProperty(String parameter, String defaultString) {
        String key = LargeDocumentTest.class.getName() + "." + parameter;
        return System.getProperty(key, defaultString);
    }

}
