package org.eaxy;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import org.eaxy.utils.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(FileTestRunner.class)
@FileTestRunner.Directory("src/test/xml/performance-suite")
public class XmlPerformanceTest {

    private final String contents;
    private File xmlFile;

    public XmlPerformanceTest(File xmlFile) throws IOException {
        this.xmlFile = xmlFile;
        long startTime = System.currentTimeMillis();
        this.contents = IOUtils.slurp(xmlFile);
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
    public void readDocument() throws IOException {
        Xml.read(xmlFile);
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

}
