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
VALUES ('550e8400-e29b-41d4-a716-446655440000', 'ULI-TEST-VALID-1', 'ULI author', 'ULI citation', CURRENT_TIMESTAMP);
