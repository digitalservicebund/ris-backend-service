-- DROP TABLE IF EXISTS previous_decision;
CREATE TABLE IF NOT EXISTS
  document_unit_link (
    parent_document_unit_id INT NOT NULL,
    child_document_unit_id INT NOT NULL,
    UNIQUE (parent_document_unit_id, child_document_unit_id),
    CONSTRAINT fk_parent_document_unit FOREIGN KEY (parent_document_unit_id) REFERENCES doc_unit (id) ON DELETE CASCADE,
    CONSTRAINT fk_child_document_unit FOREIGN KEY (child_document_unit_id) REFERENCES doc_unit (id) ON DELETE CASCADE
  );
