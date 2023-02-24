CREATE TABLE IF NOT EXISTS
  deviating_ecli (
    id BIGSERIAL NOT NULL PRIMARY KEY,
    document_unit_id BIGINT NOT NULL,
    ecli VARCHAR(255),
    CONSTRAINT fk_ecli_document_unit FOREIGN KEY (document_unit_id) REFERENCES doc_unit (id) ON DELETE CASCADE,
    CONSTRAINT uc_ecli_id_document_unit_id UNIQUE (id, document_unit_id)
  );
