package webflux.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

@Component
public class FileUtilities {

    private static final Logger LOG = LoggerFactory.getLogger(FileUtilities.class);

    public static final int THOUSAND = 1000;
    public static String RESOURCE_DIR = "src/test/resources/";
    public static String FILE_TEXT = "Velkommen til Skatteetaten. Na sendes regning via bankene med Peppol. Dette viser bytes ";
    public static String FILES_TEXT = "Velkommen til Skatteetaten. Na sendes regning via bankene med Peppol. ";

    public static void generateFile(String fileName, String lineText, int kBytes) throws IOException {
        LOG.info("Generate file:  {} kBytes: {} ", fileName, kBytes);
        int noLines = kBytes * 10;
        File file = new File(fileName);
        try (FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
            for (int i = 1; i <= noLines; i++) {
                String line = lineText + String.format("%8d", i) + "00\n";
                byte[] byteArray = line.getBytes();
                fileOutputStream.write(byteArray);
            }
            int bytesWritten = (int) file.length();
            int bytes = kBytes * THOUSAND;
            //Does not seems to be a correct test in Windows, but works in Linux
            /*
            if (bytesWritten != bytes) {
                throw
                        (new IOException("Bytes wanted: " + bytes + " generated: " + bytesWritten));
            } */
            LOG.info("File generated kBytes: {}", kBytes);
        } catch (IOException e) {
            LOG.error("Error writing to file: {} ", e.getMessage());
            throw new IOException(e);
        }
    }

    public static void generateFile(int kBytes) throws Exception {
        LOG.info("Generate single test file");
        String fileName = RESOURCE_DIR + "BetalingGen.txt";
        generateFile(fileName, FILE_TEXT, kBytes);
        LOG.info("File generated");
    }

    public static void generateFiles(int kBytes, int noFiles) throws Exception {
        LOG.info("Generate test files");
        for (int i = 0; i < noFiles; i++) {
            String fileName = RESOURCE_DIR + "BetalingGen" + i + ".txt";
            generateFile(fileName, FILES_TEXT + String.format("%5d", i) + " viser bytes ", kBytes);
        }
        LOG.info("{} Files generated", noFiles);
    }

    public static byte[] createDocBytes(String fileName) throws IOException {
        LOG.info("Creating document test");
        File file = new File(fileName);
        byte[] doc;
        try {
            return Files.readAllBytes(file.toPath());
        } catch (
                IOException e) {
            LOG.error("cannot read file {} {}", fileName, e.getMessage());
            throw new IOException(e);
        }
    }

}

