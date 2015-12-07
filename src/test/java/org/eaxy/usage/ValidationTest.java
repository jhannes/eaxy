package org.eaxy.usage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eaxy.Xml.text;
import static org.junit.Assert.fail;

import org.eaxy.Element;
import org.eaxy.Namespace;
import org.eaxy.Validator;
import org.eaxy.Xml;
import org.junit.Test;

public class ValidationTest {

    private final Validator validator = Xml.validatorFromResource("mailmessage.xsd");

    private final Namespace MSG_NS = new Namespace("http://eaxy.org/test/mailmessage", "msg");

    @Test
    public void shouldThrowErrorOnNonValidatingXml() {
        Element email = MSG_NS.el("message",
                MSG_NS.el("recipients",
                        MSG_NS.el("recipent",
                                text("mailto:johannes@brodwall.com")),
                        MSG_NS.el("recipent",
                                text("mailto:contact@brodwall.com"))));
        try {
            validator.validate(email);
            fail("Should throw validation exception");
        } catch (Exception e) {
            assertThat(e.getMessage()).contains("recipent")
                .contains("'{\"http://eaxy.org/test/mailmessage\":recipient}'");
        }
    }

    @Test
    public void shouldAcceptValidXml() {
        Namespace MSG_NS = new Namespace("http://eaxy.org/test/mailmessage", "msg");
        Element email = MSG_NS.el("message",
                MSG_NS.el("recipients",
                    MSG_NS.el("recipient",
                            MSG_NS.attr("type", "email"),
                            MSG_NS.attr("role", "cc"),
                            text("mailto:contact@brodwall.com"))));

        validator.validate(email);
    }

}
