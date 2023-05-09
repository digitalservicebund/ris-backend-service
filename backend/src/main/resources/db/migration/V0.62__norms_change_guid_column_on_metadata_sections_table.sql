CREATE EXTENSION
  IF NOT EXISTS pgcrypto;

UPDATE
  metadata_sections
SET
  guid = gen_random_uuid ()
WHERE
  guid IS NULL;

ALTER TABLE
  metadata_sections
ALTER COLUMN
  guid
SET NOT NULL;
