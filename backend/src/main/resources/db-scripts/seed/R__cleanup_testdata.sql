-- This timestamp updates the checksum, so flyway registers an update and migrates each time
-- ${flyway:timestamp}

-- YY DECISIONS

BEGIN;

DELETE
FROM incremental_migration.decision p
    USING incremental_migration.documentation_unit d
WHERE p.id = d.id
  AND d.document_number LIKE 'YY%';;

DELETE
FROM incremental_migration.pending_proceeding p
    USING incremental_migration.documentation_unit d
WHERE p.id = d.id
  AND d.document_number LIKE 'YY%';;

-- Delete any test-related records in the documentation_unit table
DELETE
FROM incremental_migration.documentation_unit
where document_number LIKE 'YY%';

COMMIT;

-- PROCEDURES
-- Unassign other doc units from test procedures, so that they can be deleted.
UPDATE incremental_migration.decision
SET current_procedure_id = NULL
WHERE current_procedure_id IN ((SELECT id
                                from incremental_migration.procedure
                                where name LIKE 'e2e\-%'));

-- Delete test-related records from documentation_unit_procedure
delete
from incremental_migration.documentation_unit_procedure
where procedure_id IN (SELECT id
                       from incremental_migration.procedure
                       where name LIKE 'e2e\-%');

-- Delete test-related records from procedure table
delete
from incremental_migration.procedure
where name LIKE 'e2e\-%';

-- DECISIONS
-- Delete from decision table first to avoid foreign key constraint issues
DELETE
FROM incremental_migration.decision
    USING incremental_migration.file_number f
WHERE decision.id IN (SELECT d.id
                      FROM incremental_migration.documentation_unit d
                               JOIN incremental_migration.file_number f ON f.documentation_unit_id = d.id
                      WHERE f.value LIKE 'e2e\-%');

-- Now delete from documentation_unit
DELETE
FROM incremental_migration.documentation_unit
    USING incremental_migration.file_number f
WHERE f.documentation_unit_id = incremental_migration.documentation_unit.id
  AND f.value LIKE 'e2e\-%';

-- SOURCES
DELETE
FROM incremental_migration.source AS source
USING incremental_migration.reference r
WHERE r.id = source.reference_id AND r.citation LIKE '%e2e\-%';

-- REFERENCES
delete
from incremental_migration.reference
where citation LIKE '%e2e\-%';

-- EDITIONS
delete
from incremental_migration.edition
where name LIKE '%e2e\-%';


-- TEXT CHECK IGNORED WORDS
delete
from incremental_migration.ignored_text_check_word
where juris_id < 0;
delete
from incremental_migration.ignored_text_check_word
where word LIKE 'etoe%';

-- EURLEX Decisions
delete from incremental_migration.attachment
where documentation_unit_id in (
    select id from incremental_migration.decision
    where celex = '62019CV0001(02)' or celex = '62024CO0878' or celex = '62023CJ0538'
);

delete from incremental_migration.documentation_unit
where id in (
    select id from incremental_migration.decision
    where celex = '62019CV0001(02)' or celex = '62024CO0878' or celex = '62023CJ0538'
);

delete from incremental_migration.eurlex
where celex = '62019CV0001(02)' or celex = '62024CO0878' or celex = '62023CJ0538';
