package webflux.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import webflux.domain.Document;
import webflux.service.DocumentService;
import webflux.service.FileService;


@RestController
@RequestMapping("db")
public class DbController {

    private static final Logger LOG = LoggerFactory.getLogger(DbController.class);

    @Autowired
    private DocumentService documentService;

    @Autowired
    private FileService fileService;

    @GetMapping("all")
    public Flux<Document> getAll() {
        LOG.info("Getting all documents");
        return this.documentService.getAllDocuments();
    }

    @GetMapping("{id}")
    public Mono<ResponseEntity<Document>> getDocumentById(@PathVariable long id) {
        LOG.info("Getting document id: {}", id);
        return this.documentService.getDocumentById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/create")
    public Mono<Document> createDocument(@RequestBody Mono<Document> documentMono) {
        LOG.info("Insert (update if existing id) document instance");
        return documentMono.flatMap(this.documentService::createDocument);
    }

    /*
    @PutMapping("{productId}")
    public Mono<DocumentMetadata> updateProduct(@PathVariable int productId,
        @RequestBody Mono<DocumentMetadata> productMono){
        return this.documentMetadataService.updateProduct(productId, productMono);
    }
    */

    @DeleteMapping("/delete/{id}")
    public Mono<Void> deleteDocument(@PathVariable int id) {
        LOG.info("Deleting document instance with id: {}", id);
        return this.documentService.deleteDocument(id);
    }

    @PostMapping(value = "/uploadToDb", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public Mono<Document> uploadToDb(@RequestPart("file") Mono<FilePart> filePartMono) {
        LOG.info("inside upload to db");
        return filePartMono.flatMap(filePart -> {
            Mono<Document> doc = fileService.uploadToMono(filePart);
            return doc.flatMap(this.documentService::createDocument);
           // return doc; skips db storage
        });
    }
}
