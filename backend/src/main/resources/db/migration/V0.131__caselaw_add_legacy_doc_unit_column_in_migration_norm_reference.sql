ALTER TABLE IF EXISTS
  incremental_migration.norm_reference
ADD COLUMN IF NOT EXISTS
  legacy_doc_unit_id uuid;

ALTER TABLE IF EXISTS
  incremental_migration.norm_reference
ADD
  CONSTRAINT fk_legacy_doc_unit FOREIGN KEY (legacy_doc_unit_id) REFERENCES public.doc_unit (uuid) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;
