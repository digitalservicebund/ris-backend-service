CREATE TABLE if not exists
  document_category (
    id uuid NOT NULL primary key,
    label char(1) NOT NULL,
    UNIQUE (label)
  );

CREATE TABLE if not exists
  document_type (
    id uuid NOT NULL primary key,
    abbreviation varchar(255),
    label varchar(255),
    multiple boolean NOT NULL default false,
    super_label_1 varchar(255),
    super_label_2 varchar(255),
    document_category_id uuid,
    UNIQUE (abbreviation, document_category_id),
    CONSTRAINT fk_document_category FOREIGN KEY (document_category_id) REFERENCES document_category (id)
  );

CREATE TABLE if not exists
  norm_abbreviation (
    id uuid NOT NULL primary key,
    abbreviation varchar(255) NOT NULL,
    decision_date date,
    document_id integer NOT NULL UNIQUE,
    document_number varchar(80),
    official_letter_abbreviation varchar(255),
    official_long_title text,
    official_short_title text,
    source char(1)
  );

CREATE TABLE if not exists
  norm_element (
    id uuid NOT NULL primary key,
    label varchar(255) NOT NULL,
    has_number_designation boolean NOT NULL default false,
    norm_code varchar(20),
    document_category_id uuid,
    CONSTRAINT fk_document_category FOREIGN KEY (document_category_id) REFERENCES document_category (id)
  );

CREATE TABLE if not exists
  region (
    id uuid NOT NULL primary key,
    code varchar(255),
    label varchar(255),
    UNIQUE (code)
  );

CREATE TABLE if not exists
  norm_abbreviation_document_type (
    norm_abbreviation_id uuid NOT NULL,
    document_type_id uuid NOT NULL,
    PRIMARY KEY (norm_abbreviation_id, document_type_id),
    CONSTRAINT fk_document_type FOREIGN KEY (document_type_id) REFERENCES document_type (id),
    CONSTRAINT fk_norm_abbreviation FOREIGN KEY (norm_abbreviation_id) REFERENCES norm_abbreviation (id)
  );

CREATE TABLE if not exists
  norm_abbreviation_region (
    norm_abbreviation_id uuid NOT NULL,
    region_id uuid NOT NULL,
    PRIMARY KEY (norm_abbreviation_id, region_id),
    CONSTRAINT fk_norm_abbreviation FOREIGN KEY (norm_abbreviation_id) REFERENCES norm_abbreviation (id),
    CONSTRAINT fk_region FOREIGN KEY (region_id) REFERENCES region (id)
  );
