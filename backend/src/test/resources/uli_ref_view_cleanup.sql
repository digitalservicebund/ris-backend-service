DELETE FROM references_schema.ref_view_active_citation_uli_caselaw;
DELETE FROM references_schema.ref_view_uli;

DELETE FROM incremental_migration.decision
WHERE id = 'adb8408b-5a77-48f9-9ed0-b8dee4f2db02';

DELETE FROM incremental_migration.documentation_unit
WHERE id = 'adb8408b-5a77-48f9-9ed0-b8dee4f2db02';
