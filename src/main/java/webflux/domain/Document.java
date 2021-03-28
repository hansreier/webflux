package webflux.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDateTime;

public class Document {
    @Id
    @Column("document_key")
    private Long documentKey;
    private byte[] document;
    @Column("document_type")
    private String documentType;
    private String comment;
    private LocalDateTime created;
    private LocalDateTime modified;

    public Long getDocumentKey() {
        return documentKey;
    }

    public void setDocumentKey(Long documentKey) {
        this.documentKey = documentKey;
    }

    public byte[] getDocument() {
        return document;
    }

    public void setDocument(byte[] document) {
        this.document = document;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public LocalDateTime getModified() {
        return modified;
    }

    public void setModified(LocalDateTime modified) {
        this.modified = modified;
    }
}

