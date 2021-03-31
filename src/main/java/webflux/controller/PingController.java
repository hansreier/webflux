package webflux.controller;

import org.junit.platform.commons.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import webflux.service.FileService;

import java.time.Duration;
import java.util.Random;

@RestController
@RequestMapping("/test")
public class PingController {

    private static final Logger LOG = LoggerFactory.getLogger(PingController.class);

    public static final String WELCOME = "Welcome ";
    public static final String TO = "to ";
    public static final String PEPPOL = "Peppol ";
    public static final String INTEGRASJON = "Integrasjon ";
    public static final String PAYMENT = "Payment ";
    public static final String TEST_MESSAGE = WELCOME + TO + PEPPOL + PAYMENT + INTEGRASJON;
    public static final String USER_ID_PREFIX = "skatt";
    public static final int RANDOM_UPPER_LIMIT = 100;

    @GetMapping(path = "/mono")
    public Mono<String> getMono() {
        return Mono.just(TEST_MESSAGE);
    }

    @GetMapping(path = "/flux", produces = MediaType.APPLICATION_ATOM_XML_VALUE)
    public Flux<String> getFlux() {
        return Flux.just(WELCOME, TO, PEPPOL, PAYMENT, INTEGRASJON)
                .delayElements(Duration.ofSeconds(1)).log();
    }

    /*
     *   Uses webclient to call a test service
     *   Obviously, this call only works locally
     */
    @GetMapping(path = "/webclient")
    public Flux<String> getWebclient() {
        LOG.info("Flux REST service using webclient");
        WebClient webClient = WebClient.create("http://localhost:8080");
        LOG.info("Webclient created");
        Flux<String> msg = webClient.get()
                .uri("localhost:8080/test/flux")
                .accept(MediaType.APPLICATION_ATOM_XML)
                .retrieve()
                .bodyToFlux(String.class)
                .doOnError(throwable -> LOG.error("Failure", throwable))
                .onErrorReturn("Webclient cannot be called");
        LOG.info("Flux Rest service called and result returned");
        return msg;
    }

    @PostMapping("/user")
    public Mono<String> saveUser(@RequestBody String user) {
        LOG.info("user Reier {}", user);
        if (StringUtils.isBlank(user)) {
            LOG.info("Empty user Reier");
            throw new RuntimeException("empty user");
        }
        if (user.startsWith("Hans")) {
            LOG.info("Hans is not my name");
            throw new RuntimeException("Do not call me Hans");
        }
        Mono<String> msg;
        LOG.debug("Inside saveUser");
        Random rand = new Random();
        int uid = rand.nextInt(RANDOM_UPPER_LIMIT);
        String userId = USER_ID_PREFIX + user + uid;
        msg = Mono.just(userId);
        // Specific error handling can be added, not required.
        // try {
        // ...
        // .doOnError(throwable -> LOG.error("Failure", throwable))
        // .onErrorResume(throwable -> Mono.error(throwable));
        // } catch (Exception e) {
        //     LOG.error("Failure",e);
        //    msg = Mono.error(e);
        // }
        return msg;
    }
}


