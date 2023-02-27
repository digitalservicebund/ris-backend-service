CREATE TABLE IF NOT EXISTS
  field_of_law_link (
    id BIGSERIAL,
    field_id BIGINT,
    linked_field_id BIGINT,
    PRIMARY KEY (id),
    FOREIGN KEY (field_id) REFERENCES lookuptable_subject_field (id) ON DELETE CASCADE,
    FOREIGN KEY (linked_field_id) REFERENCES lookuptable_subject_field (id) ON DELETE CASCADE
  );
