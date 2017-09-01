package org.eaxy.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

public class IOUtils {

    public static String slurp(File file) throws IOException {
        if (file.getName().endsWith(".gz")) {
            try(InputStream inputStream = new GZIPInputStream(new FileInputStream(file))) {
                return slurp(inputStream);
            }
        } else {
            try (BufferedReader reader = new BufferedReader(new FileReader(file), 1024)) {
                return slurp(reader);
            }
        }
    }

    public static String slurp(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream), 1024)) {
            return slurp(reader);
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
