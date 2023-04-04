ALTER TABLE
  metadata
ALTER COLUMN
  section_id
SET NOT NULL;

ALTER TABLE
  metadata
DROP COLUMN
  norm_id;
