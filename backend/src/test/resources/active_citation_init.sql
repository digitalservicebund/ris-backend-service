INSERT INTO
  incremental_migration.document_category (id, label)
VALUES
  ('dd315130-1fda-46d5-bccd-d587cf51c664', 'R');

INSERT INTO
  incremental_migration.citation_type (
    id,
    abbreviation,
    label,
    documentation_unit_document_category_id,
    citation_document_category_id,
    juris_id
  )
VALUES
  (
    '4e768071-1a19-43a1-8ab9-c185adec94bf',
    'Anwendung',
    'Anwendung',
    'dd315130-1fda-46d5-bccd-d587cf51c664',
    'dd315130-1fda-46d5-bccd-d587cf51c664',
    48
  ),
  (
    '6b4bd747-fce9-4e49-8af4-3fb4f1d3663c',
    'Nan',
    'Nichtanwendung',
    'dd315130-1fda-46d5-bccd-d587cf51c664',
    'dd315130-1fda-46d5-bccd-d587cf51c664',
    49
  );

insert into
  incremental_migration.documentation_unit (id, document_number, documentation_office_id)
values
  (
    '46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3',
    'documentnr001',
    'ba90a851-3c54-4858-b4fa-7742ffbe8f05'
  );

insert into
  incremental_migration.related_documentation (
    id,
    documentation_unit_id,
    citation_type_id,
    dtype
  )
values
  (
    'f0232240-7416-11ee-b962-0242ac120002',
    '46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3',
    '4e768071-1a19-43a1-8ab9-c185adec94bf',
    'caselaw_active_citation'
  );
