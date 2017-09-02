package org.eaxy.experimental;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import org.eaxy.Document;
import org.eaxy.Xml;
import org.junit.Test;

public class SoapSimulatorWebAppTest {

    @Test
    public void shouldShowWsdlFilesOnFrontPage() throws IOException {
        SoapSimulatorWebApp server = new SoapSimulatorWebApp(0);
        server.addSoapEndpoint("/soap/stockQuote", Xml.readResource("/xsd/StockQuoteService.wsdl"));
        server.start();

        Document frontPage = Xml.read(server.getUrl());
        assertThat(frontPage.find("...", "ul").find("li", "a").check().texts())
            .contains("StockQuoteService");
    }

    @Test
    public void shouldUploadDocument() throws IOException {
        SoapSimulatorWebApp server = new SoapSimulatorWebApp(0);
        server.start();

        HttpURLConnection connection = (HttpURLConnection) new URL(server.getUrl(), "/doc/").openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-type", "multipart/form-data; boundary=abc123");

        try (Writer writer = new OutputStreamWriter(connection.getOutputStream())) {
            writer.write("--abc123\r\n");
            writer.write("Content-disposition: form-data; name=\"soapRouterUrl\"\r\n");
            writer.write("/soap/testMessage\r\r");
            writer.write("\r\n");

            writer.write("--abc123\r\n");
            writer.write("Content-disposition: form-data; name=\"wsdlFile\"; content-type=text/xml\r\n");
            writer.write("\r\n");
            writer.write(Xml.readResource("/xsd/greath-reservation.wsdl").toIndentedXML());
            writer.write("\r\n");
            writer.write("--abc123--\r\n");
        }

        assertThat(connection.getResponseCode()).isEqualTo(200);

        Document frontPage = Xml.read(server.getUrl());
        assertThat(frontPage.find("...", "ul").find("li", "a").check().texts())
            .contains("reservationService");
    }

}
