ALTER TABLE IF EXISTS
  document_unit_status
RENAME TO
  publication_status;

ALTER TABLE
  publication_status
ADD COLUMN IF NOT EXISTS
  with_error BOOLEAN NOT NULL DEFAULT FALSE;
