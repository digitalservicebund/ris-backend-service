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

CREATE TABLE IF NOT EXISTS
  incremental_migration.documentation_office (
    id UUID PRIMARY KEY,
    abbreviation VARCHAR(20) CONSTRAINT uc_documentation_office_abbreviation UNIQUE
  );

INSERT INTO
  incremental_migration.documentation_office (id, abbreviation)
VALUES
  ('41e62dbc-e5b6-414f-91e2-0cfe559447d1', 'BGH'),
  ('f13c2fdb-5323-49aa-bc6d-09fa68c3acb9', 'CC-RIS'),
  ('ba90a851-3c54-4858-b4fa-7742ffbe8f05', 'DS');

CREATE TABLE IF NOT EXISTS
  incremental_migration.deviating_court (
    id UUID PRIMARY KEY,
    value VARCHAR(225) NOT NULL,
    rank int,
    documentation_unit_id UUID NOT NULL CONSTRAINT fk_documentation_unit REFERENCES incremental_migration.documentation_unit
  );

CREATE TABLE IF NOT EXISTS
  incremental_migration.deviating_date (
    id UUID PRIMARY KEY,
    value DATE NOT NULL,
    rank int,
    documentation_unit_id UUID NOT NULL CONSTRAINT fk_documentation_unit REFERENCES incremental_migration.documentation_unit
  );

CREATE TABLE IF NOT EXISTS
  incremental_migration.deviating_ecli (
    id UUID PRIMARY KEY,
    value VARCHAR(255) NOT NULL,
    rank int,
    documentation_unit_id UUID NOT NULL CONSTRAINT fk_documentation_unit REFERENCES incremental_migration.documentation_unit
  );

CREATE TABLE IF NOT EXISTS
  incremental_migration.deviating_file_number (
    id UUID PRIMARY KEY,
    value VARCHAR(255) NOT NULL,
    rank int,
    documentation_unit_id UUID NOT NULL CONSTRAINT fk_documentation_unit REFERENCES incremental_migration.documentation_unit
  );

create table if not exists
  incremental_migration.keyword (
    id uuid not null primary key,
    value varchar(1000) not null constraint uc_keyword_value unique
  );

create table if not exists
  incremental_migration.documentation_unit_keyword (
    documentation_unit_id uuid not null constraint fk_documentation_unit references incremental_migration.documentation_unit,
    keyword_id uuid not null constraint fk_keyword references incremental_migration.keyword,
    primary key (documentation_unit_id, keyword_id)
  );

create table if not exists
  incremental_migration.address (
    id uuid not null primary key,
    city varchar(255),
    email varchar(255),
    fax_number varchar(255),
    phone_number varchar(255),
    post_office_box varchar(255),
    post_office_box_location varchar(255),
    post_office_box_postal_code varchar(255),
    postal_code varchar(255),
    street varchar(255),
    url varchar(255)
  );

create table if not exists
  incremental_migration.court (
    id uuid not null primary key,
    additional_information varchar(1000),
    belongs_to varchar(255),
    belongs_to_branch varchar(255),
    can_deliver_lrs boolean,
    current_branch varchar(255),
    deprecated_branch varchar(255),
    deprecated_since date,
    early_name varchar(255),
    exists_since date,
    field varchar(255),
    is_foreign_court boolean default false not null,
    is_superior_court boolean default false not null,
    juris_id integer not null constraint uc_court_juris_id unique,
    late_name varchar(255),
    location varchar(255),
    official_name varchar(255),
    remark varchar(255),
    traditional_name varchar(255),
    type
      varchar(255),
      address_id uuid constraint fk_address references incremental_migration.address
  );

create table if not exists
  incremental_migration.court_region (
    court_id uuid not null constraint fk_court references incremental_migration.court,
    region_id uuid not null constraint fk_region references incremental_migration.region,
    primary key (court_id, region_id)
  );

create table if not exists
  incremental_migration.judicial_body (
    id uuid not null primary key,
    name varchar(255) not null,
    court_id uuid not null constraint fk_court references incremental_migration.court
  );

create table if not exists
  incremental_migration.numeric_figure (
    judicial_body_id uuid unique constraint fk_judicial_body references incremental_migration.judicial_body,
    from_value varchar(255) not null,
    to_value varchar(255) not null,
    type
      varchar(255) not null
  );

create table if not exists
  incremental_migration.court_synonym (
    id uuid not null primary key,
    label varchar(255) not null,
    type
      varchar(255) not null,
      court_id uuid not null constraint fk_court references incremental_migration.court
  );

