DELETE FROM
  document_unit_norm;

ALTER TABLE
  document_unit_norm
ADD COLUMN IF NOT EXISTS
  norm_abbreviation_uuid VARCHAR(255);

ALTER TABLE
  document_unit_norm
DROP COLUMN IF EXISTS
  ris_abbreviation
