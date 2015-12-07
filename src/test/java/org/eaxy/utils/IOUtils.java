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
        if (file.getName().endsWith(".gz")) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new GZIPInputStream(new FileInputStream(file))), 1024)) {
                return slurp(reader);
            }
        } else {
            try (BufferedReader reader = new BufferedReader(new FileReader(file), 1024)) {
                return slurp(reader);
            }
        }
    }

    private static String slurp(BufferedReader reader) throws IOException {
        StringBuilder result = new StringBuilder();
        int c;
        while ((c = reader.read()) != -1) {
            result.append((char)c);
        }
        return result.toString();
    }

}
