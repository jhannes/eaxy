package org.eaxy.experimental;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import org.eaxy.Document;
import org.eaxy.Xml;
import org.eaxy.experimental.XmlFormatter;
import org.junit.Test;

public class XmlFormatterTest {

    @Test
    public void shouldCanonizeSignatureElement() throws Exception {
        Document doc = Xml.read(new File("src/test/resources/envelopedSignature.xml"));

        String canonized = "<SignedInfo xmlns=\"http://www.w3.org/2000/09/xmldsig#\" xmlns:foo=\"urn:foo\"><CanonicalizationMethod Algorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments\"></CanonicalizationMethod><SignatureMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#dsa-sha1\"></SignatureMethod><Reference URI=\"\"><Transforms><Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\"></Transform></Transforms><DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"></DigestMethod><DigestValue>QlZCTMYoVmxjN3SL+jFv+qDR9rA=</DigestValue></Reference></SignedInfo>";

        String xml = XmlFormatter.canonical("http://www.w3.org/TR/2001/REC-xml-c14n-20010315")
            .toXML(doc.find("Signature", "SignedInfo").firstPath());
        assertThat(xml).isEqualTo(canonized);
    }

}
