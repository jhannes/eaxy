package org.eaxy.experimental;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eaxy.Document;
import org.eaxy.Element;
import org.eaxy.Xml;
import com.sun.net.httpserver.HttpExchange;

@SuppressWarnings("restriction")
class WebServer {

    protected final com.sun.net.httpserver.HttpServer server;

    public WebServer(int port) throws IOException {
        server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(InetAddress.getByName("localhost"), port), 0);
    }

    protected URL getUrl(HttpExchange exchange) throws MalformedURLException {
        return new URL("http", server.getAddress().getHostString(), server.getAddress().getPort(), exchange.getRequestURI().getPath());
    }

    public URL getUrl() throws MalformedURLException {
        return new URL("http", server.getAddress().getHostString(), server.getAddress().getPort(), "/");
    }

    protected void writeXmlResponse(Element output, String contentType, HttpExchange exchange) throws IOException {
        byte[] xml = output.toXML().getBytes();
        exchange.sendResponseHeaders(200, xml.length);
        exchange.getResponseHeaders().add("Content-Type", contentType);
        exchange.getResponseBody().write(xml);
        exchange.close();
    }

    protected void writeXmlResponse(Element output, HttpExchange exchange) throws IOException {
        writeXmlResponse(output, "text/xml", exchange);
    }

    protected void sendRedirect(HttpExchange exchange, String relativePath) throws IOException {
        sendRedirect(exchange, new URL(getUrl(exchange), relativePath));
    }

    protected void sendRedirect(HttpExchange exchange, URL redirectUrl) throws IOException {
        exchange.getResponseHeaders().set("Location", redirectUrl.toString());
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_SEE_OTHER, -1);
        exchange.close();
    }
}

@SuppressWarnings("restriction")
public class SoapSimulatorServer extends WebServer {
    protected final Map<String, SampleSoapXmlBuilder> soapEndpoints = new HashMap<>();

    public SoapSimulatorServer(int port) throws IOException {
        super(port);
        server.createContext("/soap", this::soapContext);
    }

    private void soapContext(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String soapAction = exchange.getRequestHeaders().getFirst("SOAPAction");
        Document xmlRequest = Xml.read(new InputStreamReader(exchange.getRequestBody()));

        if (!soapEndpoints.containsKey(path)) {
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, -1);
            exchange.close();
            return;
        }

        Element response = soapEndpoints.get(path).processRequest(soapAction, xmlRequest);
        writeXmlResponse(response, exchange);
    }

    URL addSoapEndpoint(String url, Document wsdl) throws IOException {
        soapEndpoints.put(url, new SampleSoapXmlBuilder(wsdl));
        return new URL(getUrl(), url);
    }

    public static void main(String[] args) throws IOException {
        SoapSimulatorServer server = new SoapSimulatorServer(10080);
        server.addSoapEndpoint("/soap/stockQuote",
            Xml.read(new File("src/test/resources/xsd/StockQuoteService.wsdl")));
        server.start();
    }

    void start() {
        server.start();
        System.out.println(server.getAddress() + " started");
    }
}
