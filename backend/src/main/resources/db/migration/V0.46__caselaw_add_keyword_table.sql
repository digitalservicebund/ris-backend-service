CREATE TABLE IF NOT EXISTS
  keyword (
    id BIGSERIAL PRIMARY KEY,
    document_unit_id BIGINT NOT NULL,
    keyword VARCHAR(255),
    CONSTRAINT fk_document_unit FOREIGN KEY (document_unit_id) REFERENCES doc_unit (id) ON DELETE CASCADE
  );
