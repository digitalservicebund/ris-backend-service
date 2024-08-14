CREATE TABLE IF NOT EXISTS
  documentation_office_user_group (
    id UUID NOT NULL PRIMARY KEY,
    user_group_path_name VARCHAR(255) UNIQUE NOT NULL,
    documentation_office_id UUID NOT NULL,
    is_internal BOOL NOT NULL
    -- It is not possible to reference the incremental_migration schema from the ris-backend public schema
    -- CONSTRAINT fk_documentation_office_id FOREIGN KEY(documentation_office_id) REFERENCES incremental_migration.documentation_office(id)
  );
