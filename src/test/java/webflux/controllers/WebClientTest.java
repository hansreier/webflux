package webflux.controllers;

import static webflux.controllers.PingController.TEST_MESSAGE;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import webflux.config.AppConfig;


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

        //@Disabled
        @Test
        public void testMonoEndpoint() {
            Mono<String> msg = webClient.get()
                    .uri("/test/mono")
                    .retrieve()
                    .bodyToMono(String.class).log();

            StepVerifier.create(msg)
                    .expectNext(TEST_MESSAGE)
                    .verifyComplete();
        }

        //@Disabled
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

        //@Disabled
        @Test
        public void testWebClient() {
            log.info("testWebClient started");
            Mono<String> msg = webClient.get()
                    .uri("/test/webclient")
                    .accept(MediaType.APPLICATION_ATOM_XML)
                    .retrieve()
                    .bodyToMono(String.class);

            StepVerifier.create(msg)
                    .expectNext(TEST_MESSAGE)
                    .verifyComplete();
            log.info("testWebClient completed");
        }
    }

