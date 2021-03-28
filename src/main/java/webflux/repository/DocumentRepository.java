package webflux.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import webflux.domain.Document;

@Repository
public interface DocumentRepository extends ReactiveCrudRepository<Document, Long> {
}
