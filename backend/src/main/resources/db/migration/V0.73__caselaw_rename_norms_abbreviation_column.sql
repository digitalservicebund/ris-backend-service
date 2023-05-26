DELETE FROM
  document_unit_norm;

ALTER TABLE
  document_unit_norm
RENAME COLUMN
  ris_abbreviation TO norm_abbreviation_uuid;
