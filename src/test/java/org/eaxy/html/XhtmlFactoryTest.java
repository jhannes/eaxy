package org.eaxy.html;

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.FileNotFoundException;

import org.junit.Test;

public class XhtmlFactoryTest {

    @Test
    public void shouldReadResource() throws Exception {
        new XhtmlFactory(this).create("testdocument.html");
    }

    @Test
    public void shouldThrowExceptionOnMissingResource() throws Exception {
        try {
            new XhtmlFactory(this).create("missing.html");
        } catch (FileNotFoundException e) {
            assertThat(e.getMessage()).contains("missing.html");
        }
    }


}
