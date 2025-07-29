insert into
    incremental_migration.document_type (id, abbreviation, label, document_category_id)
values
    (
        '0f382996-497f-4c2f-9a30-1c73d8ac0a87',
        'Bes',
        'Beschluss',
        '4a92661f-0367-4992-98ec-fb7cffa0d714'
    ),
    (
        '11defe05-cd4d-43e5-a07e-06c611b81a26',
        'Ur',
        'Urteil',
        '4a92661f-0367-4992-98ec-fb7cffa0d714'
    ),
    (
        '2e5bab9c-8852-49a3-8ed8-08b67399abde',
        'Ao',
        'Anordnung',
        '4a92661f-0367-4992-98ec-fb7cffa0d714'
    ),
    (
        '3fc6c738-7e81-40bd-8aa4-c5426605e9b0',
        'AmA',
        'Amtsrechtliche Anordnung',
        '4a92661f-0367-4992-98ec-fb7cffa0d714'
    ),
    (
        '1f382996-4924-4c2f-9a30-1c73d8ac0a87',
        'And',
        'Andere',
        '457dc48a-5866-4bc0-b714-7767912af5e5'
    ),
    (
        'f718a7ee-f419-46cf-a96a-29227927850c',
        'Ean',
        'Anmerkung',
        '0e71b51d-9a98-4191-8f9b-6d67ba002849'
    ),
    (
        '198b276e-8e6d-4df6-8692-44d74ed4fcba',
        'Ebs',
        'Entscheidungsbesprechung',
        '0e71b51d-9a98-4191-8f9b-6d67ba002849'
    ),
    (
        '3e58b70a-65fc-4f7a-8d3a-f42b9e9d3c47',
        'Anh',
        'Anhängiges Verfahren',
        '84c7aae1-486c-465f-9bce-f3472f9be2be'
    )
on conflict do nothing;
