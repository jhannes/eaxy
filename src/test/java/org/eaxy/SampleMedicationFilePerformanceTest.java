package org.eaxy;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.junit.Test;

public class SampleMedicationFilePerformanceTest {

    public static void download(URL url, File file, File tempDir) throws IOException {
        File dir = file.getParentFile();
        FileUtils.forceMkdir(dir);
        if (!file.exists() || file.length() == 0) {
            FileUtils.forceMkdir(tempDir);
            File tempFile = new File(tempDir, file.getName());
            System.out.println("Downloading "+ url + " to temporary file " + tempDir);
            FileUtils.copyURLToFile(url, tempFile);
            try {
                tempFile.renameTo(file);
            } finally {
                tempFile.delete();
            }
        }
        if (!file.isFile() || !file.canRead()) {
            throw new RuntimeException("Failed to create readable file " + url);
        }
    }

    @Test
    public void shouldFindElementsInHugeFile() throws Exception {
        System.out.println("downloading");
        File medicationsZip = new File("target/data/fest251.zip");
        download(new URL("https://www.legemiddelsok.no/_layouts/15/FESTmelding/fest251.zip"),
                medicationsZip, new File("target/data/tmp/"));

        System.out.println("Scanning");
        List<String> ritalinSubstitutes = new ArrayList<>();

        try (ZipFile zipFile = new ZipFile(medicationsZip)) {
            for (Element medication : ElementFilters.create("KatLegemiddelpakning", "OppfLegemiddelpakning", "Legemiddelpakning")
                    .iterate(new InputStreamReader(new BOMInputStream(zipFile.getInputStream(zipFile.getEntry("fest251.xml")))))) {
                ElementSet atcCode = medication.find("Atc");
                if (atcCode.isPresent() && atcCode.first().attr("V").equals("N06BA04")) {
                    ritalinSubstitutes.add(medication.find("NavnFormStyrke").first().text());
                }
            }
        }

        System.out.println("checking");
        assertThat(ritalinSubstitutes).contains("Medikinet Tab 5 mg", "Concerta DEPOTtab 18 mg");
    }


}
