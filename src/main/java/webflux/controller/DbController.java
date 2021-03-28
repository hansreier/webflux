package webflux.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import webflux.domain.Document;
import webflux.service.DocumentService;

@RestController
@RequestMapping("db")
public class DbController {

    private static final Logger LOG = LoggerFactory.getLogger(DbController.class);

    @Autowired
    private DocumentService documentService;

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
}
