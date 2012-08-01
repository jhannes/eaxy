package org.eaxy;

import static org.fest.assertions.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.eaxy.Xml;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(FileTestRunner.class)
@FileTestRunner.Directory("src/test/xml/samples")
public class XmlSerializationTest {

    private final File xmlFile;

    public XmlSerializationTest(File xmlFile) {
        this.xmlFile = xmlFile;
    }

    @Test
    public void serializedShouldMatch() throws Exception {
        assertThat(normalize(Xml.read(xmlFile).toXML()))
            .isEqualTo(normalize(slurp(xmlFile)));
    }

    @Test
    public void shouldTransformViaDom() throws Exception {
        assertThat(normalize(Xml.fromDom(Xml.read(xmlFile).getRootElement().toDom()).toXML()))
            .isEqualTo(normalize(slurp(xmlFile)));
    }

    protected String normalize(String text) {
        return text.trim().replaceAll("\\s+", " ");
    }

    private static String slurp(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file), 1024);
        StringBuilder result = new StringBuilder();
        int c;
        while ((c = reader.read()) != -1) {
            result.append((char)c);
        }
        return result.toString();
    }

}
