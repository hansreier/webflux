package webflux.controllers;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;

import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import webflux.config.AppConfig;
import webflux.config.AppTestConfig;
import webflux.logback.LogbackSupport;
import webflux.logback.LogbackTestSupport;
import webflux.service.FileService;
import webflux.util.FileUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static webflux.controllers.PingController.TEST_MESSAGE;
import static webflux.controllers.PingController.USER_ID_PREFIX;
import static webflux.util.FileUtilities.*;

//@DirtiesContext ??? recreate context for every method
@AutoConfigureWebTestClient(timeout = "360000")
@ContextConfiguration(classes = AppConfig.class)
@Import(AppTestConfig.class)
@WebFluxTest
public class PingControllerTest {

    private static final Logger LOG = LoggerFactory.getLogger(PingControllerTest.class);

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private FileUtilities fileUtilities;


    @Test
    public void dummyTest() {
        assertThat(LogbackTestSupport.verifyLogbackConfig()).isEqualTo("OK");
        LogbackSupport.outputLogbackConfig();
        LOG.info("Test runs without breaking the Spring Context");
    }

    //Run this or localhost:8080/test/mono
    @Test
    public void testMonoEndpoint() {
        Flux<String> msg = webTestClient.get()
                .uri("/test/mono")
                .exchange()
                .expectStatus().isOk()
                .returnResult(String.class).getResponseBody()
                .log();
        //writes welcome message to console
        msg.subscribe(System.out::println);

        StepVerifier.create(msg)
                .expectNext(TEST_MESSAGE)
                .verifyComplete();
    }

    //Run this or localhost:8080/test/flux
    @Test
    public void testFluxEndpoint() {
        Flux<String> msg = webTestClient.get()
                .uri("/test/flux")
                .accept(MediaType.APPLICATION_ATOM_XML)
                .exchange()
                .expectStatus().isOk()
                .returnResult(String.class).getResponseBody()
                .log();

        StepVerifier.create(msg)
                .expectNext(TEST_MESSAGE)
                .verifyComplete();
    }

    //Run this on localhost:8080/test/flux
    @Disabled
    @Test
    public void testWebClientEndpoint() {
        LOG.info("test start");
        Flux<String> msg = webTestClient.get()
                .uri("/test/webclient")
                .accept(MediaType.APPLICATION_ATOM_XML)
                .exchange()
                .expectStatus().isOk()
                .returnResult(String.class).getResponseBody()
                .log();

        StepVerifier.create(msg)
                .expectNext(TEST_MESSAGE)
                .verifyComplete();

        LOG.info("test completed");
    }

    @Test
    public void testUserEndpoint() {
        String user = "Reier";
        Flux<String> msg =
                webTestClient.post().uri("/test/user").contentType(MediaType.valueOf(MediaType.APPLICATION_ATOM_XML_VALUE))
                        .body(Mono.just(user), String.class)
                        .exchange()
                        .expectStatus().isOk()
                        .returnResult(String.class).getResponseBody();
        String userId = msg.blockLast();
        LOG.info("UserId: {}", userId);
        assertThat(userId).startsWith(USER_ID_PREFIX + user);
    }

    @Test
    public void testUserEndpointEmptyUser() {
        String user = "";
        Flux<String> msg =
                webTestClient.post().uri("/test/user").contentType(MediaType.valueOf(MediaType.APPLICATION_ATOM_XML_VALUE))
                        .body(Mono.just(user), String.class)
                        .exchange()
                        .expectStatus().is5xxServerError()
                        .returnResult(String.class).getResponseBody();
        String errorJSON = msg.blockLast();
        LOG.info("errorJSON: {}", errorJSON);
        assertThat(errorJSON).contains("error");
    }

    @Test
    public void testFileUpload() {
        LOG.info("Test web client for file upload started");
        String fileName = "Betaling.txt";
        File file = new File(RESOURCE_DIR + fileName);
        Flux<String> text =
                webTestClient.post()
                        .uri("/test/upload")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .body(BodyInserters.fromMultipartData(fromFile(file)))
                        .exchange()
                        .expectStatus().isOk()
                        .returnResult(String.class).getResponseBody();
        String result = text.blockLast();
        LOG.info(result);
    }

    @Test
    public void testDBFileUpload() {
        LOG.info("Test web client for file upload started");
        String fileName = "Betaling.txt";
        //String fileName = "reier97.jpg";
        File file = new File(RESOURCE_DIR + fileName);
                webTestClient.post()
                        .uri("/test/uploadToDb")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .body(BodyInserters.fromMultipartData(fromFile(file)))
                        .exchange()
                        .expectStatus().isOk()
                        .returnResult(String.class).getResponseBody();
        LOG.info("completed");
    }

