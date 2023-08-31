CREATE TABLE IF NOT EXISTS
  recitals (
    guid uuid NOT NULL PRIMARY KEY,
    marker varchar(255),
    heading varchar(255),
    text text NOT NULL
  );

CREATE TABLE IF NOT EXISTS
  formulas (
    guid uuid NOT NULL PRIMARY KEY,
    text text NOT NULL
  );

CREATE TABLE IF NOT EXISTS
  conclusions (
    guid uuid NOT NULL PRIMARY KEY,
    text text NOT NULL
  );

ALTER TABLE
  norms
ADD COLUMN IF NOT EXISTS
  recitals_guid uuid,
ADD COLUMN IF NOT EXISTS
  formula_guid uuid,
ADD COLUMN IF NOT EXISTS
  conclusion_guid uuid,
ADD
  CONSTRAINT fk_recitals_guid FOREIGN KEY (recitals_guid) REFERENCES recitals (guid) ON DELETE CASCADE,
ADD
  CONSTRAINT fk_formulas_guid FOREIGN KEY (formula_guid) REFERENCES formulas (guid) ON DELETE CASCADE,
ADD
  CONSTRAINT fk_conclusions_guid FOREIGN KEY (conclusion_guid) REFERENCES conclusions (guid) ON DELETE CASCADE;
