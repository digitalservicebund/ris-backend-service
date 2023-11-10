DELETE FROM
  xml_publication;

DELETE FROM
  publication_report;

ALTER TABLE
  xml_publication
ADD COLUMN
  uuid UUID DEFAULT uuid_generate_v7 ();

ALTER TABLE
  xml_publication
DROP
  CONSTRAINT IF EXISTS xml_mail_pkey;

DROP INDEX
  IF EXISTS xml_publication.xml_mail_pkey;

ALTER TABLE
  xml_publication
DROP COLUMN IF EXISTS
  id;

ALTER TABLE
  xml_publication
RENAME COLUMN
  uuid TO id;

ALTER TABLE
  xml_publication
ADD
  CONSTRAINT xml_publication_pkey PRIMARY KEY (id);

ALTER TABLE
  xml_publication
ADD COLUMN
  documentation_unit_id UUID;

ALTER TABLE
  xml_publication
DROP COLUMN
  document_unit_id;

ALTER TABLE
  xml_publication
ADD
  CONSTRAINT fk_documentation_unit FOREIGN KEY (documentation_unit_id) REFERENCES incremental_migration.documentation_unit (id) ON DELETE CASCADE;

ALTER TABLE
  publication_report
DROP
  CONSTRAINT IF EXISTS fk_document_unit;

ALTER TABLE
  publication_report
ADD
  CONSTRAINT fk_documentation_unit FOREIGN KEY (document_unit_id) REFERENCES incremental_migration.documentation_unit (id) ON DELETE CASCADE;
