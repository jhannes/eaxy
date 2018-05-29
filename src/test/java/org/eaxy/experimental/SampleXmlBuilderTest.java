package org.eaxy.experimental;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.eaxy.Document;
import org.eaxy.Element;
import org.eaxy.Namespace;
import org.eaxy.NonMatchingPathException;
import org.eaxy.SchemaValidationException;
import org.eaxy.Validator;
import org.eaxy.Xml;
import org.eaxy.experimental.SampleSoapXmlBuilder.SoapOperationDefinition;
import org.junit.Test;

public class SampleXmlBuilderTest {

    @Test
    public void shouldGenerateMessageFromWsdl() throws IOException {
        SampleSoapXmlBuilder builder = new SampleSoapXmlBuilder("xsd/greath-reservation.wsdl");
        SoapOperationDefinition operation = builder.service("reservationService").operation("opCheckAvailability");
        Validator validator = operation.getValidator();
        validator.validate(operation.randomInput("gh"));
        validator.validate(operation.randomOutput("gh"));

    }

    @Test
    public void shouldGenerateAttributeValues() throws IOException {
        Document schemaDoc = Xml.readResource("/mailmessage.xsd");
        SampleXmlBuilder generator = new SampleXmlBuilder(schemaDoc, null, new SampleData());
        generator.setFull(true);
        Element element = generator.createRandomElement("message");
        assertThat(element.find("recipients", "recipient").first().attr("type")).isIn("email", "phone");
    }

    @Test
    public void shouldGenerateManyElementsWhenAppropriate() throws Exception {
        Document schemaDoc = Xml.readResource("/mailmessage.xsd");
        SampleXmlBuilder generator = new SampleXmlBuilder(schemaDoc, "msg", new SampleData());
        generator.setFull(true);
        Element element = generator.createRandomElement("message");
        generator.getValidator().validate(element);
        assertThat(element.find("recipients", "recipient").size()).isGreaterThan(1);
    }

    @Test
    public void shouldBuildFull() throws IOException {
        SampleXmlBuilder generator = new SampleXmlBuilder(getClass().getResource("/xsd/po.xsd"), "po");
        generator.setFull(true);
        Element el = generator.createRandomElement("purchaseOrder");
        assertThat(el.hasAttr("orderDate")).isTrue();
        assertThat(el.find("comment")).isNotEmpty();
        assertThat(el.find("items").check().find("item")).isNotEmpty();
        assertThat(el.find("shipTo").single().attr("country")).isEqualTo("US");
        generator.getValidator().validate(el);
    }

    @Test
    public void shouldBuildMinimal() throws IOException {
        Document schemaDoc = Xml.readResource("/xsd/po.xsd");
        SampleXmlBuilder generator = new SampleXmlBuilder(schemaDoc, null, new SampleData());
        generator.setMinimal(true);
        Element el = generator.createRandomElement("purchaseOrder");
        assertThat(el.hasAttr("orderDate")).isFalse();
        assertThat(el.find("comment")).isEmpty();
        assertThat(el.find("items").check().find("item")).isEmpty();
        generator.getValidator().validate(el);
    }

    @Test
    public void shouldGenerateFromMultipleFiles() throws Exception {
    	// TODO: This used regex for SKU - would be very cool if we could support a simplified regex generator
        SampleXmlBuilder generator = new SampleXmlBuilder(getClass().getResource("/xsd/ipo.xsd"), "ipo");
        Element element = generator.createRandomElement("purchaseOrder");
        new Validator(new String[] { "xsd/ipo.xsd" }).validate(element);
        generator.getValidator().validate(element);
    }

    @Test
    public void shouldGenerateRandomInputMessage() throws IOException {
        SampleSoapXmlBuilder builder = new SampleSoapXmlBuilder(getClass().getResource("/xsd/StockQuoteService.wsdl"));
        Element input = builder.service("StockQuoteService").operation("GetLastTradePrice").randomInput("m");
        assertThat(input.tagName()).isEqualTo("TradePriceRequest");
        assertThat(input.find("tickerSymbol").single().text()).isNotEmpty();
    }

    @Test
    public void shouldGenerateRandomOutputMessage() throws IOException {
        SampleSoapXmlBuilder builder = new SampleSoapXmlBuilder(getClass().getResource("/xsd/StockQuoteService.wsdl"));
        Element output = builder.service("StockQuoteService").operation("GetLastTradePrice").randomOutput("m");
        assertThat(output.tagName()).isEqualTo("TradePrice");
        assertThat(output.find("price").single().text()).isNotEmpty();
        Float.parseFloat(output.find("price").single().text());
    }

    @Test
    public void shouldGetSoapAction() throws IOException {
        SampleSoapXmlBuilder builder = new SampleSoapXmlBuilder(Xml.readResource("/xsd/StockQuoteService.wsdl"));
        Element output = builder.getService().soapAction("http://example.com/GetLastTradePrice").randomOutput("m");
        assertThat(output.tagName()).isEqualTo("TradePrice");
        assertThat(output.find("price").single().text()).isNotEmpty();
        Float.parseFloat(output.find("price").single().text());
    }

    private static final Namespace SOAP = new Namespace("http://schemas.xmlsoap.org/soap/envelope/");

