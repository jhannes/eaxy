package org.eaxy.usage;

import org.eaxy.Document;
import org.eaxy.Xml;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Scanner;
import java.util.regex.Pattern;

public class FileDecodingTest {

    public static final String BAD = "ISO-8859-1";
    public static final String GOOD = "UTF-8";
    private static String oldSystemDefaultEncoding;

    @BeforeClass
    public static void saveDefaultEncoding() throws NoSuchFieldException, IllegalAccessException {
        oldSystemDefaultEncoding = System.getProperty("file.encoding");
    }

    @After
    public void restoreDefaultEncoding() throws NoSuchFieldException, IllegalAccessException {
        setSystemDefaultEncoding(oldSystemDefaultEncoding);
    }

    @Test
    public void shouldReproducePlainText() throws IOException, URISyntaxException {
        File file = new File(getClass().getResource("/testdocument.html").toURI());

        Document document = Xml.read(file);

        assertThat(document.toXML()).contains("Hello world");
    }

    @Test
    public void shouldReadUtf8ByDefault() throws IOException, URISyntaxException, NoSuchFieldException, IllegalAccessException {
        setSystemDefaultEncoding(GOOD);
        File file = new File(getClass().getResource("/testdocument.html").toURI());

        FileReader reader = new FileReader(file);
        String hit = new Scanner(reader).findWithinHorizon(Pattern.compile("some n.+n letters"), 1000);
        reader.close();

        assertThat(hit).contains("nørwægiån");
    }

    @Test
    public void shouldFailAtUTF8ByDefault() throws IOException, URISyntaxException, NoSuchFieldException, IllegalAccessException {
        setSystemDefaultEncoding(BAD);
        File file = new File(getClass().getResource("/testdocument.html").toURI());

        FileReader reader = new FileReader(file);
        String hit = new Scanner(reader).findWithinHorizon(Pattern.compile("some n.+n letters"), 1000);
        reader.close();

        assertThat(hit).contains("nÃ¸rwÃ¦giÃ¥n");
    }

    @Test
    public void shouldReadUtf8DespiteDefault() throws IOException, URISyntaxException, NoSuchFieldException, IllegalAccessException {
        setSystemDefaultEncoding(BAD);
        File file = new File(getClass().getResource("/testdocument.html").toURI());

        Document document = Xml.read(file);

        assertThat(document.toXML()).contains("nørwægiån");
    }


    private static void setSystemDefaultEncoding(String encoding) {
        System.setProperty("file.encoding", encoding);
        invalidateCachedFieldValue(Charset.class, "defaultCharset");
    }

    private static void invalidateCachedFieldValue(Class classObject, String fieldName) {
        try {
            Field field = classObject.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(null, null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
