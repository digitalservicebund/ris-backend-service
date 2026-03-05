CREATE SCHEMA IF NOT EXISTS references_schema;

CREATE TABLE IF NOT EXISTS references_schema.revoked_sli (
                                                              id UUID PRIMARY KEY,
                                                              doc_unit_id UUID,
                                                              revoked_at TIMESTAMP
);

INSERT INTO references_schema.revoked_sli (id, doc_unit_id, revoked_at)
VALUES ('cc4901c9-9784-40ef-b565-d6fa6217738b', '7df9d7fd-8ac5-4611-ad78-5a81660c0611', CURRENT_TIMESTAMP);
