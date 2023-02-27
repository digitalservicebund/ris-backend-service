CREATE TABLE IF NOT EXISTS
  document_unit_field_of_law (
    id BIGSERIAL PRIMARY KEY,
    document_unit_id BIGINT NOT NULL,
    field_of_law_id BIGINT NOT NULL,
    UNIQUE (document_unit_id, field_of_law_id),
    CONSTRAINT fk_document_unit FOREIGN KEY (document_unit_id) REFERENCES doc_unit (id) ON DELETE CASCADE,
    CONSTRAINT fk_field_of_law FOREIGN KEY (field_of_law_id) REFERENCES lookuptable_subject_field (id) ON DELETE CASCADE
  );

ALTER TABLE
  lookuptable_subject_field
ALTER COLUMN
  change_indicator
SET DEFAULT
  'U';
