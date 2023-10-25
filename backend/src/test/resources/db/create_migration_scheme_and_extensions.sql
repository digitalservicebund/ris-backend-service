CREATE SCHEMA
  IF NOT EXISTS incremental_migration;

CREATE EXTENSION
  IF NOT EXISTS "uuid-ossp";

CREATE EXTENSION
  IF NOT EXISTS "pg_trgm";

CREATE TABLE IF NOT EXISTS
  incremental_migration.region (
    id uuid NOT NULL,
    code character varying(255) COLLATE pg_catalog."default",
    CONSTRAINT region_pkey PRIMARY KEY (id),
    CONSTRAINT uc_region_code UNIQUE (code)
  );

CREATE TABLE IF NOT EXISTS
  incremental_migration.norm_abbreviation (
    id uuid NOT NULL,
    abbreviation character varying(255) COLLATE pg_catalog."default" NOT NULL,
    decision_date date,
    document_id bigint NOT NULL,
    document_number character varying(80) COLLATE pg_catalog."default",
    official_letter_abbreviation character varying(255) COLLATE pg_catalog."default",
    official_long_title text COLLATE pg_catalog."default",
    official_short_title text COLLATE pg_catalog."default",
    source character varying(1) COLLATE pg_catalog."default",
    CONSTRAINT norm_abbreviation_pkey PRIMARY KEY (id),
    CONSTRAINT uc_norm_abbreviation_document_id UNIQUE (document_id)
  );

CREATE TABLE IF NOT EXISTS
  incremental_migration.document_category (
    id uuid NOT NULL,
    label character varying(1) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT document_category_pkey PRIMARY KEY (id),
    CONSTRAINT uc_document_category_label UNIQUE (label)
  );

