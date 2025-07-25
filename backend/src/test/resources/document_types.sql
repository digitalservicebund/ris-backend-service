-- We need to explicitly delete category 'A' in order to be able to add it afterwards
-- Otherwise, we would have conflicts with this migration script which adds category 'A' already:
-- https://github.com/digitalservicebund/ris-data-migration/blob/d08de33076482211f35adfab516312af282b82db/schema/src/main/resources/db/migration/V1.72__add_pending_document_category.sql
delete from incremental_migration.document_category where label = 'A';

insert into
    incremental_migration.document_category (id, label)
values
    ('a7cbd3c2-0d9a-4e0c-a8a7-2f9b7d4e3542', 'A'),
    ('0f382996-497f-4c2f-9a30-1c73d8ac0a88', 'R'),
    ('4879ae8e-e809-4dd7-8517-d5c795bead79', 'U'),
    ('11defe05-cd4d-43e5-a07e-06c611b81a28', 'S')
on conflict do nothing;

insert into
    incremental_migration.document_type (id, abbreviation, label, document_category_id)
values
    (
        '0f382996-497f-4c2f-9a30-1c73d8ac0a87',
        'Bes',
        'Beschluss',
        '0f382996-497f-4c2f-9a30-1c73d8ac0a88'
    ),
    (
        '11defe05-cd4d-43e5-a07e-06c611b81a26',
        'Ur',
        'Urteil',
        '0f382996-497f-4c2f-9a30-1c73d8ac0a88'
    ),
    (
        '2e5bab9c-8852-49a3-8ed8-08b67399abde',
        'Ao',
        'Anordnung',
        '0f382996-497f-4c2f-9a30-1c73d8ac0a88'
    ),
    (
        '3fc6c738-7e81-40bd-8aa4-c5426605e9b0',
        'AmA',
        'Amtsrechtliche Anordnung',
        '0f382996-497f-4c2f-9a30-1c73d8ac0a88'
    ),
    (
        '1f382996-4924-4c2f-9a30-1c73d8ac0a87',
        'And',
        'Andere',
        '11defe05-cd4d-43e5-a07e-06c611b81a28'
    ),
    (
        'f718a7ee-f419-46cf-a96a-29227927850c',
        'Ean',
        'Anmerkung',
        '4879ae8e-e809-4dd7-8517-d5c795bead79'
    ),
    (
        '198b276e-8e6d-4df6-8692-44d74ed4fcba',
        'Ebs',
        'Entscheidungsbesprechung',
        '4879ae8e-e809-4dd7-8517-d5c795bead79'
    ),
    (
        '3e58b70a-65fc-4f7a-8d3a-f42b9e9d3c47',
        'Anh',
        'Anhängiges Verfahren',
        'a7cbd3c2-0d9a-4e0c-a8a7-2f9b7d4e3542'
    )
on conflict do nothing;
