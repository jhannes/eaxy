package org.eaxy.experimental;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;

import org.eaxy.FileTestRunner;
import org.eaxy.experimental.SampleSoapXmlBuilder.SoapOperationDefinition;
import org.eaxy.experimental.SampleSoapXmlBuilder.SoapServiceDefinition;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(FileTestRunner.class)
@FileTestRunner.Directory(value = {"src/test/xml/wsdl-suite", "src/local/xml/wsdl-suite"}, extension = ".wsdl")
public class SoapXmlBuilderDirectoryTest {

    private File wsdlFile;

    public SoapXmlBuilderDirectoryTest(File wsdlFile) {
        this.wsdlFile = wsdlFile;
        Assume.assumeTrue(wsdlFile.getName().endsWith(".wsdl"));
    }

    @Test
    public void shouldGenerateInput() throws IOException {
        SampleSoapXmlBuilder builder = new SampleSoapXmlBuilder(wsdlFile.toURI().toURL());

        SoapServiceDefinition service = builder.getService();
        for (SoapOperationDefinition operation : service.getOperations()) {
            operation.randomInput("m");
        }
        assertThat(service.getOperations()).isNotEmpty();
    }

    @Test
    public void shouldGenerateOutput() throws IOException {
        SampleSoapXmlBuilder builder = new SampleSoapXmlBuilder(wsdlFile.toURI().toURL());

        SoapServiceDefinition service = builder.getService();
        for (SoapOperationDefinition operation : service.getOperations()) {
            operation.randomOutput("o");
        }
        assertThat(service.getOperations()).isNotEmpty();
    }


}
