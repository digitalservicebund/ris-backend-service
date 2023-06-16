CREATE TABLE
  documentation_unit_link (
    id BIGSERIAL NOT NULL PRIMARY KEY,
    parent_documentation_unit_uuid UUID NOT NULL,
    child_documentation_unit_uuid UUID NOT NULL,
    type
      VARCHAR(30) NOT NULL,
      UNIQUE (
        parent_documentation_unit_uuid,
        child_documentation_unit_uuid,
        type
      ),
      FOREIGN KEY (parent_documentation_unit_uuid) REFERENCES doc_unit (uuid),
      FOREIGN KEY (child_documentation_unit_uuid) REFERENCES doc_unit (uuid)
  );

CREATE INDEX
  IF NOT EXISTS documentation_uuid_idx ON doc_unit (uuid);
