package org.eaxy.experimental;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.eaxy.Document;
import org.eaxy.Element;
import org.eaxy.Validator;
import org.eaxy.Xml;
import org.eaxy.experimental.SampleSoapXmlBuilder.SoapOperationDefinition;
import org.junit.Test;

public class SampleXmlBuilderTest {

    @Test
    public void shouldGenerateMessageFromWsdl() throws IOException {
        SampleSoapXmlBuilder builder = new SampleSoapXmlBuilder("xsd/greath-reservation.wsdl");

        SoapOperationDefinition operation = builder.service("reservationService").operation("opCheckAvailability");
        Validator validator = new Validator(operation.getSchema());
        validator.validate(operation.randomInput("gh"));
    }

    @Test
    public void shouldGenerateAttributeValues() throws IOException {
        Document schemaDoc = Xml.readResource("/mailmessage.xsd");
        SampleXmlBuilder generator = new SampleXmlBuilder(schemaDoc, null);
        generator.setFull(true);
        Element element = generator.createRandomElement("message");
        assertThat(element.find("recipients", "recipient").first().attr("type")).isIn("email", "phone");
    }

    @Test
    public void shouldGenerateManyElementsWhenAppropriate() throws Exception {
        Document schemaDoc = Xml.readResource("/mailmessage.xsd");

        SampleXmlBuilder generator = new SampleXmlBuilder(schemaDoc, "msg");
        generator.setFull(true);
        Element element = generator.createRandomElement("message");
        System.out.println(element.toIndentedXML());
        new Validator(schemaDoc).validate(element);
        assertThat(element.find("recipients", "recipient").size()).isGreaterThan(1);
    }

    @Test
    public void shouldBuildFull() throws IOException {
        Document schemaDoc = Xml.readResource("/xsd/po.xsd");
        SampleXmlBuilder generator = new SampleXmlBuilder(getClass().getResource("/xsd/po.xsd"), "po");
        generator.setFull(true);
        Element el = generator.createRandomElement("purchaseOrder");
        assertThat(el.hasAttr("orderDate")).isTrue();
        assertThat(el.find("comment")).isNotEmpty();
        assertThat(el.find("items").check().find("item")).isNotEmpty();
        assertThat(el.find("shipTo").single().attr("country")).isEqualTo("US");
        new Validator(schemaDoc).validate(el);
    }

    @Test
    public void shouldBuildMinimal() throws IOException {
        Document schemaDoc = Xml.readResource("/xsd/po.xsd");
        SampleXmlBuilder generator = new SampleXmlBuilder(schemaDoc, null);
        generator.setMinimal(true);
        Element el = generator.createRandomElement("purchaseOrder");
        System.out.println(el.toIndentedXML());
        assertThat(el.hasAttr("orderDate")).isFalse();
        assertThat(el.find("comment")).isEmpty();
        assertThat(el.find("items").check().find("item")).isEmpty();
        new Validator(schemaDoc).validate(el);
    }

    @Test
    public void shouldGenerateFromMultipleFiles() throws IOException {
        SampleXmlBuilder generator = new SampleXmlBuilder(getClass().getResource("/xsd/ipo.xsd"), "ipo");

        Element element = generator.createRandomElement("purchaseOrder");
        System.out.println(element.toIndentedXML());
        new Validator(new String[] { "xsd/ipo.xsd", "xsd/address.xsd" }).validate(element);
    }

    @Test
    public void shouldGenerateRandomSoapMessage() throws IOException {
        SampleSoapXmlBuilder builder = new SampleSoapXmlBuilder(getClass().getResource("/xsd/StockQuoteService.wsdl"));
        Element input = builder.service("StockQuoteService").operation("GetLastTradePrice").randomInput("m");
        assertThat(input.tagName()).isEqualTo("TradePriceRequest");
        assertThat(input.find("tickerSymbol").single().text()).isNotEmpty();
        Element output = builder.service("StockQuoteService").operation("GetLastTradePrice").randomOutput("m");
        assertThat(output.tagName()).isEqualTo("TradePrice");
        assertThat(output.find("price").single().text()).isNotEmpty();
        Float.parseFloat(output.find("price").single().text());
    }

}
