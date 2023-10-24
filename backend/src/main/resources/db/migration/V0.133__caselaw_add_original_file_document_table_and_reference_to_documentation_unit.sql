CREATE TABLE
  original_file_document (
    id UUID NOT NULL PRIMARY KEY,
    upload_timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    extension VARCHAR(10) NOT NULL,
    filename VARCHAR(255) NOT NULL,
    s3_object_path VARCHAR(255) NOT NULL,
    documentation_unit_id UUID NOT NULL CONSTRAINT fk_documentation_unit REFERENCES incremental_migration.documentation_unit
  );
