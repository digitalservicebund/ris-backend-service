CREATE TABLE IF NOT EXISTS
  lookuptable_subject_field (
    id BIGINT,
    parent_id BIGINT,
    parent BOOLEAN,
    change_date_mail VARCHAR(255),
    change_date_client VARCHAR(255),
    change_indicator CHAR(1),
    version VARCHAR(255),
    subject_field_number VARCHAR(255) UNIQUE,
    subject_field_text VARCHAR(1023),
    navigation_term VARCHAR(255),
    PRIMARY KEY (id),
    FOREIGN KEY (parent_id) REFERENCES lookuptable_subject_field (id)
  );

CREATE TABLE IF NOT EXISTS
  lookuptable_subject_field_keyword (
    id BIGSERIAL,
    subject_field_id BIGINT,
    value VARCHAR(255),
    PRIMARY KEY (id),
    FOREIGN KEY (subject_field_id) REFERENCES lookuptable_subject_field (id) ON DELETE CASCADE ON UPDATE CASCADE
  );

CREATE TABLE IF NOT EXISTS
  lookuptable_subject_field_norm (
    id BIGSERIAL,
    subject_field_id BIGINT,
    abbreviation VARCHAR(255),
    single_norm_description VARCHAR(255),
    PRIMARY KEY (id),
    FOREIGN KEY (subject_field_id) REFERENCES lookuptable_subject_field (id) ON DELETE CASCADE ON UPDATE CASCADE
  );
