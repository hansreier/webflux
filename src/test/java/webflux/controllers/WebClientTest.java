package webflux.controllers;

import static webflux.controllers.PingController.TEST_MESSAGE;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static webflux.controllers.PingController.USER_ID_PREFIX;
import static webflux.util.FileUtilities.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import webflux.config.AppTestConfig;
import webflux.util.FileUtilities;
import webflux.util.Utilities;

import java.io.File;

    //@DirtiesContext ??? recreate context for every method
    @SpringBootTest
    @ContextConfiguration(classes = AppTestConfig.class)
    public class WebClientTest {

    private static final Logger LOG = LoggerFactory.getLogger(WebClientTest.class);

    private WebClient webClient;

    @Autowired
    private FileUtilities fileUtilities;

    @Autowired
    private Utilities utilities;

        @BeforeEach
        public void setWebClient() {
             webClient = WebClient.create("http://localhost:8080");
        }

        @Test
        @EnabledIfEnvironmentVariable(named ="spring.profiles.active", matches ="(ITEST)")
        public void testApplicationContext() {
            utilities.printBeans();
        }

        @Test
        @EnabledIfEnvironmentVariable(named ="spring.profiles.active", matches ="(ITEST)")
        public void testMonoEndpoint() {
            Mono<String> msg = webClient.get()
                    .uri("/test/mono")
                    .accept(MediaType.APPLICATION_XML)
                    .retrieve()
                    .bodyToMono(String.class).log();

            StepVerifier.create(msg)
                    .expectNext(TEST_MESSAGE)
                    .verifyComplete();
        }

        @Test
        @EnabledIfEnvironmentVariable(named ="spring.profiles.active", matches ="(ITEST)")
        public void testFluxEndpoint() {
            Flux<String> msg = webClient.get()
                    .uri("/test/flux")
                    .accept(MediaType.APPLICATION_ATOM_XML)
                    .retrieve()
                    .bodyToFlux(String.class);

            StepVerifier.create(msg)
                    .expectNext(TEST_MESSAGE)
                    .verifyComplete();
        }

        @Test
        @EnabledIfEnvironmentVariable(named ="spring.profiles.active", matches ="(ITEST)")
        public void testWebClient() {
            LOG.info("testWebClient started");
            Flux<String> msg = webClient.get()
                    .uri("/test/webclient")
                    .accept(MediaType.APPLICATION_ATOM_XML)
                    .retrieve().bodyToFlux(String.class);

            LOG.info("testWebClient call completed");
            String result = msg.blockLast();
            assertThat(msg.blockLast()).isEqualTo(TEST_MESSAGE);
            LOG.info("testWebClient completed");
        }

        @Test
        @EnabledIfEnvironmentVariable(named ="spring.profiles.active", matches ="(ITEST)")
        public void testUserEndpoint() {
            String user = "Reier";
            Mono<String> msg =
                    webClient.post()
                            .uri("/test/user").contentType(MediaType.valueOf(MediaType.APPLICATION_ATOM_XML_VALUE))
                            .accept(MediaType.APPLICATION_ATOM_XML)
                            .body(Mono.just(user), String.class)
                            .retrieve() //If error, the detailed cause cannot be read using .retrieve();
                            // .onStatus(httpStatus -> !HttpStatus.OK.equals(httpStatus), clientResponse -> Mono.empty())
                            .bodyToMono(String.class)
                    .doOnError(e ->LOG.error("REIER: " + e.getMessage()))
                    .onErrorReturn("error");
            LOG.info("Call done");
            String userId = msg.block();
            LOG.info("UserId: {}", userId);
            assertThat(userId).startsWith(USER_ID_PREFIX + user);
        }

        @Test
        @EnabledIfEnvironmentVariable(named ="spring.profiles.active", matches ="(ITEST)")
        public void testUserEndpoint2() {
            String user = "Reier";
            Mono<String> msg =
                    webClient.post()
                            .uri("/test/user").contentType(MediaType.valueOf(MediaType.APPLICATION_ATOM_XML_VALUE))
                            .accept(MediaType.APPLICATION_ATOM_XML)
                            .body(Mono.just(user), String.class)
                            .exchange()
                            .flatMap(clientResponse  -> clientResponse.bodyToMono(String.class));

            LOG.info("Call done");
        }

        @Test
        @EnabledIfEnvironmentVariable(named ="spring.profiles.active", matches ="(ITEST)")
        public void testFileUpload() {
            LOG.info("Test web client for file upload started");
            String fileName = "Betaling.txt";
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource(fileName).getFile());
            LOG.info("Fil opprettet ");
            Flux<String> msg =
                    webClient.post()
                            .uri("/test/upload")
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            //.accept(MediaType.APPLICATION_ATOM_XML)
                            .body(BodyInserters.fromMultipartData(fromFile(file)))
                            .retrieve()
                            .bodyToFlux(String.class);

            String result = msg.blockLast();
        }

        @Test
        @EnabledIfEnvironmentVariable(named ="spring.profiles.active", matches ="(ITEST)")
        public void testFileSaveGeneratedUpload() throws Exception {
            int kBytes = 100000; // 2000000
            LOG.info("Test web client for file upload started");
            String fileName = RESOURCE_DIR + "BetalingGen.txt";
            fileUtilities.generateFile(fileName, FILE_TEXT, kBytes);
            File file = new File(fileName);
            Flux<Integer> msg =
                    webClient.post()
                            .uri("/test/uploadToDisk")
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            //.accept(MediaType.APPLICATION_ATOM_XML)
                            .body(BodyInserters.fromMultipartData(fromFile(file)))
                            .retrieve()
                            .bodyToFlux(Integer.class);
            Integer bytesWritten = msg.blockLast();
            assertThat(kBytes * THOUSAND).isEqualTo(bytesWritten);
            LOG.info("File upload completed kBytes: {}", kBytes);
        }



        private MultiValueMap<String, HttpEntity<?>> fromFile(File file) {
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", new FileSystemResource(file));
            return builder.build();
        }




    }

