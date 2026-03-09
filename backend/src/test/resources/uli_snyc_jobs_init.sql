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

CREATE TABLE IF NOT EXISTS references_schema.ref_view_active_citation_uli_caselaw (
                                                                                      id VARCHAR(255) PRIMARY KEY,
                                                                                      source_id UUID,
                                                                                      target_id UUID
);

INSERT INTO references_schema.ref_view_uli (id, document_number, author, citation, published_at)
VALUES ('550e8400-e29b-41d4-a716-446655440000', 'ULI-TEST-001', 'Autor A', 'NJW 2024, 1', now() at time zone 'utc');

INSERT INTO references_schema.ref_view_active_citation_uli_caselaw (id, source_id, target_id)
VALUES ('link-1', '550e8400-e29b-41d4-a716-446655440000', 'f13e7fe2-78a5-11ee-b962-0242ac120002');
