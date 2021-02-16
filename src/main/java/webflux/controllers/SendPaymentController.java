package webflux.controllers;

import java.nio.charset.StandardCharsets;
import java.util.Random;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

//MediaType.MULTIPART_FORM_DATA ?????   MULTIPPART_FILE
// https://dzone.com/articles/step-by-step-procedure-of-spring-webflux-multipart
// TODO Should be simplifie
@RestController
@RequestMapping("/utbetaling")
@Slf4j
public class SendPaymentController {

    public static final int RANDOM_UPPER_LIMIT = 100;

    @PostMapping(value = "/utbetaling", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public Flux<String> sendPayment(@RequestPart("files) ") Flux<FilePart> filePartFlux) {
        Flux<String> f;
        f = filePartFlux.flatMap((FilePart filePart) ->
                //filePart.content().map(dataBuffer -> {
                filePart.content().map((DataBuffer dataBuffer) -> {
                    //Fill bytes array by reading data fra databuffer
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    //Free databuffer
                    DataBufferUtils.release(dataBuffer);
                    //convert to string, each String is a single line of the file
                    return new String(bytes, StandardCharsets.UTF_8);
                }));
        return f;
    }

    @PostMapping("/users")
    public Mono<String> saveUser(@RequestBody String user) {
        log.info("Inside saveUser");
        Random rand = new Random();
        int uid = rand.nextInt(RANDOM_UPPER_LIMIT);
        String userId = user + uid;
        Mono.just(userId);
        return Mono.just(userId);
    }



  /*
    @PostMapping("/search/id")
    public Flux<User> fetchUsersByIds(@RequestBody List<Integer> ids) {
        return userService.fetchUsers(ids);
    }
    @PostMapping(path = "/pets", consumes = "application/json")

    @PostMapping(path = "/input", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<?>> input(@RequestParam("file") MultipartFile file) throws IOException {
        final ByteBuffer byteBuffer = ByteBuffer.wrap(file.getBytes());

        //[..]

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    */
}

