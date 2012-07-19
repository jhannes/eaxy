package org.eaxy;

import static org.fest.assertions.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import org.eaxy.Xml;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(FileTestRunner.class)
@FileTestRunner.Directory("src/test/xml/performance-suite")
public class XmlPerformanceTest {

    private String contents;

    public XmlPerformanceTest(File xmlFile) throws IOException {
        long startTime = System.currentTimeMillis();
        this.contents = slurp(xmlFile);
        long duration = System.currentTimeMillis() - startTime;
        if (duration > timeout()) {
            System.err.println("Warning: " + xmlFile + " read in " + (duration/1000.0) + "s - length: " + contents.length());
        }
    }

    private int timeout() {
        // 1.5 second + 0.2 seconds/MB
        return Math.min(1500 + contents.length()/5000, 10000);
    }

    @Test
    public void readsShouldBeFast() {
        long startTime = System.currentTimeMillis();
        Xml.xml(contents);
        assertThat(System.currentTimeMillis() - startTime).as("millis").isLessThan(timeout());
    }

    @Test
    public void writesShouldBeFast() {
        Document element = Xml.xml(contents);
        long startTime = System.currentTimeMillis();
        element.toXML();
        assertThat(System.currentTimeMillis() - startTime).as("millis").isLessThan(timeout());
    }

    protected String normalize(String text) {
        return text.trim().replaceAll("\\s+", " ");
    }

    private static String slurp(File file) throws IOException {
        BufferedReader reader;
        reader = new BufferedReader(new FileReader(file), 1024);
        if (file.getName().endsWith(".gz")) {
            reader = new BufferedReader(
                    new InputStreamReader(new GZIPInputStream(new FileInputStream(file))), 1024);
        }
        StringBuilder result = new StringBuilder();
        int c;
        while ((c = reader.read()) != -1) {
            result.append((char)c);
        }
        return result.toString();
    }

}
