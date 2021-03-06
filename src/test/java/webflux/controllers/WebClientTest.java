package webflux.controllers;

import static webflux.controllers.PingController.TEST_MESSAGE;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static webflux.controllers.PingController.USER_ID_PREFIX;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import webflux.config.AppConfig;

import java.io.File;


//@DirtiesContext ??? recreate context for every method
    @ContextConfiguration(classes = AppConfig.class)
    //@ActiveProfiles(SpringProfiles.DEFAULT)
    @Slf4j
    public class WebClientTest {

        private WebClient webClient;

        @BeforeEach
        public void setWebClient() {
             webClient = WebClient.create("http://localhost:8080");
        }

        @Test
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
        public void testWebClient() {
            log.info("testWebClient started");
            Flux<String> msg = webClient.get()
                    .uri("/test/webclient")
                    .accept(MediaType.APPLICATION_XML)
                    .retrieve().bodyToFlux(String.class);

            log.info("testWebClient call completed");
            String result = msg.blockLast();
            assertThat(msg.blockLast()).isEqualTo(TEST_MESSAGE);
            log.info("testWebClient completed");
        }

        @Test
        //@EnabledIfEnvironmentVariable(named ="spring.profiles.active", matches ="(ITEST)")
        public void testUserEndpoint() {
            String user = "Reier";
            Mono<String> msg =
                    webClient.post()
                            .uri("/test/user").contentType(MediaType.valueOf(MediaType.APPLICATION_XML_VALUE))
                            .accept(MediaType.APPLICATION_XML)
                            .body(Mono.just(user), String.class)
                            .retrieve()
                            .bodyToMono(String.class).log();

            String userId = msg.block();
            log.info("UserId: {}", userId);
            assertThat(userId).startsWith(USER_ID_PREFIX + user);
        }

        @Test
        //@EnabledIfEnvironmentVariable(named ="spring.profiles.active", matches ="(ITEST)")
        public void testFileUpload() {
            log.info("Test web client for file upload started");
            String fileName = "Betaling.txt";
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource(fileName).getFile());
            log.info("Fil opprettet ");
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

        private MultiValueMap<String, HttpEntity<?>> fromFile(File file) {
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", new FileSystemResource(file));
            return builder.build();
        }




    }

