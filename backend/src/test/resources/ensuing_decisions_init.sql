INSERT INTO
  incremental_migration.document_category (id, label)
VALUES
  ('dd315130-1fda-46d5-bccd-d587cf51c664', 'R');

insert into
  incremental_migration.court (
    id,
    juris_id,
    location,
    type
  )
values
  (
    '96301f85-9bd2-4690-a67f-f9fdfe725de3',
    '1357',
    'Karlsruhe',
    'BGH'
  );

insert into
  incremental_migration.document_type (id, abbreviation, label, document_category_id)
values
  (
    'b57e016e-665d-486c-902e-c191f5a7acf6',
    'Bes',
    'Beschluss',
    'dd315130-1fda-46d5-bccd-d587cf51c664'
  );

insert into
  incremental_migration.documentation_unit (id, document_number, documentation_office_id)
values
  (
    '46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3',
    'documentnr001',
    'ba90a851-3c54-4858-b4fa-7742ffbe8f05'
  ),
  (
    'f13e7fe2-78a5-11ee-b962-0242ac120002',
    'documentnr002',
    'ba90a851-3c54-4858-b4fa-7742ffbe8f05'
  );

insert into
  incremental_migration.related_documentation (
    id,
    court_location,
    court_type,
    court_id,
    date,
    document_number,
    document_type_id,
    file_number,
    note,
    dtype,
    documentation_unit_id,
    rank
  )
values
  (
    'f0232240-7416-11ee-b962-0242ac120002',
    'Karlsruhe',
    'BGH',
    '96301f85-9bd2-4690-a67f-f9fdfe725de3',
    '2011-01-21',
    null,
    'b57e016e-665d-486c-902e-c191f5a7acf6',
    'abc',
    'note1',
    'ensuing_decision',
    '46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3',
    1
  ),
  (
    'f0232240-7416-11ee-b962-0242ac120003',
    'Karlsruhe',
    'BGH',
    '96301f85-9bd2-4690-a67f-f9fdfe725de3',
    '2011-01-21',
    null,
    'b57e016e-665d-486c-902e-c191f5a7acf6',
    'cba',
    'note2',
    'pending_decision',
    '46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3',
    2
  );
