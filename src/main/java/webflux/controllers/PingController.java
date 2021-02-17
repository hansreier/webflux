package webflux.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
@RequestMapping("/test")
@Slf4j
public class PingController {
    public static final String WELCOME = "Welcome ";
    public static final String TO = "to ";
    public static final String WEBFLUX = "WebFlux ";
    public static final String DEMO = "Demo ";
    public static final String PROGRAM = "Program ";
    public static final String TEST_MESSAGE = WELCOME + TO + WEBFLUX + DEMO + PROGRAM;

    @GetMapping(path = "/mono")
    public Mono<String> getMono() {
        log.info("Mono REST service");
        return Mono.just(TEST_MESSAGE);
    }

    @GetMapping(path = "/flux", produces = MediaType.APPLICATION_ATOM_XML_VALUE)
    public Flux<String> getFlux() {
        log.info("Flux REST service");
        return Flux.just(WELCOME, TO, WEBFLUX, DEMO, PROGRAM)
                .delayElements(Duration.ofSeconds(1)).log();
    }

    @GetMapping(path = "/webclient")
    public Mono<String> getWebclient() {
        WebClient webClient = WebClient.create();
        Flux<String> msg = webClient.get()
                .uri("/test/flux")
                .accept(MediaType.APPLICATION_ATOM_XML)
                .retrieve()
                .bodyToFlux(String.class);
        return Mono.from(msg);
    }
}

