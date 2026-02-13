--- Test Dokstellen
INSERT INTO
    jurisdiction_type (id, label, juris_id)
VALUES
    ('27e099f9-5b47-4ce9-ac58-b84ca4643bc2', 'Ordentliche Gerichtsbarkeit', '1') ON CONFLICT
    DO
    NOTHING;

UPDATE documentation_office
SET jurisdiction_type_id = '27e099f9-5b47-4ce9-ac58-b84ca4643bc2'
WHERE id = 'bd350c93-7ff0-4409-9c62-371e3d0c749e';
