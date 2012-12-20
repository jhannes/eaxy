package org.eaxy.html;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

public class XhtmlFactory {

    private final ClassLoader classLoader;

    public XhtmlFactory(Object classLoaderExampleObject) {
        this(classLoaderExampleObject.getClass());
    }

    public XhtmlFactory(Class<?> classLoaderExampleClass) {
        this(classLoaderExampleClass.getClassLoader());
    }

    public XhtmlFactory(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public Xhtml create(String resource) throws IOException {
        URL url = classLoader.getResource(resource);
        if (url == null) {
            throw new FileNotFoundException(resource);
        }
        return Xhtml.fromResource(url);
    }

}
