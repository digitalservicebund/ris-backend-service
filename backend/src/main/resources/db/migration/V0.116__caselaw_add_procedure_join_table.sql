CREATE TABLE IF NOT EXISTS
  procedure (
    id uuid NOT NULL PRIMARY KEY,
    created_at TIMESTAMP DEFAULT (now()),
    name varchar(255),
    documentation_office_id UUID,
    CONSTRAINT fk_documentation_office FOREIGN KEY (documentation_office_id) REFERENCES documentation_office (id),
    CONSTRAINT unique_title_per_office UNIQUE (name, documentation_office_id)
  );

ALTER TABLE IF EXISTS
  doc_unit
ADD COLUMN
  procedure_id UUID,
ADD
  CONSTRAINT fk_procedure FOREIGN KEY (procedure_id) REFERENCES procedure (id);
