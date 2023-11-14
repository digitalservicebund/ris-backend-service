ALTER TABLE
  procedure
DROP
  CONSTRAINT IF EXISTS fk_documentation_office;

ALTER TABLE
  procedure
ADD
  CONSTRAINT fk_documentation_office FOREIGN KEY (documentation_office_id) REFERENCES incremental_migration.documentation_office (id) ON DELETE CASCADE;

ALTER TABLE
  procedure_link
DROP COLUMN IF EXISTS
  id;

ALTER TABLE
  procedure_link
DROP
  CONSTRAINT IF EXISTS fk_documentation_unit;

ALTER TABLE
  procedure_link
ADD
  CONSTRAINT fk_documentation_unit FOREIGN KEY (documentation_unit_id) REFERENCES incremental_migration.documentation_unit (id) ON DELETE CASCADE;
