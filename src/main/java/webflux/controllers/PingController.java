package webflux.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Random;

import static webflux.controllers.SendPaymentController.RANDOM_UPPER_LIMIT;

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
    public static final String USER_ID_PREFIX ="ciber";

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
    public Flux<String> getWebclient() {
        log.info("Flux REST service using webclient");

        WebClient webClient = WebClient.create("http://localhost:8080");
        log.info("webClient created");
        Flux<String> msg = webClient.get()
                .uri("/test/flux")
                .accept(MediaType.APPLICATION_ATOM_XML)
                .retrieve()
                .bodyToFlux(String.class);
        log.info("Flux REST service called and result returned");
        return msg;
    }

    @PostMapping("/user")
    public Mono<String> saveUser(@RequestBody String user) {
        log.info("Inside saveUser");
        Random rand = new Random();
        int uid = rand.nextInt(RANDOM_UPPER_LIMIT);
        String userId = USER_ID_PREFIX + user + uid;
        return Mono.just(userId);
    }
    //https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#webflux-multipart
    //https://dzone.com/articles/step-by-step-procedure-of-spring-webflux-multipart
    //Kan også blande fildata og andre data som JSON, dette er et veldig aktuelt scenario
    //@PostMapping("/")
    //public String handle(@RequestPart("meta-data") Part metadata,
    //    @RequestPart("file-data") FilePart file) {
    // Dette under stemmer med begge tutorials men likevel returneres  400.

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public Flux<String> upload( @Validated @RequestPart("file) ") Mono<FilePart> filePartMono) {
        log.info("Starting file upload");
        Flux<String> result = filePartMono.flatMapMany(x -> x.content()
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return new String(bytes, StandardCharsets.UTF_8);
                }));

        //try {
        //    saveFile(result);
        //} catch (IOException e) {
        //    e.printStackTrace();
        //}

        log.info("file upload to server completed");
        return result;
    }
    /*
    //Save file on disk
    //https://www.vinsguru.com/reactor-flux-file-reading/
    // instead of try with resources
    // This is reactive correct code, but not described correct import for write and close
    private void saveFile(Flux<String> contents) throws IOException {
        log.info("Saving file");
        Path opPath = Paths.get("/tmp/betaling.txt");
        BufferedWriter bw = Files.newBufferedWriter(opPath, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        contents
            .subscribe(s -> write(bw, s),
                (e) -> close(bw),  // close file if error / oncomplete
                () -> close(bw)
            );

    }
    */

}

