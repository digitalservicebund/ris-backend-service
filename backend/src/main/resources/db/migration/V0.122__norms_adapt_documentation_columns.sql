UPDATE
  articles
SET
  marker = ''
WHERE
  marker IS NULL;

ALTER TABLE IF EXISTS
  articles
ALTER COLUMN
  marker
SET NOT NULL;

UPDATE
  document_section
SET
  marker = ''
WHERE
  marker IS NULL;

ALTER TABLE IF EXISTS
  document_section
ALTER COLUMN
  marker
SET NOT NULL;

UPDATE
  document_section
SET
  heading = ''
WHERE
  heading IS NULL;

ALTER TABLE IF EXISTS
  document_section
ALTER COLUMN
  heading
SET NOT NULL;
