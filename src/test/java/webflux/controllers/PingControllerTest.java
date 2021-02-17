package webflux.controllers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import lombok.extern.slf4j.Slf4j;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import webflux.config.AppConfig;
import webflux.config.AppTestConfig;
import webflux.logback.LogbackSupport;
import webflux.logback.LogbackTestSupport;

import static webflux.controllers.PingController.TEST_MESSAGE;

//@ExtendWith(SpringExtension.class) //Required in this case?
//@DirtiesContext ??? recreate context for every method
@AutoConfigureWebTestClient
@ContextConfiguration(classes = AppConfig.class)
// @ActiveProfiles(SpringProfiles.TEST)
@Slf4j
@Import(AppTestConfig.class)
@WebFluxTest
public class PingControllerTest {

    @Autowired
    private WebTestClient webTestClient;


    @Test
    public void dummyTest() {
        Assertions.assertEquals("OK", LogbackTestSupport.verifyLogbackConfig());
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
                .accept(MediaType.APPLICATION_ATOM_XML)
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
    @Test
    public void testWebClientEndpoint() {
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

