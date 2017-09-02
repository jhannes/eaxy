package org.eaxy.experimental;

import static org.assertj.core.api.Assertions.assertThat;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

import org.eaxy.Document;
import org.eaxy.Element;
import org.eaxy.Namespace;
import org.eaxy.Validator;
import org.eaxy.Xml;
import org.eaxy.experimental.SampleSoapXmlBuilder.SoapOperationDefinition;
import org.junit.Test;

public class SoapSimulatorServerTest {

    private static final Namespace SOAP = new Namespace("http://schemas.xmlsoap.org/soap/envelope/", "S");

    @Test
    public void shouldRespondToSoapCall() throws IOException {
        Document wsdlFile = Xml.readResource("/xsd/StockQuoteService.wsdl");

        SoapSimulatorServer server = new SoapSimulatorServer(0);
        server.addSoapEndpoint("/soap/stockQuote", wsdlFile);
        server.start();

        URL url = new URL(server.getUrl(), "/soap/stockQuote");

        SampleSoapXmlBuilder builder = new SampleSoapXmlBuilder(wsdlFile);
        SoapOperationDefinition operation = builder.getService().operation("GetLastTradePrice");
        Element input = SOAP.el("Envelope",
            SOAP.el("Header"),
            SOAP.el("Body", operation.randomInput("m")));
        String soapAction = operation.getSoapAction();

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("SOAPAction", soapAction);
        connection.setRequestProperty("Content-type", "text/xml");

        try (Writer writer = new OutputStreamWriter(connection.getOutputStream())) {
            writer.write(input.toIndentedXML());
        }
        int responseCode = connection.getResponseCode();
        System.out.println(responseCode);

        Element response;
        try (Reader reader = new InputStreamReader(connection.getInputStream())) {
            response = Xml.read(reader).getRootElement();
        }

        assertThat(response.getName()).isEqualTo(SOAP.name("Envelope"));
        Element output = response.find(SOAP.name("Body"), "TradePrice").single();
        new Validator(operation.targetSchema()).validate(output);
    }


}
