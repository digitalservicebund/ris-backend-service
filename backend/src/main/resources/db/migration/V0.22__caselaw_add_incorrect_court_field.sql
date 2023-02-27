CREATE TABLE IF NOT EXISTS
  incorrect_court (
    id BIGSERIAL not null primary key,
    document_unit_id BIGINT NOT NULL,
    court VARCHAR(256),
    CONSTRAINT fk_incorrect_court_document_unit FOREIGN KEY (document_unit_id) REFERENCES doc_unit (id) ON DELETE CASCADE
  )
