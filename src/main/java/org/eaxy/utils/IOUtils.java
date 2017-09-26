package org.eaxy.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPInputStream;

import org.eaxy.CanNeverHappenException;

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

    public static File toTmpFile(String content, String prefix, String postfix) throws IOException {
        String schemaHash = shaDigest(content);
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        File schemaFile = new File(tmpDir, prefix + schemaHash + postfix);
        if (!schemaFile.exists() || schemaFile.length() == 0) {
            try (FileWriter writer = new FileWriter(schemaFile)) {
                writer.write(content);
            }
        }
        return schemaFile;
    }

    static String shaDigest(String schemaSource) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA");
            return toHexString(md.digest(schemaSource.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new CanNeverHappenException("SHA is always supported", e);
        }
    }

    static String toHexString(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
    }

}
