CREATE TABLE IF NOT EXISTS
  procedure_link (
    id uuid NOT NULL PRIMARY KEY,
    created_at TIMESTAMP DEFAULT (now()),
    documentation_unit_id UUID,
    procedure_id UUID,
    CONSTRAINT fk_documentation_unit FOREIGN KEY (documentation_unit_id) REFERENCES doc_unit (uuid),
    CONSTRAINT fk_procedure FOREIGN KEY (procedure_id) REFERENCES procedure (id)
  );
