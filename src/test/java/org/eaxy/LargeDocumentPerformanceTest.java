package org.eaxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eaxy.Xml.el;
import static org.eaxy.Xml.text;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.Random;

import org.junit.Test;

public class LargeDocumentPerformanceTest {

    public static void main(String[] args) throws IOException {

        int step = 1000;
        while (true) {
            int elementCount = step/1000 * 1000;
            System.out.print("buildLargeDocument\t" + elementCount + "\t");
            System.out.println(buildLargeDocument(elementCount));
            System.out.print("writeLargeDocument\t" + elementCount + "\t");
            System.out.println(writeLargeDocument(elementCount));
            System.out.print("searchInLargeDocument\t" + elementCount + "\t");
            System.out.println(searchInLargeDocument(elementCount));
            step *= 1.5;
        }
    }

    private static Random random = new Random();

    private final int elementCount = getProperty("elementCount", 10000);

    @Test
    public void shouldBuildLargeDocument() {
        long duration = buildLargeDocument(elementCount);
        assertThat(duration).as("duration").isLessThan(elementCount/30+100);
    }

    private static long buildLargeDocument(int elementCount) {
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
        assertThat(duration).as("duration").isLessThan(elementCount/5+500);
    }

    private static long writeLargeDocument(int elementCount) throws IOException {
        Element root = el("root");
        for (int i=0; i<elementCount; i++) {
            root.add(el("some_element").text("with text").id(String.valueOf(random.nextInt())));
            root.add(text("\n"));
        }

        long start = System.currentTimeMillis();
        try(FileWriter writer = new FileWriter(getTmpFile())) {
            new Document(root).visit(new WriterXmlVisitor(writer));
            writer.close();
        }
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
        assertThat(matches.texts()).contains("yes").doesNotContain("no");
        assertThat(matches.size()).isGreaterThan(elementCount/15).isLessThan(elementCount/8);
        long duration = System.currentTimeMillis()-start;
        return duration;
    }

    @Test
    public void shouldIterateContentsInLargeDocument() throws IOException {
        long duration = iterateInLargeDocument(elementCount);
        assertThat(duration).isLessThan(elementCount/5+250).as("duration");
    }

    private static long iterateInLargeDocument(int elementCount) {
        Element root = el("root");
        for (int i=0; i<elementCount; i++) {
            int nextInt = random.nextInt(10);
            String className = "class-" + nextInt;
            root.add(el("some_element",
                    el("child").addClass(className).text(nextInt == 0 ? "yes" : "no")));
            root.add(text("\n"));
        }
        long start = System.currentTimeMillis();

        for (Element element : ElementFilters.create("some_element", "child.class-0").iterate(new StringReader(new Document(root).toXML()))) {
            assertThat(element.text()).contains("yes");
        }
        return System.currentTimeMillis()-start;
    }

    private static String getTmpFile() {
        return System.getProperty("java.io.tmpdir") + "/example-xml-" + random.nextInt() + ".xml";
    }

    private int getProperty(String parameter, int defaultValue) {
        return Integer.parseInt(getProperty(parameter, String.valueOf(defaultValue)));
    }

    private String getProperty(String parameter, String defaultString) {
        String key = LargeDocumentPerformanceTest.class.getName() + "." + parameter;
        return System.getProperty(key, defaultString);
    }

}
