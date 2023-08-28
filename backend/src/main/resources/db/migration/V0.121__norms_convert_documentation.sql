CREATE TABLE IF NOT EXISTS
  document_section (
    guid uuid NOT NULL PRIMARY KEY,
    order_number INT NOT NULL,
    type
      varchar(255) NOT NULL,
      marker varchar(255),
      heading varchar(255),
      norm_guid uuid NOT NULL,
      parent_section_guid uuid,
      CONSTRAINT fk_norm_guid FOREIGN KEY (norm_guid) REFERENCES norms (guid) ON DELETE CASCADE,
      CONSTRAINT fk_parent_section_guid FOREIGN KEY (parent_section_guid) REFERENCES document_section (guid) ON DELETE CASCADE
  );

ALTER TABLE IF EXISTS
  articles
RENAME COLUMN
  title TO heading;

ALTER TABLE IF EXISTS
  articles
ADD COLUMN IF NOT EXISTS
  order_number INT NOT NULL DEFAULT 0;

ALTER TABLE IF EXISTS
  articles
ALTER COLUMN
  marker
DROP NOT NULL;

ALTER TABLE IF EXISTS
  articles
ALTER COLUMN
  norm_guid
DROP NOT NULL;

ALTER TABLE IF EXISTS
  articles
ADD COLUMN IF NOT EXISTS
  document_section_guid uuid;

ALTER TABLE IF EXISTS
  articles
ADD
  CONSTRAINT fk_document_section_guid FOREIGN KEY (document_section_guid) REFERENCES document_section (guid) ON DELETE CASCADE;

DROP TABLE IF EXISTS
  contents,
  sections;
