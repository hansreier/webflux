package webflux.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import webflux.service.DocumentService;
import webflux.service.FileService;

@RestController
@RequestMapping("/test")
public class FileController {

    private static final Logger LOG = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileService fileService;

    //upload and return file content in Flux<String>
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public Flux<String> upload(@RequestPart("file") Mono<FilePart> filePartMono) {
        Flux<String> result = filePartMono.flatMapMany(fileService::upload);
        LOG.info("file upload to server completed");
        return result;
    }

    //Upload and save to disk, bytes written is returned in Flux<Integer>
    @PostMapping(value = "/uploadToDisk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public Flux<Integer> uploadToDisk(@RequestPart("file") Mono<FilePart> filePartMono) {
        LOG.info("inside upload to disk");
        Flux<Integer> result = filePartMono.flatMapMany(fileService::uploadToDisk);
        LOG.info("file upload to disk on server completed");
        return result;
    }
}