CREATE TABLE IF NOT EXISTS
  incremental_migration.document_type (
    id uuid NOT NULL,
    abbreviation character varying(32) COLLATE pg_catalog."default",
    label character varying(255) COLLATE pg_catalog."default",
    multiple boolean NOT NULL DEFAULT false,
    super_label_1 character varying(255) COLLATE pg_catalog."default",
    super_label_2 character varying(255) COLLATE pg_catalog."default",
    document_category_id uuid,
    CONSTRAINT document_type_pkey PRIMARY KEY (id),
    CONSTRAINT uc_abbreviation_document_category_id UNIQUE (abbreviation, document_category_id),
    CONSTRAINT fk_document_category FOREIGN KEY (document_category_id) REFERENCES incremental_migration.document_category (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION
  );

CREATE TABLE IF NOT EXISTS
  incremental_migration.norm_abbreviation_document_type (
    norm_abbreviation_id uuid NOT NULL,
    document_type_id uuid NOT NULL,
    CONSTRAINT norm_abbreviation_document_type_pkey PRIMARY KEY (norm_abbreviation_id, document_type_id),
    CONSTRAINT fk_document_type FOREIGN KEY (document_type_id) REFERENCES incremental_migration.document_type (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION,
    CONSTRAINT fk_norm_abbreviation FOREIGN KEY (norm_abbreviation_id) REFERENCES incremental_migration.norm_abbreviation (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION
  );

CREATE TABLE IF NOT EXISTS
  incremental_migration.norm_abbreviation_region (
    norm_abbreviation_id uuid NOT NULL,
    region_id uuid NOT NULL,
    CONSTRAINT norm_abbreviation_region_pkey PRIMARY KEY (norm_abbreviation_id, region_id),
    CONSTRAINT fk_norm_abbreviation FOREIGN KEY (norm_abbreviation_id) REFERENCES incremental_migration.norm_abbreviation (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION,
    CONSTRAINT fk_region FOREIGN KEY (region_id) REFERENCES incremental_migration.region (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION
  );

CREATE TABLE IF NOT EXISTS
  incremental_migration.norm_element (
    id uuid NOT NULL,
    label character varying(255) COLLATE pg_catalog."default" NOT NULL,
    has_number_designation boolean NOT NULL DEFAULT false,
    norm_code character varying(18) COLLATE pg_catalog."default",
    juris_id integer NOT NULL,
    document_category_id uuid,
    CONSTRAINT norm_element_pkey PRIMARY KEY (id),
    CONSTRAINT uc_norm_element_juris_id UNIQUE (juris_id),
    CONSTRAINT fk_document_category FOREIGN KEY (document_category_id) REFERENCES incremental_migration.document_category (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION
  );

create materialized view
  norm_abbreviation_search_migration as
select
  na.*,
  r.code,
  r.id as region_id,
  setweight(to_tsvector('german', na.abbreviation), 'A') || setweight(
    to_tsvector(
      'german',
      coalesce(na.official_letter_abbreviation, '')
    ),
    'B'
  ) || setweight(
    to_tsvector('german', coalesce(na.official_short_title, '')),
    'B'
  ) || setweight(
    to_tsvector('german', coalesce(na.official_long_title, '')),
    'B'
  ) || setweight(to_tsvector('german', coalesce(r.code, '')), 'B') weighted_vector
from
  incremental_migration.norm_abbreviation na
  left join incremental_migration.norm_abbreviation_region nar on na.id = nar.norm_abbreviation_id
  left join incremental_migration.region r on nar.region_id = r.id;

CREATE INDEX
  norm_abbreviation_search_migration_idx ON norm_abbreviation_search_migration USING GIN (weighted_vector);

CREATE TABLE IF NOT EXISTS
  incremental_migration.norm_reference (
    id uuid NOT NULL,
    norm_abbreviation_raw_value character varying(1000) COLLATE pg_catalog."default",
    single_norm character varying(255) COLLATE pg_catalog."default",
    date_of_version date,
    date_of_relevance character varying(255) COLLATE pg_catalog."default",
    documentation_unit_id uuid,
    norm_abbreviation_id uuid,
    date_of_version_raw_value character varying(255) COLLATE pg_catalog."default",
    legacy_doc_unit_id uuid
  );

CREATE TABLE IF NOT EXISTS
  incremental_migration.documentation_unit (
    id uuid NOT NULL,
    case_facts text COLLATE pg_catalog."default",
    court_id uuid,
    decision_date date,
    decision_grounds text COLLATE pg_catalog."default",
    dissenting_opinion text COLLATE pg_catalog."default",
    document_number character varying(255) COLLATE pg_catalog."default" NOT NULL,
    document_type_id uuid,
    documentation_office_id uuid,
    ecli character varying(255) COLLATE pg_catalog."default",
    grounds text COLLATE pg_catalog."default",
    guiding_principle text COLLATE pg_catalog."default",
    headline text COLLATE pg_catalog."default",
    headnote text COLLATE pg_catalog."default",
    input_type character varying(255) COLLATE pg_catalog."default",
    judicial_body character varying(255) COLLATE pg_catalog."default",
    jurisdiction_type_id uuid,
    other_headnote text COLLATE pg_catalog."default",
    other_long_text text COLLATE pg_catalog."default",
    procedure character varying(255) COLLATE pg_catalog."default",
    source character varying(1000) COLLATE pg_catalog."default",
    tenor text COLLATE pg_catalog."default",
    outline text COLLATE pg_catalog."default",
    year_of_dispute character varying(32) COLLATE pg_catalog."default",
    duplicate_check boolean,
    legal_effect character varying(255),
    CONSTRAINT documentation_unit_pkey PRIMARY KEY (id)
  );

CREATE TABLE IF NOT EXISTS
  incremental_migration.file_number (
    id uuid NOT NULL,
    value character varying(1000) COLLATE pg_catalog."default" NOT NULL,
    documentation_unit_id uuid NOT NULL,
    CONSTRAINT file_number_pkey PRIMARY KEY (id)
  );

CREATE TABLE IF NOT EXISTS
  incremental_migration.decision_name (
    id uuid NOT NULL,
    value character varying(1000) COLLATE pg_catalog."default" NOT NULL,
    documentation_unit_id uuid NOT NULL,
    CONSTRAINT decision_name_pkey PRIMARY KEY (id)
  );

CREATE TABLE IF NOT EXISTS
  incremental_migration.documentation_unit_region (
    documentation_unit_id uuid NOT NULL,
    region_id uuid NOT NULL,
    CONSTRAINT documentation_unit_region_pkey PRIMARY KEY (documentation_unit_id, region_id)
  );
