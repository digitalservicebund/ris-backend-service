DROP TABLE IF EXISTS
  deviating_decisiondate;

CREATE TABLE IF NOT EXISTS
  deviating_decision_date (
    id BIGSERIAL NOT NULL PRIMARY KEY,
    document_unit_id BIGINT NOT NULL,
    decision_date TIMESTAMP,
    CONSTRAINT fk_decisiondate_document_unit FOREIGN KEY (document_unit_id) REFERENCES doc_unit (id) ON DELETE CASCADE,
    CONSTRAINT uc_decisiondate_id_document_unit_id UNIQUE (id, document_unit_id)
  );
