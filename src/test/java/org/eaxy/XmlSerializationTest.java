package org.eaxy;

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.File;

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
    public void serializedShouldMatch() throws Exception {
        assertThat(normalize(Xml.read(xmlFile).copy().toXML()))
            .isEqualTo(normalize(IOUtils.slurp(xmlFile)));
    }

    @Test
    public void shouldTransformViaDom() throws Exception {
        assertThat(normalize(Xml.fromDom(Xml.read(xmlFile).getRootElement().toDom()).toXML()))
            .isEqualTo(normalize(IOUtils.slurp(xmlFile)));
    }

    protected String normalize(String text) {
        return text.trim().replaceAll("\\s+", " ");
    }

}
