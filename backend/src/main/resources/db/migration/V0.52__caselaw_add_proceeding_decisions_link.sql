CREATE TABLE IF NOT EXISTS
  proceeding_decision_link (
    id BIGSERIAL,
    parent_document_unit_id BIGINT NOT NULL,
    child_document_unit_id BIGINT NOT NULL,
    UNIQUE (parent_document_unit_id, child_document_unit_id),
    CONSTRAINT fk_parent_document_unit FOREIGN KEY (parent_document_unit_id) REFERENCES doc_unit (id),
    CONSTRAINT fk_child_document_unit FOREIGN KEY (child_document_unit_id) REFERENCES doc_unit (id)
  );
