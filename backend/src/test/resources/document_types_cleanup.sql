DELETE FROM incremental_migration.document_type WHERE ID in (
    '0f382996-497f-4c2f-9a30-1c73d8ac0a87',
    '11defe05-cd4d-43e5-a07e-06c611b81a26',
    '2e5bab9c-8852-49a3-8ed8-08b67399abde',
    '3fc6c738-7e81-40bd-8aa4-c5426605e9b0',
    '1f382996-4924-4c2f-9a30-1c73d8ac0a87',
    'f718a7ee-f419-46cf-a96a-29227927850c',
    '198b276e-8e6d-4df6-8692-44d74ed4fcba');
DELETE FROM incremental_migration.document_category WHERE id IN (
    '0f382996-497f-4c2f-9a30-1c73d8ac0a88',
    '4879ae8e-e809-4dd7-8517-d5c795bead79',
    '11defe05-cd4d-43e5-a07e-06c611b81a28'
);