    @Test
    public void shouldRespondToSoapCall() throws IOException {
        SampleSoapXmlBuilder builder = new SampleSoapXmlBuilder(Xml.readResource("/xsd/StockQuoteService.wsdl"));
        SoapOperationDefinition operation = builder.getService().operation("GetLastTradePrice");
        Element input = SOAP.el("Envelope",
            SOAP.el("Header"),
            SOAP.el("Body", operation.randomInput("m")));
        String soapAction = "http://example.com/GetLastTradePrice";
        Element soapOutput = builder.processRequest(soapAction, input);

        assertThat(soapOutput.getName()).isEqualTo(SOAP.name("Envelope"));
        Element output = soapOutput.find(SOAP.name("Body"), "TradePrice").single();
        operation.getValidator().validate(output);
    }

    @Test
    public void shouldValidateSoapInput() throws IOException {
        final SampleSoapXmlBuilder builder = new SampleSoapXmlBuilder(Xml.readResource("/xsd/StockQuoteService.wsdl"));
        Element input = SOAP.el("Envelope",
            SOAP.el("Header"),
            SOAP.el("Body", Xml.el("wrongElement", "Some content")));
        String soapAction = "\"http://example.com/GetLastTradePrice\"";

        assertThatThrownBy(() -> builder.processRequest(soapAction, input))
            .isInstanceOf(SchemaValidationException.class)
            .hasMessageContaining("wrongElement");
    }

    @Test
    public void shouldGenerateCurrencyConversion() throws IOException {
        SampleSoapXmlBuilder builder = new SampleSoapXmlBuilder(
            new File("src/test/xml/wsdl-suite/currencyconvertor-multischema.wsdl").toURI().toURL());

        SoapOperationDefinition operation = builder.service("CurrencyConvertor").operation("ConversionRate");
        operation.getValidator().validate(operation.randomInput("msg"));
        operation.getValidator().validate(operation.randomOutput("msg"));
    }

    @Test
    public void shouldValidateSoapEnvelope() throws IOException {
        SampleSoapXmlBuilder builder = new SampleSoapXmlBuilder(Xml.readResource("/xsd/StockQuoteService.wsdl"));
        SoapOperationDefinition operation = builder.getService().operation("GetLastTradePrice");
        String soapAction = "http://example.com/GetLastTradePrice";
        Element input = operation.randomInput("msg");

        builder.processRequest(soapAction,
            SOAP.el("Envelope", SOAP.el("Header"), SOAP.el("Body", input)));
        assertThatThrownBy(() ->
                builder.processRequest(soapAction, SOAP.el("Envelope", SOAP.el("Header"), input)))
            .isInstanceOf(NonMatchingPathException.class)
            .hasMessageContaining("http://schemas.xmlsoap.org/soap/envelope/\":Body");
        assertThatThrownBy(() ->
                builder.processRequest(soapAction,
                    SOAP.el("Ennvelopp", SOAP.el("Body", input))))
            .isInstanceOf(NonMatchingPathException.class)
            .hasMessageContaining("http://schemas.xmlsoap.org/soap/envelope/\":Envelope");
    }

    private static final Namespace XS = new Namespace("http://www.w3.org/2001/XMLSchema", "xs");

    @Test
    public void shouldGenerateComplexTypeFromOtherSchema() throws IOException {
        Element rootSchema = XS.el("schema",
            Xml.attr("targetNamespace", "http://eaxy.org/response"),
            Xml.attr("elementFormDefault", "qualified"),
            new Namespace("http://eaxy.org/response", "tns"),
            new Namespace("http://eaxy.org/a", "a"));
        rootSchema.addAll(
            XS.el("import").attr("namespace", "http://eaxy.org/a"),
            XS.el("complexType", Xml.attr("name", "Response"),
                XS.el("sequence",
                    XS.el("element").name("Result").type("a:Result"))),
            XS.el("element").name("Response").type("tns:Response"));

        Element elementSchema = XS.el("schema",
            Xml.attr("targetNamespace", "http://eaxy.org/a"),
            Xml.attr("elementFormDefault", "qualified"),
            new Namespace("http://eaxy.org/name", "name"),
            new Namespace("http://eaxy.org/a", "tns"));
        elementSchema.addAll(
            XS.el("import").attr("namespace", "http://eaxy.org/name"),
            XS.el("complexType", Xml.attr("name", "Result"),
                XS.el("sequence",
                    XS.el("element").name("RequestId").type("xs:string").attr("minOccurs", "0"),
                    XS.el("element").name("FullNameEl").type("name:FullName"),
                    XS.el("element").name("UserId").type("xs:string"))),
            XS.el("element").name("Result").type("tns:Result"));

        Element nameSchema = XS.el("schema",
            Xml.attr("targetNamespace", "http://eaxy.org/name"),
            Xml.attr("elementFormDefault", "qualified"),
            new Namespace("http://eaxy.org/name", "tns"));
        nameSchema.addAll(
            XS.el("complexType", Xml.attr("name", "FullName"),
                XS.el("sequence",
                    XS.el("element").name("FirstName").type("xs:string").attr("minOccurs", "0"),
                    XS.el("element").name("LastName").type("xs:string"))),
            XS.el("element").name("FullName").type("tns:FullName"));


        SampleXmlBuilder builder = new SampleXmlBuilder("msg",
            new Document(rootSchema),
            Arrays.asList(new Document(elementSchema), new Document(nameSchema)));
        Element msg = builder.createRandomElement("Response");
        builder.getValidator().validate(msg);
    }
}
