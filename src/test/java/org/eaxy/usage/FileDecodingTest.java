package org.eaxy.usage;

import org.eaxy.Document;
import org.eaxy.Xml;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Scanner;

public class FileDecodingTest {

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
    public void shouldReadUtf8DespiteDefault() throws IOException, URISyntaxException, NoSuchFieldException, IllegalAccessException {
        setSystemDefaultEncoding("ISO-8859-1");
        File file = new File(getClass().getResource("/testdocument.html").toURI());

        String reference = slurp(file);
        Document document = Xml.read(file);

        assertThat(reference).contains("nÃ¸rwÃ¦giÃ¥n");
        assertThat(document.toXML()).contains("nørwægiån");
    }

    private static void setSystemDefaultEncoding(String encoding) {
        System.setProperty("file.encoding", encoding);
        invalidateFieldCache(Charset.class, "defaultCharset");
    }

    private static void invalidateFieldCache(Class classObject, String fieldName) {
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


    private String slurp(File file) throws FileNotFoundException {
        Scanner scan = new Scanner(file);
        scan.useDelimiter("\\Z");
        return scan.next();
    }

}
