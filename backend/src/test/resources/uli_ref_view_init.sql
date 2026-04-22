CREATE SCHEMA IF NOT EXISTS references_schema;

CREATE TABLE IF NOT EXISTS references_schema.ref_view_uli (
                                                              id UUID PRIMARY KEY,
                                                              document_number VARCHAR(255),
                                                              author VARCHAR(255),
                                                              citation VARCHAR(255),
                                                              document_type_raw_value VARCHAR(255),
                                                              legal_periodical_raw_value VARCHAR(255),
                                                              published_at TIMESTAMP
);

INSERT INTO references_schema.ref_view_uli (id, document_number, author, citation, published_at)
VALUES ('550e8400-e29b-41d4-a716-446655440000', 'ULI-TEST-1', 'ULI author', 'ULI citation', CURRENT_TIMESTAMP);

INSERT INTO references_schema.ref_view_uli (id, document_number, author, citation, published_at)
VALUES ('661f9511-f30c-52e5-b827-557766551111', 'ULI-TEST-2', 'Second ULI author', 'Second citation', CURRENT_TIMESTAMP);

CREATE TABLE IF NOT EXISTS references_schema.ref_view_active_citation_uli_caselaw (
                                                                                       id UUID PRIMARY KEY,
                                                                                       source_documentation_unit_id UUID,
                                                                                       target_documentation_unit_id UUID
);

INSERT INTO references_schema.ref_view_active_citation_uli_caselaw (id, source_documentation_unit_id, target_documentation_unit_id)
VALUES
    (gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440000', 'adb8408b-5a77-48f9-9ed0-b8dee4f2db02'),
    (gen_random_uuid(), '661f9511-f30c-52e5-b827-557766551111', 'adb8408b-5a77-48f9-9ed0-b8dee4f2db02');

INSERT INTO incremental_migration.documentation_unit (id, document_number, documentation_office_id)
VALUES ('adb8408b-5a77-48f9-9ed0-b8dee4f2db02', 'YYTestDoc2000', '6be0bb1a-c196-484a-addf-822f2ab557f7');

INSERT INTO incremental_migration.decision (id)
VALUES ('adb8408b-5a77-48f9-9ed0-b8dee4f2db02');
