package webflux.controllers;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.ActiveProfiles;
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

import java.io.File;

import static webflux.controllers.PingController.TEST_MESSAGE;
import static webflux.controllers.PingController.USER_ID_PREFIX;

//@DirtiesContext ??? recreate context for every method
@AutoConfigureWebTestClient(timeout = "36000")
@ContextConfiguration(classes = AppConfig.class)
@Slf4j
@Import(AppTestConfig.class)
@WebFluxTest
public class PingControllerTest {

    @Autowired
    private WebTestClient webTestClient;


    @Test
    public void dummyTest() {
        assertThat(LogbackTestSupport.verifyLogbackConfig()).isEqualTo("OK");
        LogbackSupport.outputLogbackConfig();
        log.info("Test runs without breaking the Spring Context");
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
                .accept(MediaType.APPLICATION_XML)
                //.accept(MediaType.APPLICATION_STREAM_JSON)
                .exchange()
                .expectStatus().isOk()
                .returnResult(String.class).getResponseBody()
                .log();

        StepVerifier.create(msg)
                .expectNext(TEST_MESSAGE)
                .verifyComplete();
    }

    //Run this or localhost:8080/test/flux
    //@Disabled
    @Test
    public void testWebClientEndpoint() {
        log.info("test start");
        Flux<String> msg = webTestClient.get()
                .uri("/test/webclient")
                .accept(MediaType.APPLICATION_XML)
                .exchange()
                .expectStatus().isOk()
                .returnResult(String.class).getResponseBody()
                .log();

        StepVerifier.create(msg)
                .expectNext(TEST_MESSAGE)
                .verifyComplete();

                log.info("test completed");
    }

    @Test
    public void testUserEndpoint() {
        String user = "Reier";
        Flux<String> msg =
                webTestClient.post().uri("/test/user").contentType(MediaType.valueOf(MediaType.APPLICATION_XML_VALUE))
                        .body(Mono.just(user), String.class)
                        .exchange()
                        .expectStatus().isOk()
                        .returnResult(String.class).getResponseBody();
        String userId = msg.blockLast();
        log.info("UserId: {}", userId);
        assertThat(userId).startsWith(USER_ID_PREFIX + user);
    }

    @Test
    public void testFileUpload() {
        log.info("Test web client for file upload started");
        String fileName = "Betaling.txt";
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        Flux<String> text =
                webTestClient.post()
                        .uri("/test/upload")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .body(BodyInserters.fromMultipartData(fromFile(file)))
                        .exchange()
                        .expectStatus().isOk()
                        .returnResult(String.class).getResponseBody();
         String result = text.blockFirst();
         log.info(result);
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

