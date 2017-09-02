package org.eaxy;


import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.assertj.core.api.StringAssert;
import org.eaxy.utils.IOUtils;
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
    public void shouldReadWithStax() throws Exception {
        try (InputStreamReader input = input()) {
            Document doc = StaxReader.read(input);
            input.close();
            assertThat(normalize(doc.copy().toXML()))
            .isEqualTo(normalize(IOUtils.slurp(xmlFile)));
        }
    }

    @Test
    public void shouldReadWithSax() throws Exception {
        try(InputStream input = new FileInputStream(xmlFile)) {
            Document doc = SaxReader.read(input);
            input.close();
            assertThat(normalize(doc.copy().toXML()))
                .isEqualTo(normalize(IOUtils.slurp(xmlFile)));
        }
    }

    private InputStreamReader input() throws FileNotFoundException {
        return new InputStreamReader(new FileInputStream(xmlFile));
    }

    @Test
    public void shouldTransformSerializedViaDom() throws Exception {
        assertEquals(DomSerializedTransformer.fromDom(DomSerializedTransformer.toDom(Xml.read(xmlFile))),
                IOUtils.slurp(xmlFile));
    }

    @Test
    public void shouldTransformViaDom() throws Exception {
        Document doc = Xml.fromDom(Xml.toDom(Xml.read(xmlFile)));
        assertEquals(Xml.fromDom(Xml.toDom(doc)),
                IOUtils.slurp(xmlFile));
    }

    @Test
    public void shouldIterate() throws IOException {
        int elementCount = 0;
        ElementQuery filter = ElementFilters.create("*");
        for (@SuppressWarnings("unused") Element element : filter.iterate(xmlFile.toURI().toURL())) {
            elementCount++;
        }
        assertThat(elementCount).isEqualTo(Xml.read(xmlFile).find(filter).size());
    }

    private StringAssert assertEquals(Document document, String fileContents) {
        return (StringAssert) assertThat(normalize(document.toXML()))
            .isEqualTo(normalize(fileContents));
    }

    protected String normalize(String text) {
        return text.trim().replaceAll("\\s+", " ");
    }

}
