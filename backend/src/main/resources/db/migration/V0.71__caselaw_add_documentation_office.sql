CREATE TABLE IF NOT EXISTS
  documentation_office (
    id uuid NOT NULL primary key,
    label Varchar(255) CONSTRAINT uc_abbreviation UNIQUE,
    abbreviation Varchar(10)
  );

ALTER TABLE
  doc_unit
DROP COLUMN IF EXISTS
  dokumentationsstelle;

ALTER TABLE
  doc_unit
ADD COLUMN IF NOT EXISTS
  documentation_office_id UUID;

ALTER TABLE
  doc_unit
ADD
  CONSTRAINT fk_documentation_office FOREIGN KEY (documentation_office_id) REFERENCES documentation_office (id);
