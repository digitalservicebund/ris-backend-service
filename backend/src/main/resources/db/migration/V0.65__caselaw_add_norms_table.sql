CREATE TABLE IF NOT EXISTS
  document_unit_norm (
    id BIGSERIAL PRIMARY KEY,
    document_unit_id BIGINT NOT NULL,
    ris_abbreviation VARCHAR(255),
    single_norm VARCHAR(255),
    date_of_version TIMESTAMP WITH TIME ZONE,
    date_of_relevance VARCHAR(255),
    CONSTRAINT fk_document_unit FOREIGN KEY (document_unit_id) REFERENCES doc_unit (id) ON DELETE CASCADE
  );
