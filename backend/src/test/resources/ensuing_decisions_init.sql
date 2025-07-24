INSERT INTO
  incremental_migration.document_category (id, label)
VALUES
  ('dd315130-1fda-46d5-bccd-d587cf51c664', 'R'),
  ('4eea678f-39a9-4c56-aba1-b29423415209', 'N');

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
  ),
  (
    'f99a0003-bfa3-4baa-904c-be07e274c741',
    '1362',
    'Karlsruhe',
    'BVerfG'
  );

insert into
  incremental_migration.document_type (id, abbreviation, label, document_category_id)
values
  (
    'b57e016e-665d-486c-902e-c191f5a7acf6',
    'Bes',
    'Beschluss',
    'dd315130-1fda-46d5-bccd-d587cf51c664'
  ),
  (
    '0c64fc8f-806c-4c43-a80f-dc54500b2a5a',
    'AO',
    'Anordnung',
    '4eea678f-39a9-4c56-aba1-b29423415209'
  );

insert into
  incremental_migration.documentation_unit (id, document_number, documentation_office_id)
values
  (
    '46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3',
    'documentnr001',
    '6be0bb1a-c196-484a-addf-822f2ab557f7'
  ),
  (
    'f13e7fe2-78a5-11ee-b962-0242ac120002',
    'documentnr002',
    '6be0bb1a-c196-484a-addf-822f2ab557f7'
  );

insert into
    incremental_migration.decision (id)
values
    (
        '46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3'
    ),
    (
        'f13e7fe2-78a5-11ee-b962-0242ac120002'
    );

insert into
  incremental_migration.related_documentation (
    id,
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
    '96301f85-9bd2-4690-a67f-f9fdfe725de3',
    '2011-01-21',
    'documentnr002',
    '0c64fc8f-806c-4c43-a80f-dc54500b2a5a',
    'cba',
    'note2',
    'pending_decision',
    '46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3',
    2
  );
