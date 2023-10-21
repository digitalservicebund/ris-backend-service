ALTER TABLE IF EXISTS
  incremental_migration.norm_reference
ADD COLUMN IF NOT EXISTS
  legacy_doc_unit_id uuid;
