CREATE SCHEMA IF NOT EXISTS references_schema;

CREATE TABLE IF NOT EXISTS references_schema.revoked_adm (
                                                              id UUID PRIMARY KEY,
                                                              doc_unit_id UUID,
                                                              revoked_at TIMESTAMP
);

DELETE FROM references_schema.revoked_adm WHERE id = 'f0f4d34b-1685-4548-9076-3b68b5d285b2';
INSERT INTO references_schema.revoked_adm (id, doc_unit_id, revoked_at)
VALUES ('f0f4d34b-1685-4548-9076-3b68b5d285b2', '41519069-ae24-4dd9-a412-32f2d594fb86', now() at time zone 'utc');
