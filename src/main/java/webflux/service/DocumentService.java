package webflux.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import webflux.domain.Document;
import webflux.repository.DocumentRepository;

import java.time.LocalDateTime;

@Service
@Transactional
public class DocumentService {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentService.class);

    @Autowired
    private DocumentRepository repository;

    public Flux<Document> getAllDocuments() {
        return this.repository.findAll();
    }

    public Mono<Document> getDocumentById(Long documentKey) {
        return this.repository.findById(documentKey);
    }

    public Mono<Document> createDocument(final Document document) {
        LOG.info("Save document in database");
        return this.repository.save(document);
    }

    public Mono<Document> updateDocument(long documentId, final Mono<Document> documentMono) {
        return this.repository.findById(documentId)
            .flatMap(p -> documentMono.map(u -> {
                p.setComment(u.getComment());
                p.setDocumentType((u.getDocumentType()));
                p.setModified(LocalDateTime.now());
                return p;
            }))
            .flatMap(p -> this.repository.save(p));
    }

    public Mono<Void> deleteDocument(final long id) {
        //LOG.info("Inside deleteDocument service");
        return this.repository.deleteById(id);
    }

}

