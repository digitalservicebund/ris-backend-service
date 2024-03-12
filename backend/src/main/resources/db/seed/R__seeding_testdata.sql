-- This timestamp updates the checksum, so flyway registers an update and migrates each time
-- ${flyway:timestamp}
delete from
  incremental_migration.documentation_unit
where
  document_number LIKE 'YY%';

INSERT INTO
  incremental_migration.documentation_unit (id, document_number, documentation_office_id)
SELECT
  '491aef4d-9fb3-4769-b1a5-4edbb9321c6d'::uuid,
  'YYYTestDoc1',
  id
FROM
  incremental_migration.documentation_office
WHERE
  abbreviation = 'DS'
UNION ALL
SELECT
  '947ff4db-71a8-466d-995d-dd6be8760f98'::uuid,
  'YYYTestDoc2',
  id
FROM
  incremental_migration.documentation_office
WHERE
  abbreviation = 'DS';
