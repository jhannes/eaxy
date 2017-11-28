package org.eaxy.experimental;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eaxy.Element;
import org.eaxy.experimental.SampleSoapXmlBuilder.SoapOperationDefinition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class SoapXmlBuilderDirectoryTest {

    @Parameters(name = "{0}")
    public static List<Object[]> suite() throws IOException {
        ArrayList<Object[]> result = new ArrayList<>();

        File directory = new File("src/test/xml/wsdl-suite");
        if (directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                if (!file.getName().endsWith(".wsdl")) continue;
                SampleSoapXmlBuilder builder = new SampleSoapXmlBuilder(file.toURI().toURL());
                for (SoapOperationDefinition operationDefinition : builder.getService().getOperations()) {
                    result.add(new Object[] { operationDefinition });
                }
            }
        }
        directory = new File("src/local/xml/wsdl-suite");
        if (directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                if (!file.getName().endsWith(".wsdl")) continue;
                SampleSoapXmlBuilder builder = new SampleSoapXmlBuilder(file.toURI().toURL());
                for (SoapOperationDefinition operationDefinition : builder.getService().getOperations()) {
                    result.add(new Object[] { operationDefinition });
                }
            }
        }


        return result;
    }

    private SoapOperationDefinition operation;

    public SoapXmlBuilderDirectoryTest(SoapOperationDefinition operation) {
        this.operation = operation;
    }

    @Test
    public void shouldGenerateInput() throws IOException {
        Element msg = operation.randomInput("m");
        operation.getValidator().validate(msg);
    }

    @Test
    public void shouldGenerateOutput() throws IOException {
        Element msg = operation.randomOutput("o");
        operation.getValidator().validate(msg);
    }


}
