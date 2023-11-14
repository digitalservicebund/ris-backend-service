ALTER TABLE
  status
DROP
  CONSTRAINT IF EXISTS fk_document_unit;

ALTER TABLE
  status
ADD
  CONSTRAINT fk_document_unit FOREIGN KEY (document_unit_id) REFERENCES incremental_migration.documentation_unit (id) ON DELETE CASCADE;
