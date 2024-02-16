insert into
  incremental_migration.document_category (id, label)
values
  ('0f382996-497f-4c2f-9a30-1c73d8ac0a88', 'R'),
  ('11defe05-cd4d-43e5-a07e-06c611b81a28', 'S') on conflict
do
  nothing;

insert into
  incremental_migration.document_type (id, abbreviation, label, document_category_id)
values
  (
    '0f382996-497f-4c2f-9a30-1c73d8ac0a87',
    'Beschluss',
    'Bes',
    '0f382996-497f-4c2f-9a30-1c73d8ac0a88'
  ),
  (
    '11defe05-cd4d-43e5-a07e-06c611b81a26',
    'Urteil',
    'Ur',
    '0f382996-497f-4c2f-9a30-1c73d8ac0a88'
  ),
  (
    '2e5bab9c-8852-49a3-8ed8-08b67399abde',
    'Anordnung',
    'Ao',
    '0f382996-497f-4c2f-9a30-1c73d8ac0a88'
  ),
  (
    '3fc6c738-7e81-40bd-8aa4-c5426605e9b0',
    'Amtsrechtliche Anordnung',
    'AmA',
    '0f382996-497f-4c2f-9a30-1c73d8ac0a88'
  ),
  (
    '1f382996-4924-4c2f-9a30-1c73d8ac0a87',
    'Andere',
    'And',
    '11defe05-cd4d-43e5-a07e-06c611b81a28'
  ) on conflict
do
  nothing;
