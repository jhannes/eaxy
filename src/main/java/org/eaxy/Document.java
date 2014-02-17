package org.eaxy;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class Document {

	private String version = "1.0";
	private String encoding = "UTF-8";
    private Element rootElement;
    private final List<String> dtds = new ArrayList<String>();
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    public Document(Element root) {
        rootElement = root;
    }

    public Document() {
    }

    public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public void setRootElement(Element rootElement) {
        this.rootElement = rootElement;
    }

    public Element getRootElement() {
        return rootElement;
    }

    public String toXML() {
        StringWriter result = new StringWriter();
        try {
            writeTo(result);
        } catch (IOException e) {
            throw new CanNeverHappenException("StringBuilder doesn't throw IOException", e);
        }
        return result.toString();
    }

    public void addDTD(String dtdString) {
        this.dtds.add(dtdString);
    }

    public void writeTo(Writer writer) throws IOException {
    	writer.append("<?xml version=\"");
    	writer.append(version);
        writer.append("\" encoding=\"");
        writer.append(encoding);
        writer.append("\"?>");
        writer.append(LINE_SEPARATOR);
        for (String dtd : dtds) {
            writer.append(dtd);
            writer.append(LINE_SEPARATOR);
        }
        rootElement.writeTo(writer);
    }

    public ElementSet find(Object... path) {
        return getRootElement().find(path);
    }

    public Element select(Object element) {
        return getRootElement().select(element);
    }

    public Document copy() {
        Document result = new Document(rootElement.copy());
        result.dtds.addAll(dtds);
        return result;
    }
}
