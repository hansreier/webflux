package webflux.controllers;

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
        LOG.info("Getting all document metadata");
        return this.documentService.getAllDocuments();
    }

    @GetMapping("{id}")
    public Mono<ResponseEntity<Document>> getDocumentMetadataById(@PathVariable long id) {
        LOG.info("Getting document metadata id: {}", id);
        return this.documentService.getDocumentById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/create")
    public Mono<Document> createDocumentMetadata(@RequestBody Mono<Document> documenMetadataMono) {
        LOG.info("Insert (update if existing id) metadata instance");
        return documenMetadataMono.flatMap(this.documentService::createDocument);
    }

    /*
    @PutMapping("{productId}")
    public Mono<DocumentMetadata> updateProduct(@PathVariable int productId,
        @RequestBody Mono<DocumentMetadata> productMono){
        return this.documentMetadataService.updateProduct(productId, productMono);
    }
    */

    @DeleteMapping("/delete/{id}")
    public Mono<Void> deleteDocumentMetadata(@PathVariable int id) {
        LOG.info("Deleting document metadata instance with id: {}", id);
        return this.documentService.deleteDocument(id);
    }
}
