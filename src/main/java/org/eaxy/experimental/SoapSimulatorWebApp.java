package org.eaxy.experimental;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import org.eaxy.Content;
import org.eaxy.Element;
import org.eaxy.Xml;

import com.sun.net.httpserver.HttpExchange;

@SuppressWarnings("restriction")
public class SoapSimulatorWebApp extends SoapSimulatorServer {

    public SoapSimulatorWebApp(int port) throws IOException {
        super(port);
        server.createContext("/", this::rootContext);
        server.createContext("/doc", this::docContext);
    }

    private void rootContext(HttpExchange exchange) throws IOException {
        if (exchange.getRequestURI().getPath().equals("/favicon.ico")) {
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, -1);
            exchange.close();
        } else {
            sendRedirect(exchange, "/doc/");
        }
    }

    private void docContext(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equals("POST")) {
            Map<String, String> formData = parseMultipart(exchange);
            addSoapEndpoint(formData.get("\"soapRouterUrl\""),
                    Xml.xml(formData.get("\"wsdlFile\"")));
            sendRedirect(exchange, "/doc/");
        } else {
            writeXmlResponse(indexPage(), "text/html", exchange);
        }
    }

    private Element indexPage() {
        Content[] endpointList = Xml.map(soapEndpoints.keySet(),
            endpoint -> Xml.el("li", Xml.el("a", Xml.attr("href", endpoint),
                            Xml.text(soapEndpoints.get(endpoint).getPortName()))));
        return Xml.el("html",
            Xml.el("head", Xml.el("title", "Hello world")),
            Xml.el("body",
                Xml.el("h1", "SOAP Service simulator"),
                Xml.el("h2", "Existing endpoints"),
                Xml.el("ul", endpointList),
                Xml.el("h2", "Register new WSDL-file"),
                Xml.el("form", Xml.attr("method", "POST"), Xml.attr("enctype", "multipart/form-data"),
                    Xml.el("label", "WSDL-file"),
                    Xml.el("input").type("file").name("wsdlFile"),
                    Xml.el("label", "Path"),
                    Xml.el("input").type("text").name("soapRouterUrl"),
                    Xml.el("label", "Enabled"),
                    Xml.el("input").type("checkbox").name("enabled"),
                    Xml.el("button", "Upload"))));
    }

    public static void main(String[] args) throws IOException {
        SoapSimulatorWebApp server = new SoapSimulatorWebApp(10080);
        server.addSoapEndpoint("/soap/stockQuote",
            Xml.read(new File("src/test/resources/xsd/StockQuoteService.wsdl")));
        server.start();
    }

    protected static Map<String, String> parseMultipart(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestHeaders().getFirst("Content-type").split(";")[0].equals("multipart/form-data")) {
            System.err.println("Invalid content type " + exchange.getRequestHeaders().getFirst("Content-type").split(";"));
        }
        String[] contentTypeParam = exchange.getRequestHeaders().getFirst("Content-type").split(";")[1].split("=");
        if (!contentTypeParam[0].trim().equals("boundary")) {
            System.err.println("Invalid content type param " + contentTypeParam);
        }
        String boundary = "--" + contentTypeParam[1].trim();
        try(InputStream inputStream = exchange.getRequestBody()) {
            return readMultipart(boundary, inputStream);
        }
    }

    private static Map<String, String> readMultipart(String boundary, InputStream inputStream) throws IOException {
        Map<String, String> formData = new HashMap<>();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line = reader.readLine();
        OUTER: while (line != null) {
            String currentName = null;
            String header = line;
            if (header.equals(boundary)) {
                header = reader.readLine();
                if (header == null) {
                    break;
                }
            }

            int partHeaderOffset = header.indexOf(':');
            if (!header.substring(0, partHeaderOffset).equalsIgnoreCase("Content-Disposition")) {
                System.err.println("Unexpected header " + header.substring(0, partHeaderOffset));
            }
            String[] arguments = header.substring(partHeaderOffset+1).trim().split(";");
            for (String argument : arguments) {
                if (argument.trim().equals("form-data")) continue;
                String[] arg = argument.split("=");
                if (arg[0].trim().equals("name")) {
                    currentName = arg[1].trim();
                }
            }
            do {
                line = reader.readLine();
                if (line == null) return formData;
            } while (!line.trim().isEmpty());

            formData.put(currentName, "");
            do {
                line = reader.readLine();
                if (line == null) return formData;
                if (line.startsWith(boundary)) {
                    line = reader.readLine();
                    continue OUTER;
                }
                String nextLine = line;
                formData.compute(currentName, (k,v) -> v + nextLine);
            } while (true);
        }
        return formData;
    }
}
