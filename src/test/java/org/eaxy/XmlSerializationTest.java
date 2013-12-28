package org.eaxy;

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

import org.eaxy.utils.IOUtils;
import org.fest.assertions.api.StringAssert;
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
        InputStreamReader input = input();
		Document doc = StaxReader.read(input);
		input.close();
		assertThat(normalize(doc.copy().toXML()))
            .isEqualTo(normalize(IOUtils.slurp(xmlFile)));
    }

    @Test
	public void shouldReadWithSax() throws Exception {
        InputStreamReader input = input();
		Document doc = SaxReader.read(input);
		input.close();
		assertThat(normalize(doc.copy().toXML()))
            .isEqualTo(normalize(IOUtils.slurp(xmlFile)));
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
    	Document doc = DomTransformer.fromDom(DomTransformer.toDom(Xml.read(xmlFile)));
		assertEquals(DomTransformer.fromDom(DomTransformer.toDom(doc)),
    			IOUtils.slurp(xmlFile));
    }

	private StringAssert assertEquals(Document document, String fileContents) {
		return assertThat(normalize(document.toXML()))
            .isEqualTo(normalize(fileContents));
	}

    protected String normalize(String text) {
        return text.trim().replaceAll("\\s+", " ");
    }

}
