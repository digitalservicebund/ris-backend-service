ALTER TABLE
  paragraphs
ADD COLUMN
  article_guid uuid;

UPDATE
  paragraphs
SET
  article_guid = (
    SELECT
      guid
    FROM
      articles
    WHERE
      id = paragraphs.article_id
    LIMIT
      1
  );

ALTER TABLE
  articles
ADD COLUMN
  norm_guid uuid;

UPDATE
  articles
SET
  norm_guid = (
    SELECT
      guid
    FROM
      norms
    WHERE
      id = articles.norm_id
    LIMIT
      1
  );

ALTER TABLE
  files
ADD COLUMN
  norm_guid uuid;

UPDATE
  files
SET
  norm_guid = (
    SELECT
      guid
    FROM
      norms
    WHERE
      id = files.norm_id
    LIMIT
      1
  );

ALTER TABLE
  metadata_sections
ADD COLUMN
  section_guid uuid;

UPDATE
  metadata_sections as ms
SET
  section_guid = (
    SELECT
      guid
    FROM
      metadata_sections
    WHERE
      id = ms.section_id
    LIMIT
      1
  );
