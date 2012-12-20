package org.eaxy.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

public class IOUtils {

	public static String slurp(File file) throws IOException {
	    BufferedReader reader = new BufferedReader(new FileReader(file), 1024);
	    StringBuilder result = new StringBuilder();
	    try {
			if (file.getName().endsWith(".gz")) {
				reader.close();
			    reader = new BufferedReader(
			            new InputStreamReader(new GZIPInputStream(new FileInputStream(file))), 1024);
			}
			int c;
			while ((c = reader.read()) != -1) {
			    result.append((char)c);
			}
		} finally {
			reader.close();
		}
	    return result.toString();
	}

}
