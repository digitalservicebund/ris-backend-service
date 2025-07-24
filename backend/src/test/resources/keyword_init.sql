insert into
  incremental_migration.documentation_unit (id, document_number, documentation_office_id)
values
  (
    '46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3',
    'documentnr001',
    '6be0bb1a-c196-484a-addf-822f2ab557f7'
  ),
  (
    '89db7895-ca6f-4333-bcb3-83b18c8f3b49',
    'documentnr002',
    '6be0bb1a-c196-484a-addf-822f2ab557f7'
  ),
  (
    '399c79a6-749b-11ee-b962-0242ac120002',
    'documentnr003',
    '6be0bb1a-c196-484a-addf-822f2ab557f7'
  );

insert into
    incremental_migration.decision (id)
values
    (
        '46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3'
    ),
    (
        '89db7895-ca6f-4333-bcb3-83b18c8f3b49'
    ),
    (
        '399c79a6-749b-11ee-b962-0242ac120002'
    );

insert into
    incremental_migration.status (id, documentation_unit_id, publication_status, created_at, with_error)
values
    (
        '75988131-f355-414d-9da5-dcbcdbf4b98f',
        '46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3',
        'PUBLISHED',
        current_timestamp,
        false
    ),
    (
        '65988131-f355-414d-9da5-dcbcdbf4b98f',
        '89db7895-ca6f-4333-bcb3-83b18c8f3b49',
        'PUBLISHED',
        current_timestamp,
        false
    ),
    (
        '55988131-f355-414d-9da5-dcbcdbf4b98f',
        '399c79a6-749b-11ee-b962-0242ac120002',
        'PUBLISHED',
        current_timestamp,
        false
    );

update incremental_migration.documentation_unit set current_status_id = '75988131-f355-414d-9da5-dcbcdbf4b98f' where id = '46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3';
update incremental_migration.documentation_unit set current_status_id = '65988131-f355-414d-9da5-dcbcdbf4b98f' where id = '89db7895-ca6f-4333-bcb3-83b18c8f3b49';
update incremental_migration.documentation_unit set current_status_id = '55988131-f355-414d-9da5-dcbcdbf4b98f' where id = '399c79a6-749b-11ee-b962-0242ac120002';

insert into
  incremental_migration.keyword (id, value)
values
  (
    'f0232240-7416-11ee-b962-0242ac120002',
    'keyword1'
  ),
  (
    'e8cb50fc-74ad-11ee-b962-0242ac120002',
    'keyword2'
  );

insert into
  incremental_migration.documentation_unit_keyword (documentation_unit_id, keyword_id)
values
  (
    '46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3',
    'f0232240-7416-11ee-b962-0242ac120002'
  ),
  (
    '46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3',
    'e8cb50fc-74ad-11ee-b962-0242ac120002'
  ),
  (
    '89db7895-ca6f-4333-bcb3-83b18c8f3b49',
    'f0232240-7416-11ee-b962-0242ac120002'
  );