    @Test
    public void testDBUploadFileGenerated() throws Exception {
        LOG.info("Test web client for file upload started");
        String fileName = RESOURCE_DIR + "BetalingGen.txt";
        fileUtilities.generateFile(fileName, FILE_TEXT, 5000);
        LOG.info("File generated");
        File file = new File(fileName);
        LOG.info("Fil lengde:"+ file.length());
        webTestClient.post()
                .uri("/test/uploadToDb")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(fromFile(file)))
                .exchange()
                .expectStatus().isOk()
                .returnResult(String.class).getResponseBody();
        LOG.info("Completed");
    }


    @Test
    @Disabled
    public void testFileGenerated() throws Exception {
        int kBytes = 200000;
        LOG.info("Test web client for file upload started");
        String fileName = RESOURCE_DIR + "BetalingGen.txt";
        fileUtilities.generateFile(fileName, FILE_TEXT, kBytes);
        LOG.info("File generated");
    }

    @Test
    public void testFileGeneratedUpload() throws Exception {
        LOG.info("Test web client for file upload started");
        String fileName = RESOURCE_DIR + "BetalingGen.txt";
        fileUtilities.generateFile(fileName, FILE_TEXT, KBYTES);
        LOG.info("File generated");
        File file = new File(fileName);
        Flux<String> text =
                webTestClient.post()
                        .uri("/test/uploadToDisk")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .body(BodyInserters.fromMultipartData(fromFile(file)))
                        .exchange()
                        .expectStatus().isOk()
                        .returnResult(String.class).getResponseBody();
        String result = text.blockLast();
        LOG.info(result);
    }

    @Test
    public void testFileSaveUploadBinary() {
        LOG.info("Test web client for file upload started");
        String fileName = "reier97.jpg";
        File file = new File(RESOURCE_DIR + fileName);
        int fileSize = (int) file.length();
        Flux<Integer> response =
                webTestClient.post()
                        .uri("/test/uploadToDisk")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .body(BodyInserters.fromMultipartData(fromFile(file)))
                        .exchange()
                        .expectStatus().isOk()
                        .returnResult(Integer.class).getResponseBody();
        Integer bytesWritten = response.blockLast();
        assertThat(fileSize).isEqualTo(bytesWritten);
        LOG.info("File upload completed kBytes: {}", fileSize / THOUSAND);
    }

    @Test
    @Disabled
    public void testFileSaveGeneratedUpload() throws Exception {
        int kBytes = KBYTES;
        String fileName = RESOURCE_DIR + "BetalingGen.txt";
        fileUtilities.generateFile(fileName, FILE_TEXT, kBytes);
        File file = new File(fileName);
        Flux<Integer> response =
                webTestClient.post()
                        .uri("/test/uploadToDisk")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .body(BodyInserters.fromMultipartData(fromFile(file)))
                        .exchange()
                        .expectStatus().isOk()
                        .returnResult(Integer.class).getResponseBody();
        Integer bytesWritten = response.blockLast();
        assertThat(kBytes * THOUSAND).isEqualTo(bytesWritten);
        LOG.info("File upload completed kBytes: {}", kBytes);
    }

    @Test
    @Disabled
    public void testMultipeFilesGenerated() throws Exception {
        int kBytes = 10000;
        for (int i = 0; i < NO_FILES; i++) {
            LOG.info("Test web client for file upload started");
            String fileName = RESOURCE_DIR + "BetalingGen" + i + ".txt";
            fileUtilities.generateFile(fileName, FILES_TEXT + String.format("%5d", i) + " viser bytes ", kBytes);
        }
        LOG.info("{} Files generated", NO_FILES);
    }

    @Test
    @Disabled
    public void testFilesSavedUpload() {
        int kBytes = 10000;
        List<Flux<Integer>> responses = new ArrayList<>();
        for (int i = 0; i < NO_FILES; i++) {
            LOG.info("Test asyncrounous upload of multiple files");
            String fileName = RESOURCE_DIR + "BetalingGen" + i + ".txt";
            File file = new File(fileName);
            responses.add(
                    webTestClient.post()
                            .uri("/test/uploadToDisk")
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .body(BodyInserters.fromMultipartData(fromFile(file)))
                            .exchange()
                            .expectStatus().isOk()
                            .returnResult(Integer.class).getResponseBody());
            LOG.info("Reier: {}", responses.get(i).getClass());
        }

        responses.forEach((r) -> {
            Integer bytesWritten = r.blockLast();
            assertThat(kBytes * THOUSAND).isEqualTo(bytesWritten);
            LOG.info("File upload completed kBytes: {}", kBytes);
        });


        LOG.info("File upload completed kBytes: {}", kBytes);
    }

    private MultiValueMap<String, HttpEntity<?>> fromFile(File file) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new FileSystemResource(file));
        return builder.build();
    }





    /*
    @Test
    public void getUsers() {
        String user = "Reier";
        (webTestClient.post().uri("/users").contentType(MediaType.valueOf(MediaType.APPLICATION_XML_VALUE))
            .body(Mono.just(user), String.class)
            .exchange()
            .expectStatus().isCreated())
            .expectBody();
    }
   */

}

