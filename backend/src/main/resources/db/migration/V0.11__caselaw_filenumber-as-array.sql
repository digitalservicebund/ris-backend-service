CREATE TABLE IF NOT EXISTS
  file_number (
    id BIGSERIAL NOT NULL PRIMARY KEY,
    document_unit_id BIGINT NOT NULL,
    file_number VARCHAR(255),
    is_deviating BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_file_number_document_unit FOREIGN KEY (document_unit_id) REFERENCES doc_unit (id) ON DELETE CASCADE,
    CONSTRAINT uc_id_document_unit_id UNIQUE (id, document_unit_id)
  );

INSERT INTO
  file_number (document_unit_id, file_number, is_deviating)
SELECT
  id,
  aktenzeichen,
  FALSE
FROM
  doc_unit
WHERE
  aktenzeichen IS NOT NULL
  AND aktenzeichen != '';

ALTER TABLE
  doc_unit
DROP COLUMN
  aktenzeichen
