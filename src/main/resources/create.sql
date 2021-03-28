CREATE TABLE IF NOT EXISTS document
(
    document_key BIGSERIAL PRIMARY KEY,
    document_type VARCHAR(80),
    created TIMESTAMP,
    modified TIMESTAMP,
    comment VARCHAR(200),
    document BYTEA
);



