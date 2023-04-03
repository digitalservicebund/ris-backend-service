ALTER TABLE
  metadata_sections
DROP COLUMN
  order_number;

ALTER TABLE
  metadata
ADD
  section_id INT NULL;
