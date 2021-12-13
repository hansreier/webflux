package webflux.controller;

// Publish on/subscribe on.
// https://zoltanaltfatter.com/2018/08/26/subscribeOn-publishOn-in-Reactor/
// https://www.vinsguru.com/reactor-schedulers-publishon-vs-subscribeon/
//https://spring.io/blog/2019/12/13/flight-of-the-flux-3-hopping-threads-and-schedulers
//https://spring.io/blog/2019/12/13/flight-of-the-flux-3-hopping-threads-and-schedulers
//https://www.baeldung.com/spring-webflux-concurrency e.g separate thread-bool for webclient.
//https://developer.okta.com/blog/2021/08/13/reactive-java
//How to call blocking code from reactive...
//https://github.com/reactor/reactor-core/issues/1756
//https://stackoverflow.com/questions/59566982/what-happens-in-webflux-if-a-single-synchronous-call-is-made
//https://www.codingame.com/playgrounds/929/reactive-programming-with-reactor-3/BlockingToReactive
//https://betterprogramming.pub/how-to-avoid-blocking-in-reactive-java-757ec7024676
//https://projectreactor.io/docs/core/release/reference/#faq.wrap-blocking

//From blocking to reactive.
//https://www.codingame.com/playgrounds/929/reactive-programming-with-reactor-3/ReactiveToBlocking
//Can use timeout with block...
//Spring har også tast sceduler og @schedule
//https://itnext.io/how-to-make-legacy-code-reactive-2debcb3d0a40

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
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
import webflux.config.AppTestConfig;
import webflux.domain.Document;
import webflux.logback.LogbackSupport;
import webflux.logback.LogbackTestSupport;
import webflux.util.FileUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static webflux.controller.PingController.TEST_MESSAGE;
import static webflux.controller.PingController.USER_ID_PREFIX;
import static webflux.util.FileUtilities.*;

//@DirtiesContext ??? recreate context for every method
@AutoConfigureWebTestClient(timeout = "360000")
@ContextConfiguration(classes = AppTestConfig.class)
@WebFluxTest(controllers = PingController.class)
public class PingControllerTest {

    private static final Logger LOG = LoggerFactory.getLogger(PingControllerTest.class);

    private static final int NO_FILES = 5;
    private static final int KBYTES = 50;
    private static final int KBYTES2 = 100;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeAll
    private static void startup() throws Exception { generateFile(KBYTES);
      //  generateFiles(KBYTES2, NO_FILES);
    }

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

    @Disabled
    @Test
    public void testWebClientBlockingEndpoint() {
        LOG.info("test start");
        Flux<String> msg = webTestClient.get()
                .uri("/test/webclientBlocking")
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

    @Disabled
    @Test
    public void testWebClientBlockingEndpoint2() {
        LOG.info("test start");
        Flux<String> msg = webTestClient.get()
                .uri("/test/webclientBlocking2")
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
                        .uri("/db/upload")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .body(BodyInserters.fromMultipartData(fromFile(file)))
                        .exchange()
                        .expectStatus().isOk()
                        .returnResult(String.class).getResponseBody();
        LOG.info("completed");
    }

    @Test
    //Funker bare på små størrelser, så noe er feil v100 kBytes er greit
    public void testDBUploadFileGenerated() throws Exception {
        LOG.info("Test web client for file upload started");
        String fileName = RESOURCE_DIR + "BetalingGen.txt";
        FileUtilities.generateFile(fileName, FILE_TEXT, 20);
        LOG.info("File generated");
        File file = new File(fileName);
        LOG.info("Fil lengde:"+ file.length());
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
    @Disabled
    public void testFileGenerated() throws Exception {
        int kBytes = 200000;
        LOG.info("Test web client for file upload started");
        String fileName = RESOURCE_DIR + "BetalingGen.txt";
        FileUtilities.generateFile(fileName, FILE_TEXT, kBytes);
        LOG.info("File generated");
    }

    @Test
    public void testFileGeneratedUpload() throws Exception {
        LOG.info("Test web client for file upload started");
        String fileName = RESOURCE_DIR + "BetalingGen.txt";
        FileUtilities.generateFile(fileName, FILE_TEXT, KBYTES);
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
        FileUtilities.generateFile(fileName, FILE_TEXT, kBytes);
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
            FileUtilities.generateFile(fileName, FILES_TEXT + String.format("%5d", i) + " viser bytes ", kBytes);
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

    @Test
    public void testSendXML() {
        LOG.info("Test web client send XML");
        Document savedDocument;
        Document doc = new Document();
        doc.setDocumentKey(1L);
        Flux<Document> response =
                webTestClient.post()
                        .uri("/test/xml")
                       // .contentType(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .accept(MediaType.APPLICATION_XML)
                        .body(Mono.just(doc), Document.class)
                        .exchange()
                        .expectStatus().isOk()
                        .returnResult(Document.class).getResponseBody();
        Document result = response.blockLast();
        assertThat(result).isNotNull();
        LOG.info("Document read key: {} comment: {}",result.getDocumentKey(), result.getComment());
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

