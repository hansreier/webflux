package webflux.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Component
public class FileUtilities {

    private static final Logger LOG = LoggerFactory.getLogger(FileUtilities.class);

    public static final int THOUSAND = 1000;
    public static final int KBYTES = 500;

    public static String RESOURCE_DIR = "src/test/resources/";
    public static String FILE_TEXT = "Velkommen til Skatteetaten. Nå sendes regning via bankene med Peppol. Dette viser bytes ";
    public static String FILES_TEXT = "Velkommen til Skatteetaten. Nå sendes regning via bankene med Peppol. ";
    public static int NO_FILES = 5;

    public void generateFile(String fileName, String lineText, int kBytes) throws Exception {
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
            if (bytesWritten != bytes) {
                throw
                        (new IOException("Bytes wanted: " + bytes + " generated: " + bytesWritten));
            }
            LOG.info("File generated kBytes: {}",kBytes);
        } catch (Exception e) {
            LOG.error("Error writing to file: {} ", e.getMessage());
            throw new Exception(e);
        }
    }
}

