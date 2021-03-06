package webflux.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicInteger;

/*********************'
 * https://www.baeldung.com/java-nio2-async-file-channel
 * https://github.com/entzik/reactive-spring-boot-examples/blob/master/src/main/java/com/
 *    thekirschners/springbootsamples/reactiveupload/ReactiveUploadResource.java
 */

@Service
public class FileService {

    private static final Logger LOG = LoggerFactory.getLogger(FileService.class);

    private static final int LINE_FEED = 10;
    private static final int HUNDRED = 100;
    private final String baseDir;

    public FileService(@Value("${app.files}") String baseDir) {
        this.baseDir = baseDir;
    }

    public Flux<String> upload(FilePart x) {
        Flux<String> dBuffer;
        dBuffer = x.content().map(dataBuffer -> {
            final int size = dataBuffer.readableByteCount();
            byte[] bytes = new byte[size];
            dataBuffer.read(bytes);
            DataBufferUtils.release(dataBuffer);
            String read = new String(bytes, StandardCharsets.UTF_8);
            if (LOG.isTraceEnabled()) {
                LOG.trace("bytes read {} {}", size, read);
            } else {
                if (LOG.isDebugEnabled()) {
                    int pos = read.indexOf(LINE_FEED) + 1;
                    if (pos == HUNDRED - 1) {
                        pos = 0;
                    }
                    LOG.debug("bytes read {} {}", size, read.substring(pos, pos + HUNDRED - 2));
                }
            }
            return read;
        });
        return dBuffer;
    }

    public Flux<Integer> uploadToDisk(FilePart x) {
        Flux<Integer> dData;
        Flux<String> dBuffer;
        try {
            LOG.info("Upload to disk service");
            Path path = createFilePath(baseDir, x.filename());
            AsynchronousFileChannel channel = AsynchronousFileChannel
                    .open(path, StandardOpenOption.WRITE);
            AtomicInteger fileWriteOffset = new AtomicInteger(0);

            dData = x.content().map(dataBuffer -> {
                final int size = dataBuffer.readableByteCount();
                byte[] bytes = new byte[size];
                dataBuffer.read(bytes);
                DataBufferUtils.release(dataBuffer);
                String read = new String(bytes, StandardCharsets.UTF_8);

                LOG.debug("bytes read {} {}", size, strip(read));

                // create a file channel compatible byte buffer
                final ByteBuffer byteBuffer = ByteBuffer.allocate(size);
                byteBuffer.put(bytes);
                byteBuffer.flip();

                // get the current write offset and increment by the buffer size
                final int filePartOffset = fileWriteOffset.getAndAdd(size);
                LOG.debug("processing file part at offset {}", filePartOffset);
                // write the buffer to disk
                channel.write(byteBuffer, filePartOffset);
                return filePartOffset + size;
            });
        } catch (Exception e) {
            return Flux.error(e);
        }
        LOG.info("upload to disk service completed");
        return dData;
    }

    public boolean isPureAscii(String v) {
        return v.matches("\\A\\p{ASCII}*\\z");
    }

    public String strip(String v) {
        return v.replaceAll(
                "[^a-zA-Z0-9._!;:=¤%()|/\\u00C6\\u00E6\\u00D8\\u00F8\\u00C5\\u00E5@#\\]?\\[$\\\\ ]", "*");
    }

    /*
     *   Create file (and required directory) given file name with full path
     */
    private Path createFilePath(String path, String fileName) throws IOException {
        String filePathAndName = path + File.separator + fileName;
        LOG.info("File name: {}", filePathAndName);
        File out = new File(filePathAndName);
        boolean created;
        if (!out.exists()) {
            final boolean dirCreated = out.getParentFile().mkdirs();
            if (dirCreated) {
                LOG.info("Directory for storing files does not exist, is created: {}", path);
            }
            created = out.createNewFile();
            if (!created) {
                throw new IOException("File is not created");
            }
        }
        LOG.info("File is created on disk");
        return out.toPath();
    }
}