create table
  incremental_migration.citation_type (
    id uuid not null primary key,
    abbreviation varchar(255) not null,
    label varchar(255),
    documentation_unit_document_category_id uuid constraint fk_documentation_unit_document_category references incremental_migration.document_category,
    citation_document_category_id uuid constraint fk_citation_document_category references incremental_migration.document_category,
    juris_id integer not null constraint uc_citation_type_juris_id unique
  );

create table
  incremental_migration.related_documentation (
    id uuid not null primary key,
    court_location varchar(255),
    court_type varchar(255),
    court_id uuid constraint fk_court references incremental_migration.court,
    date date,
    document_number varchar(255),
    document_type_id uuid constraint fk_document_type references incremental_migration.document_type,
    file_number varchar(255),
    citation_type_id uuid constraint fk_citation_type references incremental_migration.citation_type,
    note text,
    dtype varchar(31) not null,
    documentation_unit_id uuid constraint fk_documentation_unit references incremental_migration.documentation_unit,
    document_type_raw_value varchar(255)
  );

create type
  incremental_migration.notation as enum('OLD', 'NEW');

create table
  incremental_migration.field_of_law (
    id uuid not null primary key,
    identifier varchar(255) not null constraint uc_field_of_law_identifier unique,
    text varchar(1023) not null,
    juris_id integer not null,
    notation incremental_migration.notation not null,
    constraint uc_field_of_law_juris_id_notation unique (juris_id, notation)
  );

create table
  incremental_migration.field_of_law_keyword (
    id uuid not null primary key,
    value varchar(255) not null constraint uc_field_of_law_keyword unique
  );

create table
  incremental_migration.field_of_law_navigation_term (
    id uuid not null primary key,
    value varchar(1023) not null constraint uc_field_of_law_navigation_term unique
  );

create table
  incremental_migration.field_of_law_norm (
    id uuid not null primary key,
    abbreviation varchar(255) not null,
    single_norm_description varchar(255),
    field_of_law_id uuid not null constraint fk_field_of_law references incremental_migration.field_of_law
  );

create index
  field_of_law_norm_field_of_law_id_idx on incremental_migration.field_of_law_norm (field_of_law_id);

create table
  incremental_migration.field_of_law_field_of_law_keyword (
    field_of_law_id uuid not null constraint fk_field_of_law references incremental_migration.field_of_law,
    field_of_law_keyword_id uuid not null constraint fk_field_of_law_keyword references incremental_migration.field_of_law_keyword,
    primary key (field_of_law_id, field_of_law_keyword_id)
  );

create index
  field_of_law_keyword_id_idx on incremental_migration.field_of_law_field_of_law_keyword (field_of_law_keyword_id);

create table
  incremental_migration.field_of_law_field_of_law_navigation_term (
    field_of_law_id uuid not null constraint fk_field_of_law references incremental_migration.field_of_law,
    field_of_law_navigation_term_id uuid not null constraint fk_field_of_law_navigation_term references incremental_migration.field_of_law_navigation_term,
    primary key (field_of_law_id, field_of_law_navigation_term_id)
  );

create index
  field_of_law_navigation_term_id_idx on incremental_migration.field_of_law_field_of_law_navigation_term (field_of_law_navigation_term_id);

create table
  incremental_migration.field_of_law_field_of_law_parent (
    field_of_law_id uuid not null primary key constraint fk_field_of_law references incremental_migration.field_of_law,
    field_of_law_parent_id uuid not null constraint fk_field_of_law_parent references incremental_migration.field_of_law
  );

create index
  field_of_law_parent_id_idx on incremental_migration.field_of_law_field_of_law_parent (field_of_law_parent_id);

create table
  incremental_migration.field_of_law_field_of_law_text_reference (
    field_of_law_id uuid not null constraint fk_field_of_law references incremental_migration.field_of_law,
    field_of_law_text_reference_id uuid not null constraint field_of_law_reference references incremental_migration.field_of_law,
    primary key (field_of_law_id, field_of_law_text_reference_id)
  );

create index
  field_of_law_text_reference_id_idx on incremental_migration.field_of_law_field_of_law_text_reference (field_of_law_text_reference_id);

create table
  incremental_migration.documentation_unit_field_of_law (
    documentation_unit_id uuid not null constraint fk_documentation_unit references incremental_migration.documentation_unit,
    field_of_law_id uuid not null constraint fk_field_of_law references incremental_migration.field_of_law,
    primary key (documentation_unit_id, field_of_law_id)
  );

create index
  documentation_unit_field_of_law_field_of_law_id_idx on incremental_migration.documentation_unit_field_of_law (field_of_law_id);
