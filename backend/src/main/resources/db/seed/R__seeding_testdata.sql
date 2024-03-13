-- This timestamp updates the checksum, so flyway registers an update and migrates each time
-- ${flyway:timestamp}
delete from
  incremental_migration.documentation_unit
where
  document_number LIKE 'YY%';

INSERT INTO
  incremental_migration.documentation_unit (id, document_number, documentation_office_id)
SELECT
  gen_random_uuid (),
  'YYTestDoc0001',
  id
FROM
  incremental_migration.documentation_office
WHERE
  abbreviation = 'DS'
UNION ALL
SELECT
  gen_random_uuid (),
  'YYTestDoc0002',
  id
FROM
  incremental_migration.documentation_office
WHERE
  abbreviation = 'DS';
