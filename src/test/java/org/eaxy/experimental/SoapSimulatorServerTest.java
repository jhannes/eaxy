package org.eaxy.experimental;

import java.io.IOException;
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
        server.start();

        URL url = server.addSoapEndpoint("/soap/stockQuote", wsdlFile);

        SampleSoapXmlBuilder builder = new SampleSoapXmlBuilder(wsdlFile);
        SoapOperationDefinition operation = builder.getService().operation("GetLastTradePrice");

        Element response = operation.sendRandomRequest(url);
        Element output = response.find(SOAP.name("Body"), "TradePrice").single();
        new Validator(operation.targetSchema()).validate(output);
    }


}
