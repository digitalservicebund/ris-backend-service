
INSERT INTO
  documentation_unit (id, document_number, documentation_office_id)
VALUES
  (
    '16f9ae5c-ea72-46d8-864c-ce9dd7cee4a3',
    '1234567890123',
    '6be0bb1a-c196-484a-addf-822f2ab557f7'
  ),
  (
    '26f9ae5c-ea72-46d8-864c-ce9dd7cee4a3',
    'docNumber00002',
    '6be0bb1a-c196-484a-addf-822f2ab557f7'
  ),
  (
    '36f9ae5c-ea72-46d8-864c-ce9dd7cee4a3',
    'docNumber00003',
    '6be0bb1a-c196-484a-addf-822f2ab557f7'
  ),
  (
    '46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3',
    'bghDocument123',
    'bd350c93-7ff0-4409-9c62-371e3d0c749e'
  );

INSERT INTO
    decision (id)
VALUES
    (
        '16f9ae5c-ea72-46d8-864c-ce9dd7cee4a3'
    ),
    (
        '26f9ae5c-ea72-46d8-864c-ce9dd7cee4a3'
    ),
    (
        '36f9ae5c-ea72-46d8-864c-ce9dd7cee4a3'
    ),
    (
        '46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3'
    );

insert into
    status (id, documentation_unit_id, publication_status, created_at, with_error)
values
    (
        '75988131-f355-414d-9da5-dcbcdbf4b98f',
        '16f9ae5c-ea72-46d8-864c-ce9dd7cee4a3',
        'PUBLISHED',
        current_timestamp,
        false
    ),
    (
        '65988131-f355-414d-9da5-dcbcdbf4b98f',
        '26f9ae5c-ea72-46d8-864c-ce9dd7cee4a3',
        'PUBLISHED',
     current_timestamp,
        false
    ),
    (
        '55988131-f355-414d-9da5-dcbcdbf4b98f',
        '36f9ae5c-ea72-46d8-864c-ce9dd7cee4a3',
        'PUBLISHED',
        current_timestamp,
        false
    ),
    (
        '45988131-f355-414d-9da5-dcbcdbf4b98f',
        '46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3',
        'PUBLISHED',
        current_timestamp,
        false
    );

update documentation_unit set current_status_id = '75988131-f355-414d-9da5-dcbcdbf4b98f' where id = '16f9ae5c-ea72-46d8-864c-ce9dd7cee4a3';
update documentation_unit set current_status_id = '65988131-f355-414d-9da5-dcbcdbf4b98f' where id = '26f9ae5c-ea72-46d8-864c-ce9dd7cee4a3';
update documentation_unit set current_status_id = '55988131-f355-414d-9da5-dcbcdbf4b98f' where id = '36f9ae5c-ea72-46d8-864c-ce9dd7cee4a3';
update documentation_unit set current_status_id = '45988131-f355-414d-9da5-dcbcdbf4b98f' where id = '46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3';

INSERT INTO
  procedure (id, documentation_office_id, name, created_at, documentation_office_user_group_id)
VALUES
  (
    '1e768071-1a19-43a1-8ab9-c185adec94bf',
    '6be0bb1a-c196-484a-addf-822f2ab557f7',
    'procedure1',
    null,
    null
  ),
  (
    '2e768071-1a19-43a1-8ab9-c185adec94bf',
    '6be0bb1a-c196-484a-addf-822f2ab557f7',
    'procedure2',
    null,
    null
  ),
  (
    '3e768071-1a19-43a1-8ab9-c185adec94bf',
    '6be0bb1a-c196-484a-addf-822f2ab557f7',
    'procedure3',
    null,
    null
  ),
  (
    '9e768071-1a19-43a1-8ab9-c185adec94bf',
    'bd350c93-7ff0-4409-9c62-371e3d0c749e',
    'testProcedure BGH',
    null,
    null
  ),
  (
    '3f768071-1a19-43a1-8ab9-c185adec94bf',
    '6be0bb1a-c196-484a-addf-822f2ab557f7',
    'with date',
    '2023-12-04 18:21:48.334174+00',
    null
  ),
  (
    '4f768071-1a19-43a1-8ab9-c185adec94bf',
    '6be0bb1a-c196-484a-addf-822f2ab557f7',
    'with date in past',
    '2022-12-04 18:21:48.334174+00',
    null
  ),
  (
    '5f768071-1a19-43a1-8ab9-c185adec94bf',
    '6be0bb1a-c196-484a-addf-822f2ab557f7',
    'without date',
    null,
    null
  );
