ALTER TABLE
  metadata
ADD COLUMN
  section_guid uuid;

UPDATE
  metadata
SET
  section_guid = (
    SELECT
      guid
    FROM
      metadata_sections
    WHERE
      id = metadata.section_id
    LIMIT
      1
  );
