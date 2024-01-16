CREATE SCHEMA
  IF NOT EXISTS public;

CREATE SCHEMA
  IF NOT EXISTS incremental_migration;

CREATE EXTENSION
  IF NOT EXISTS "uuid-ossp";

CREATE EXTENSION
  IF NOT EXISTS "pg_trgm";

--
-- Name: notation; Type: TYPE; Schema: incremental_migration; Owner: -
--
CREATE TYPE
  incremental_migration.notation AS ENUM('OLD', 'NEW');

SET
  default_table_access_method = heap;

--
-- Name: address; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.address (
    id uuid NOT NULL,
    city character varying(255),
    email character varying(255),
    fax_number character varying(255),
    phone_number character varying(255),
    post_office_box character varying(255),
    post_office_box_location character varying(255),
    post_office_box_postal_code character varying(255),
    postal_code character varying(255),
    street character varying(255),
    url character varying(255)
  );

--
-- Name: administrative_regulation_citation; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.administrative_regulation_citation (
    id uuid NOT NULL,
    dtype character varying(7) NOT NULL,
    citation_type_id uuid,
    citation_type_raw_value character varying(255) NOT NULL,
    administrative_regulation character varying(255) NOT NULL,
    legal_periodical_raw_value character varying(255),
    legal_periodical_id uuid,
    citation character varying(255),
    administrative_regulation_document_number character varying(255),
    documentation_unit_id uuid
  );

--
-- Name: citation_type; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.citation_type (
    id uuid NOT NULL,
    abbreviation character varying(255) NOT NULL,
    label character varying(255) NOT NULL,
    documentation_unit_document_category_id uuid,
    citation_document_category_id uuid,
    juris_id integer NOT NULL
  );

--
-- Name: country_of_origin; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.country_of_origin (
    id uuid NOT NULL,
    value character varying(255) NOT NULL,
    documentation_unit_id uuid NOT NULL
  );

--
-- Name: court; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.court (
    id uuid NOT NULL,
    additional_information character varying(1000),
    belongs_to character varying(255),
    belongs_to_branch character varying(255),
    can_deliver_lrs boolean,
    current_branch character varying(255),
    deprecated_branch character varying(255),
    deprecated_since date,
    early_name character varying(255),
    exists_since date,
    field character varying(255),
    is_foreign_court boolean DEFAULT false NOT NULL,
    is_superior_court boolean DEFAULT false NOT NULL,
    juris_id integer NOT NULL,
    late_name character varying(255),
    location character varying(255),
    official_name character varying(255),
    remark character varying(255),
    traditional_name character varying(255),
    type
      character varying(255),
      address_id uuid
  );

--
-- Name: court_region; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.court_region (court_id uuid NOT NULL, region_id uuid NOT NULL);

--
-- Name: court_synonym; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.court_synonym (
    id uuid NOT NULL,
    label character varying(255) NOT NULL,
    type
      character varying(2) NOT NULL,
      court_id uuid NOT NULL
  );

--
-- Name: decision_name; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.decision_name (
    id uuid NOT NULL,
    value character varying(255) NOT NULL,
    documentation_unit_id uuid NOT NULL
  );

--
-- Name: definition; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.definition (
    id uuid NOT NULL,
    value character varying(255) NOT NULL,
    documentation_unit_id uuid NOT NULL,
    rank integer DEFAULT '-1'::integer NOT NULL
  );

--
-- Name: dependent_literature_citation; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.dependent_literature_citation (
    id uuid NOT NULL,
    author character varying(255) NOT NULL,
    literature_document_number character varying(255),
    citation character varying(255) NOT NULL,
    document_type_id uuid,
    legal_periodical_raw_value character varying(255) NOT NULL,
    legal_periodical_id uuid,
    dtype character varying(7) NOT NULL,
    documentation_unit_id uuid,
    document_type_raw_value character varying(255)
  );

--
-- Name: deviating_court; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.deviating_court (
    id uuid NOT NULL,
    value character varying(255) NOT NULL,
    documentation_unit_id uuid NOT NULL,
    rank integer DEFAULT '-1'::integer NOT NULL
  );

--
-- Name: deviating_date; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.deviating_date (
    id uuid NOT NULL,
    value date NOT NULL,
    documentation_unit_id uuid NOT NULL,
    rank integer DEFAULT '-1'::integer NOT NULL
  );

--
-- Name: deviating_document_number; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.deviating_document_number (
    id uuid NOT NULL,
    value character varying(255) NOT NULL,
    documentation_unit_id uuid NOT NULL,
    rank integer DEFAULT '-1'::integer NOT NULL
  );

--
-- Name: deviating_ecli; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.deviating_ecli (
    id uuid NOT NULL,
    value character varying(255) NOT NULL,
    documentation_unit_id uuid NOT NULL,
    rank integer DEFAULT '-1'::integer NOT NULL
  );

--
-- Name: deviating_file_number; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.deviating_file_number (
    id uuid NOT NULL,
    value character varying(255) NOT NULL,
    documentation_unit_id uuid NOT NULL,
    rank integer DEFAULT '-1'::integer NOT NULL
  );

--
-- Name: document_category; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.document_category (
    id uuid NOT NULL,
    label character varying(1) NOT NULL
  );

--
-- Name: document_type; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.document_type (
    id uuid NOT NULL,
    abbreviation character varying(32),
    label character varying(255),
    multiple boolean DEFAULT false NOT NULL,
    super_label_1 character varying(255),
    super_label_2 character varying(255),
    document_category_id uuid
  );

--
-- Name: documentation_office; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.documentation_office (
    id uuid NOT NULL,
    abbreviation character varying(6)
  );

--
-- Name: documentation_unit; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.documentation_unit (
    id uuid NOT NULL,
    case_facts text,
    court_id uuid,
    decision_date date,
    decision_grounds text,
    dissenting_opinion text,
    document_number character varying(255) NOT NULL,
    document_type_id uuid,
    documentation_office_id uuid NOT NULL,
    ecli character varying(255),
    grounds text,
    guiding_principle text,
    headline text,
    headnote text,
    input_type character varying(255),
    judicial_body character varying(255),
    jurisdiction_type_id uuid,
    other_headnote text,
    other_long_text text,
    procedure character varying(255),
    source character varying(1000),
    tenor text,
    outline text,
    year_of_dispute character varying(4),
    duplicate_check boolean,
    legislative_mandate boolean DEFAULT false NOT NULL,
    legal_effect character varying(255),
    legal_effect_raw_value character varying(255)
  );

--
-- Name: documentation_unit_field_of_law; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.documentation_unit_field_of_law (
    documentation_unit_id uuid NOT NULL,
    field_of_law_id uuid NOT NULL,
    rank integer DEFAULT '-1'::integer NOT NULL
  );

--
-- Name: documentation_unit_keyword; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.documentation_unit_keyword (
    documentation_unit_id uuid NOT NULL,
    keyword_id uuid NOT NULL,
    rank integer DEFAULT '-1'::integer NOT NULL
  );

--
-- Name: documentation_unit_procedure; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.documentation_unit_procedure (
    documentation_unit_id uuid NOT NULL,
    procedure_id uuid NOT NULL,
    rank integer DEFAULT '-1'::integer NOT NULL
  );

--
-- Name: documentation_unit_region; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.documentation_unit_region (
    documentation_unit_id uuid NOT NULL,
    region_id uuid NOT NULL
  );

--
-- Name: field_of_law; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.field_of_law (
    id uuid NOT NULL,
    identifier character varying(255) NOT NULL,
    text character varying(1000) NOT NULL,
    juris_id integer NOT NULL,
    notation incremental_migration.notation NOT NULL
  );

--
-- Name: field_of_law_field_of_law_keyword; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.field_of_law_field_of_law_keyword (
    field_of_law_id uuid NOT NULL,
    field_of_law_keyword_id uuid NOT NULL
  );

--
-- Name: field_of_law_field_of_law_navigation_term; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.field_of_law_field_of_law_navigation_term (
    field_of_law_id uuid NOT NULL,
    field_of_law_navigation_term_id uuid NOT NULL
  );

--
-- Name: field_of_law_field_of_law_parent; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.field_of_law_field_of_law_parent (
    field_of_law_id uuid NOT NULL,
    field_of_law_parent_id uuid NOT NULL
  );

--
-- Name: field_of_law_field_of_law_text_reference; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.field_of_law_field_of_law_text_reference (
    field_of_law_id uuid NOT NULL,
    field_of_law_text_reference_id uuid NOT NULL
  );

--
-- Name: field_of_law_keyword; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.field_of_law_keyword (
    id uuid NOT NULL,
    value character varying(255) NOT NULL
  );

--
-- Name: field_of_law_navigation_term; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.field_of_law_navigation_term (
    id uuid NOT NULL,
    value character varying(255) NOT NULL
  );

--
-- Name: field_of_law_norm; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.field_of_law_norm (
    id uuid NOT NULL,
    abbreviation character varying(255) NOT NULL,
    single_norm_description character varying(255),
    field_of_law_id uuid NOT NULL
  );

--
-- Name: file_number; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.file_number (
    id uuid NOT NULL,
    value character varying(1000) NOT NULL,
    documentation_unit_id uuid NOT NULL,
    rank integer DEFAULT '-1'::integer NOT NULL
  );

--
-- Name: flyway_schema_history; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type
      character varying(20) NOT NULL,
      script character varying(1000) NOT NULL,
      checksum integer,
      installed_by character varying(100) NOT NULL,
      installed_on timestamp without time zone DEFAULT now() NOT NULL,
      execution_time integer NOT NULL,
      success boolean NOT NULL
  );

--
-- Name: incremental_migration_status; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.incremental_migration_status (
    id uuid NOT NULL,
    created_at timestamp with time zone NOT NULL,
    last_historic_import_version character varying(7) NOT NULL,
    last_daily_import_version character varying(10) NOT NULL
  );

--
-- Name: independent_literature_citation; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.independent_literature_citation (
    id uuid NOT NULL,
    literature_document_number character varying(255),
    author character varying(255) NOT NULL,
    book_title character varying(1000) NOT NULL,
    year_of_publication character varying(255) NOT NULL,
    dtype character varying(7) NOT NULL,
    documentation_unit_id uuid
  );

--
-- Name: job_profile; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.job_profile (
    id uuid NOT NULL,
    value character varying(255) NOT NULL,
    documentation_unit_id uuid NOT NULL
  );

--
-- Name: judicial_body; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.judicial_body (
    id uuid NOT NULL,
    name character varying(255) NOT NULL,
    court_id uuid NOT NULL
  );

--
-- Name: jurisdiction_type; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.jurisdiction_type (id uuid NOT NULL, label character varying(255));

--
-- Name: keyword; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.keyword (
    id uuid NOT NULL,
    value character varying(1000) NOT NULL
  );

--
-- Name: legal_force; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.legal_force (
    id uuid NOT NULL,
    legal_force_type_id uuid NOT NULL,
    norm_abbreviation_id uuid,
    norm_abbreviation_raw_value character varying(255) NOT NULL,
    single_norm character varying(255),
    date_of_version date,
    date_of_relevance character varying(4),
    region_id uuid NOT NULL,
    documentation_unit_id uuid NOT NULL,
    rank integer DEFAULT '-1'::integer NOT NULL
  );

--
-- Name: legal_force_type; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.legal_force_type (
    id uuid NOT NULL,
    abbreviation character varying(255) NOT NULL,
    label character varying(255) NOT NULL,
    juris_id integer NOT NULL
  );

--
-- Name: legal_periodical; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.legal_periodical (
    id uuid NOT NULL,
    abbreviation character varying(255) NOT NULL,
    title character varying(255),
    subtitle character varying(255),
    primary_reference boolean,
    evaluation boolean,
    remark text,
    publisher character varying(255),
    editor character varying(255),
    publications_per_year integer,
    citation_style character varying(255),
    responsible_documentation_office_caselaw_id uuid,
    published_from character varying(4),
    published_to character varying(4),
    literature_evaluation_from character varying(4),
    literature_evaluation_to character varying(4),
    literature_evaluation_complete character varying(4),
    caselaw_evaluation_from character varying(4),
    caselaw_evaluation_to character varying(4),
    caselaw_evaluation_complete character varying(4),
    attachment_to character varying(10),
    juris_id integer NOT NULL
  );

--
-- Name: legal_periodical_document_category; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.legal_periodical_document_category (
    legal_periodical_id uuid NOT NULL,
    document_category_id uuid NOT NULL
  );

--
-- Name: legal_periodical_documentation_office; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.legal_periodical_documentation_office (
    legal_periodical_id uuid NOT NULL,
    documentation_office_id uuid NOT NULL
  );

--
-- Name: legal_periodical_keyword; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.legal_periodical_keyword (
    id uuid NOT NULL,
    value character varying(255) NOT NULL
  );

--
-- Name: legal_periodical_legal_periodical_keyword; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.legal_periodical_legal_periodical_keyword (
    legal_periodical_id uuid NOT NULL,
    legal_periodical_keyword_id uuid NOT NULL
  );

--
-- Name: legal_periodical_region; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.legal_periodical_region (
    legal_periodical_id uuid NOT NULL,
    region_id uuid NOT NULL
  );

--
-- Name: legal_periodical_synonym; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.legal_periodical_synonym (
    id uuid NOT NULL,
    label character varying(255) NOT NULL,
    type
      character varying(2) NOT NULL,
      legal_periodical_id uuid NOT NULL
  );

--
-- Name: norm_abbreviation; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.norm_abbreviation (
    id uuid NOT NULL,
    abbreviation character varying(255) NOT NULL,
    decision_date date,
    document_id bigint NOT NULL,
    document_number character varying(80),
    official_letter_abbreviation character varying(255),
    official_long_title text,
    official_short_title text,
    source character varying(1)
  );

--
-- Name: norm_abbreviation_document_type; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.norm_abbreviation_document_type (
    norm_abbreviation_id uuid NOT NULL,
    document_type_id uuid NOT NULL
  );

--
-- Name: norm_abbreviation_region; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.norm_abbreviation_region (
    norm_abbreviation_id uuid NOT NULL,
    region_id uuid NOT NULL
  );

--
-- Name: region; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.region (id uuid NOT NULL, code character varying(255));

--
-- Name: norm_abbreviation_search_migration; Type: MATERIALIZED VIEW; Schema: incremental_migration; Owner: -
--
CREATE MATERIALIZED VIEW
  incremental_migration.norm_abbreviation_search_migration AS
SELECT
  na.id,
  na.abbreviation,
  na.decision_date,
  na.document_id,
  na.document_number,
  na.official_letter_abbreviation,
  na.official_long_title,
  na.official_short_title,
  na.source,
  r.code,
  r.id AS region_id,
  (
    (
      (
        (
          setweight(
            to_tsvector('german'::regconfig, (na.abbreviation)::text),
            'A'::"char"
          ) || setweight(
            to_tsvector(
              'german'::regconfig,
              (
                COALESCE(
                  na.official_letter_abbreviation,
                  ''::character varying
                )
              )::text
            ),
            'B'::"char"
          )
        ) || setweight(
          to_tsvector(
            'german'::regconfig,
            COALESCE(na.official_short_title, ''::text)
          ),
          'B'::"char"
        )
      ) || setweight(
        to_tsvector(
          'german'::regconfig,
          COALESCE(na.official_long_title, ''::text)
        ),
        'B'::"char"
      )
    ) || setweight(
      to_tsvector(
        'german'::regconfig,
        (COALESCE(r.code, ''::character varying))::text
      ),
      'B'::"char"
    )
  ) AS weighted_vector
FROM
  (
    (
      incremental_migration.norm_abbreviation na
      LEFT JOIN incremental_migration.norm_abbreviation_region nar ON ((na.id = nar.norm_abbreviation_id))
    )
    LEFT JOIN incremental_migration.region r ON ((nar.region_id = r.id))
  )
WITH
  NO DATA;

--
-- Name: norm_element; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.norm_element (
    id uuid NOT NULL,
    label character varying(255) NOT NULL,
    has_number_designation boolean DEFAULT false NOT NULL,
    norm_code character varying(17),
    juris_id integer NOT NULL,
    document_category_id uuid
  );

--
-- Name: norm_reference; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.norm_reference (
    id uuid NOT NULL,
    norm_abbreviation_raw_value character varying(255),
    single_norm character varying(255),
    date_of_version date,
    date_of_relevance character varying(4),
    documentation_unit_id uuid,
    norm_abbreviation_id uuid,
    date_of_version_raw_value character varying(255),
    legacy_doc_unit_id uuid,
    rank integer DEFAULT '-1'::integer NOT NULL
  );

--
-- Name: numeric_figure; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.numeric_figure (
    judicial_body_id uuid,
    from_value character varying(255) NOT NULL,
    to_value character varying(255) NOT NULL,
    type
      character varying(255) NOT NULL
  );

--
-- Name: original_xml; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.original_xml (
    path character varying(1000),
    content text,
    documentation_unit_id uuid
  );

--
-- Name: participating_judge; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.participating_judge (
    id uuid NOT NULL,
    name character varying(255) NOT NULL,
    referenced_opinions character varying(255),
    rank integer DEFAULT '-1'::integer NOT NULL,
    documentation_unit_id uuid NOT NULL
  );

--
-- Name: procedure; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.procedure (
    id uuid NOT NULL,
    name character varying(255) NOT NULL,
    created_at timestamp with time zone,
    documentation_office_id uuid NOT NULL
  );

--
-- Name: reference; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.reference (
    id uuid NOT NULL,
    type
      character varying(255),
      reference_supplement character varying(255),
      legal_periodical_id uuid,
      legal_periodical_raw_value character varying(255) NOT NULL,
      citation character varying(255) NOT NULL,
      documentation_unit_id uuid NOT NULL,
      footnote text,
      rank integer DEFAULT '-1'::integer NOT NULL
  );

--
-- Name: related_documentation; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.related_documentation (
    id uuid NOT NULL,
    court_location_raw_value character varying(255),
    court_type_raw_value character varying(255),
    court_id uuid,
    date date,
    document_number character varying(255),
    document_type_id uuid,
    file_number character varying(255),
    deviating_file_number character varying(255),
    citation_type_id uuid,
    note text,
    dtype character varying(24) NOT NULL,
    documentation_unit_id uuid,
    document_type_raw_value character varying(255),
    rank integer DEFAULT '-1'::integer NOT NULL,
    referenced_documentation_unit_id uuid,
    date_known boolean DEFAULT true NOT NULL
  );

--
-- Name: responsible_documentation_office_literature; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.responsible_documentation_office_literature (
    id uuid NOT NULL,
    value character varying(10) NOT NULL,
    legal_periodical_id uuid NOT NULL
  );

--
-- Name: status; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.status (
    id uuid NOT NULL,
    created_at timestamp with time zone,
    publication_status character varying(255) NOT NULL,
    with_error boolean,
    zustand_raw_value character varying(255),
    verarbzustand_raw_value character varying(255),
    issuer_address character varying(255),
    documentation_unit_id uuid NOT NULL
  );

--
-- Name: submission; Type: TABLE; Schema: incremental_migration; Owner: -
--
CREATE TABLE
  incremental_migration.submission (
    id uuid NOT NULL,
    value character varying(255) NOT NULL,
    documentation_unit_id uuid NOT NULL,
    rank integer DEFAULT '-1'::integer NOT NULL
  );

--
-- Name: document_type abbreviation_document_category_id_key; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.document_type
ADD
  CONSTRAINT abbreviation_document_category_id_key UNIQUE (abbreviation, document_category_id);

--
-- Name: address address_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.address
ADD
  CONSTRAINT address_pkey PRIMARY KEY (id);

--
-- Name: administrative_regulation_citation administrative_regulation_citation_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.administrative_regulation_citation
ADD
  CONSTRAINT administrative_regulation_citation_pkey PRIMARY KEY (id);

--
-- Name: citation_type citation_type_juris_id_key; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.citation_type
ADD
  CONSTRAINT citation_type_juris_id_key UNIQUE (juris_id);

--
-- Name: citation_type citation_type_key; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.citation_type
ADD
  CONSTRAINT citation_type_key UNIQUE (
    abbreviation,
    documentation_unit_document_category_id,
    citation_document_category_id
  );

--
-- Name: citation_type citation_type_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.citation_type
ADD
  CONSTRAINT citation_type_pkey PRIMARY KEY (id);

--
-- Name: country_of_origin country_of_origin_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.country_of_origin
ADD
  CONSTRAINT country_of_origin_pkey PRIMARY KEY (id);

--
-- Name: court court_juris_id_key; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.court
ADD
  CONSTRAINT court_juris_id_key UNIQUE (juris_id);

--
-- Name: court court_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.court
ADD
  CONSTRAINT court_pkey PRIMARY KEY (id);

--
-- Name: court_region court_region_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.court_region
ADD
  CONSTRAINT court_region_pkey PRIMARY KEY (court_id, region_id);

--
-- Name: court_synonym court_synonym_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.court_synonym
ADD
  CONSTRAINT court_synonym_pkey PRIMARY KEY (id);

--
-- Name: decision_name decision_name_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.decision_name
ADD
  CONSTRAINT decision_name_pkey PRIMARY KEY (id);

--
-- Name: definition definition_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.definition
ADD
  CONSTRAINT definition_pkey PRIMARY KEY (id);

--
-- Name: dependent_literature_citation dependent_literature_citation_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.dependent_literature_citation
ADD
  CONSTRAINT dependent_literature_citation_pkey PRIMARY KEY (id);

--
-- Name: deviating_court deviating_court_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.deviating_court
ADD
  CONSTRAINT deviating_court_pkey PRIMARY KEY (id);

--
-- Name: deviating_date deviating_date_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.deviating_date
ADD
  CONSTRAINT deviating_date_pkey PRIMARY KEY (id);

--
-- Name: deviating_document_number deviating_document_number_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.deviating_document_number
ADD
  CONSTRAINT deviating_document_number_pkey PRIMARY KEY (id);

--
-- Name: deviating_ecli deviating_ecli_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.deviating_ecli
ADD
  CONSTRAINT deviating_ecli_pkey PRIMARY KEY (id);

--
-- Name: deviating_file_number deviating_file_number_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.deviating_file_number
ADD
  CONSTRAINT deviating_file_number_pkey PRIMARY KEY (id);

--
-- Name: document_category document_category_label_key; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.document_category
ADD
  CONSTRAINT document_category_label_key UNIQUE (label);

--
-- Name: document_category document_category_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.document_category
ADD
  CONSTRAINT document_category_pkey PRIMARY KEY (id);

--
-- Name: documentation_unit document_number_key; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.documentation_unit
ADD
  CONSTRAINT document_number_key UNIQUE (document_number);

--
-- Name: document_type document_type_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.document_type
ADD
  CONSTRAINT document_type_pkey PRIMARY KEY (id);

--
-- Name: documentation_office documentation_office_abbreviation_key; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.documentation_office
ADD
  CONSTRAINT documentation_office_abbreviation_key UNIQUE (abbreviation);

--
-- Name: documentation_office documentation_office_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.documentation_office
ADD
  CONSTRAINT documentation_office_pkey PRIMARY KEY (id);

--
-- Name: documentation_unit_field_of_law documentation_unit_field_of_law_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.documentation_unit_field_of_law
ADD
  CONSTRAINT documentation_unit_field_of_law_pkey PRIMARY KEY (documentation_unit_id, field_of_law_id);

--
-- Name: documentation_unit_procedure documentation_unit_id_procedure_id_rank_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.documentation_unit_procedure
ADD
  CONSTRAINT documentation_unit_id_procedure_id_rank_pkey PRIMARY KEY (documentation_unit_id, procedure_id, rank);

--
-- Name: documentation_unit_keyword documentation_unit_keyword_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.documentation_unit_keyword
ADD
  CONSTRAINT documentation_unit_keyword_pkey PRIMARY KEY (documentation_unit_id, keyword_id);

--
-- Name: documentation_unit documentation_unit_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.documentation_unit
ADD
  CONSTRAINT documentation_unit_pkey PRIMARY KEY (id);

--
-- Name: documentation_unit_region documentation_unit_region_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.documentation_unit_region
ADD
  CONSTRAINT documentation_unit_region_pkey PRIMARY KEY (documentation_unit_id, region_id);

--
-- Name: field_of_law_field_of_law_keyword field_of_law_field_of_law_keyword_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.field_of_law_field_of_law_keyword
ADD
  CONSTRAINT field_of_law_field_of_law_keyword_pkey PRIMARY KEY (field_of_law_id, field_of_law_keyword_id);

--
-- Name: field_of_law_field_of_law_navigation_term field_of_law_field_of_law_navigation_term_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.field_of_law_field_of_law_navigation_term
ADD
  CONSTRAINT field_of_law_field_of_law_navigation_term_pkey PRIMARY KEY (field_of_law_id, field_of_law_navigation_term_id);

--
-- Name: field_of_law_field_of_law_parent field_of_law_field_of_law_parent_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.field_of_law_field_of_law_parent
ADD
  CONSTRAINT field_of_law_field_of_law_parent_pkey PRIMARY KEY (field_of_law_id);

--
-- Name: field_of_law_field_of_law_text_reference field_of_law_field_of_law_text_reference_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.field_of_law_field_of_law_text_reference
ADD
  CONSTRAINT field_of_law_field_of_law_text_reference_pkey PRIMARY KEY (field_of_law_id, field_of_law_text_reference_id);

--
-- Name: field_of_law field_of_law_identifier_key; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.field_of_law
ADD
  CONSTRAINT field_of_law_identifier_key UNIQUE (identifier);

--
-- Name: field_of_law field_of_law_juris_id_notation_key; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.field_of_law
ADD
  CONSTRAINT field_of_law_juris_id_notation_key UNIQUE (juris_id, notation);

--
-- Name: field_of_law_keyword field_of_law_keyword_key; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.field_of_law_keyword
ADD
  CONSTRAINT field_of_law_keyword_key UNIQUE (value);

--
-- Name: field_of_law_keyword field_of_law_keyword_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.field_of_law_keyword
ADD
  CONSTRAINT field_of_law_keyword_pkey PRIMARY KEY (id);

--
-- Name: field_of_law_navigation_term field_of_law_navigation_term_key; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.field_of_law_navigation_term
ADD
  CONSTRAINT field_of_law_navigation_term_key UNIQUE (value);

--
-- Name: field_of_law_navigation_term field_of_law_navigation_term_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.field_of_law_navigation_term
ADD
  CONSTRAINT field_of_law_navigation_term_pkey PRIMARY KEY (id);

--
-- Name: field_of_law_norm field_of_law_norm_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.field_of_law_norm
ADD
  CONSTRAINT field_of_law_norm_pkey PRIMARY KEY (id);

--
-- Name: field_of_law field_of_law_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.field_of_law
ADD
  CONSTRAINT field_of_law_pkey PRIMARY KEY (id);

--
-- Name: file_number file_number_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.file_number
ADD
  CONSTRAINT file_number_pkey PRIMARY KEY (id);

--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.flyway_schema_history
ADD
  CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);

--
-- Name: incremental_migration_status incremental_migration_status_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.incremental_migration_status
ADD
  CONSTRAINT incremental_migration_status_pkey PRIMARY KEY (id);

--
-- Name: independent_literature_citation independent_literature_citation_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.independent_literature_citation
ADD
  CONSTRAINT independent_literature_citation_pkey PRIMARY KEY (id);

--
-- Name: job_profile job_profile_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.job_profile
ADD
  CONSTRAINT job_profile_pkey PRIMARY KEY (id);

--
-- Name: judicial_body judicial_body_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.judicial_body
ADD
  CONSTRAINT judicial_body_pkey PRIMARY KEY (id);

--
-- Name: jurisdiction_type jurisdiction_label_key; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.jurisdiction_type
ADD
  CONSTRAINT jurisdiction_label_key UNIQUE (label);

--
-- Name: jurisdiction_type jurisdiction_type_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.jurisdiction_type
ADD
  CONSTRAINT jurisdiction_type_pkey PRIMARY KEY (id);

--
-- Name: keyword keyword_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.keyword
ADD
  CONSTRAINT keyword_pkey PRIMARY KEY (id);

--
-- Name: keyword keyword_value_key; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.keyword
ADD
  CONSTRAINT keyword_value_key UNIQUE (value);

--
-- Name: legal_force_type legal_force_juris_id_key; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.legal_force_type
ADD
  CONSTRAINT legal_force_juris_id_key UNIQUE (juris_id);

--
-- Name: legal_force legal_force_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.legal_force
ADD
  CONSTRAINT legal_force_pkey PRIMARY KEY (id);

--
-- Name: legal_force_type legal_force_type_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.legal_force_type
ADD
  CONSTRAINT legal_force_type_pkey PRIMARY KEY (id);

--
-- Name: legal_periodical_document_category legal_periodical_document_category_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.legal_periodical_document_category
ADD
  CONSTRAINT legal_periodical_document_category_pkey PRIMARY KEY (legal_periodical_id, document_category_id);

--
-- Name: legal_periodical_documentation_office legal_periodical_documentation_office_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.legal_periodical_documentation_office
ADD
  CONSTRAINT legal_periodical_documentation_office_pkey PRIMARY KEY (legal_periodical_id, documentation_office_id);

--
-- Name: legal_periodical legal_periodical_juris_id_key; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.legal_periodical
ADD
  CONSTRAINT legal_periodical_juris_id_key UNIQUE (juris_id);

--
-- Name: legal_periodical_keyword legal_periodical_keyword_key; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.legal_periodical_keyword
ADD
  CONSTRAINT legal_periodical_keyword_key UNIQUE (value);

--
-- Name: legal_periodical_keyword legal_periodical_keyword_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.legal_periodical_keyword
ADD
  CONSTRAINT legal_periodical_keyword_pkey PRIMARY KEY (id);

--
-- Name: legal_periodical_legal_periodical_keyword legal_periodical_legal_periodical_keyword_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.legal_periodical_legal_periodical_keyword
ADD
  CONSTRAINT legal_periodical_legal_periodical_keyword_pkey PRIMARY KEY (legal_periodical_id, legal_periodical_keyword_id);

--
-- Name: legal_periodical legal_periodical_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.legal_periodical
ADD
  CONSTRAINT legal_periodical_pkey PRIMARY KEY (id);

--
-- Name: legal_periodical_region legal_periodical_region_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.legal_periodical_region
ADD
  CONSTRAINT legal_periodical_region_pkey PRIMARY KEY (legal_periodical_id, region_id);

--
-- Name: legal_periodical_synonym legal_periodical_synonym_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.legal_periodical_synonym
ADD
  CONSTRAINT legal_periodical_synonym_pkey PRIMARY KEY (id);

--
-- Name: norm_abbreviation norm_abbreviation_document_id_key; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.norm_abbreviation
ADD
  CONSTRAINT norm_abbreviation_document_id_key UNIQUE (document_id);

--
-- Name: norm_abbreviation_document_type norm_abbreviation_document_type_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.norm_abbreviation_document_type
ADD
  CONSTRAINT norm_abbreviation_document_type_pkey PRIMARY KEY (norm_abbreviation_id, document_type_id);

--
-- Name: norm_abbreviation norm_abbreviation_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.norm_abbreviation
ADD
  CONSTRAINT norm_abbreviation_pkey PRIMARY KEY (id);

--
-- Name: norm_abbreviation_region norm_abbreviation_region_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.norm_abbreviation_region
ADD
  CONSTRAINT norm_abbreviation_region_pkey PRIMARY KEY (norm_abbreviation_id, region_id);

--
-- Name: norm_element norm_element_juris_id_key; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.norm_element
ADD
  CONSTRAINT norm_element_juris_id_key UNIQUE (juris_id);

--
-- Name: norm_element norm_element_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.norm_element
ADD
  CONSTRAINT norm_element_pkey PRIMARY KEY (id);

--
-- Name: norm_reference norm_reference_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.norm_reference
ADD
  CONSTRAINT norm_reference_pkey PRIMARY KEY (id);

--
-- Name: numeric_figure numeric_figure_judicial_body_id_key; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.numeric_figure
ADD
  CONSTRAINT numeric_figure_judicial_body_id_key UNIQUE (judicial_body_id);

--
-- Name: original_xml original_xml_documentation_unit_id_key; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.original_xml
ADD
  CONSTRAINT original_xml_documentation_unit_id_key UNIQUE (documentation_unit_id);

--
-- Name: participating_judge participating_judge_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.participating_judge
ADD
  CONSTRAINT participating_judge_pkey PRIMARY KEY (id);

--
-- Name: procedure procedure_name_documentation_office_id_key; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.procedure
ADD
  CONSTRAINT procedure_name_documentation_office_id_key UNIQUE (name, documentation_office_id);

--
-- Name: procedure procedure_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.procedure
ADD
  CONSTRAINT procedure_pkey PRIMARY KEY (id);

--
-- Name: reference reference_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.reference
ADD
  CONSTRAINT reference_pkey PRIMARY KEY (id);

--
-- Name: region region_code_key; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.region
ADD
  CONSTRAINT region_code_key UNIQUE (code);

--
-- Name: region region_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.region
ADD
  CONSTRAINT region_pkey PRIMARY KEY (id);

--
-- Name: related_documentation related_documentation_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.related_documentation
ADD
  CONSTRAINT related_documentation_pkey PRIMARY KEY (id);

--
-- Name: responsible_documentation_office_literature responsible_documentation_office_literature_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.responsible_documentation_office_literature
ADD
  CONSTRAINT responsible_documentation_office_literature_pkey PRIMARY KEY (id);

--
-- Name: status status_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.status
ADD
  CONSTRAINT status_pkey PRIMARY KEY (id);

--
-- Name: submission submission_pkey; Type: CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.submission
ADD
  CONSTRAINT submission_pkey PRIMARY KEY (id);

--
-- Name: administrative_regulation_citation_citation_type_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  administrative_regulation_citation_citation_type_id_idx ON incremental_migration.administrative_regulation_citation USING btree (citation_type_id);

--
-- Name: administrative_regulation_citation_documentation_unit_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  administrative_regulation_citation_documentation_unit_id_idx ON incremental_migration.administrative_regulation_citation USING btree (documentation_unit_id);

--
-- Name: administrative_regulation_citation_legal_periodical_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  administrative_regulation_citation_legal_periodical_id_idx ON incremental_migration.administrative_regulation_citation USING btree (legal_periodical_id);

--
-- Name: citation_type_citation_document_category_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  citation_type_citation_document_category_id_idx ON incremental_migration.citation_type USING btree (citation_document_category_id);

--
-- Name: citation_type_documentation_unit_document_category_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  citation_type_documentation_unit_document_category_id_idx ON incremental_migration.citation_type USING btree (documentation_unit_document_category_id);

--
-- Name: country_of_origin_documentation_unit_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  country_of_origin_documentation_unit_id_idx ON incremental_migration.country_of_origin USING btree (documentation_unit_id);

--
-- Name: court_address_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  court_address_id_idx ON incremental_migration.court USING btree (address_id);

--
-- Name: court_region_region_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  court_region_region_id_idx ON incremental_migration.court_region USING btree (region_id);

--
-- Name: court_synonym_court_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  court_synonym_court_id_idx ON incremental_migration.court_synonym USING btree (court_id);

--
-- Name: decision_name_documentation_unit_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  decision_name_documentation_unit_id_idx ON incremental_migration.decision_name USING btree (documentation_unit_id);

--
-- Name: definition_documentation_unit_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  definition_documentation_unit_id_idx ON incremental_migration.definition USING btree (documentation_unit_id);

--
-- Name: dependent_literature_citation_document_type_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  dependent_literature_citation_document_type_id_idx ON incremental_migration.dependent_literature_citation USING btree (document_type_id);

--
-- Name: dependent_literature_citation_documentation_unit_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  dependent_literature_citation_documentation_unit_id_idx ON incremental_migration.dependent_literature_citation USING btree (documentation_unit_id);

--
-- Name: dependent_literature_citation_legal_periodical_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  dependent_literature_citation_legal_periodical_id_idx ON incremental_migration.dependent_literature_citation USING btree (legal_periodical_id);

--
-- Name: deviating_court_documentation_unit_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  deviating_court_documentation_unit_id_idx ON incremental_migration.deviating_court USING btree (documentation_unit_id);

--
-- Name: deviating_date_documentation_unit_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  deviating_date_documentation_unit_id_idx ON incremental_migration.deviating_date USING btree (documentation_unit_id);

--
-- Name: deviating_document_number_documentation_unit_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  deviating_document_number_documentation_unit_id_idx ON incremental_migration.deviating_document_number USING btree (documentation_unit_id);

--
-- Name: deviating_ecli_documentation_unit_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  deviating_ecli_documentation_unit_id_idx ON incremental_migration.deviating_ecli USING btree (documentation_unit_id);

--
-- Name: deviating_file_number_documentation_unit_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  deviating_file_number_documentation_unit_id_idx ON incremental_migration.deviating_file_number USING btree (documentation_unit_id);

--
-- Name: document_type_document_category_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  document_type_document_category_id_idx ON incremental_migration.document_type USING btree (document_category_id);

--
-- Name: documentation_unit_court_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  documentation_unit_court_id_idx ON incremental_migration.documentation_unit USING btree (court_id);

--
-- Name: documentation_unit_decision_date_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  documentation_unit_decision_date_idx ON incremental_migration.documentation_unit USING btree (decision_date);

--
-- Name: documentation_unit_document_type_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  documentation_unit_document_type_id_idx ON incremental_migration.documentation_unit USING btree (document_type_id);

--
-- Name: documentation_unit_documentation_office_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  documentation_unit_documentation_office_id_idx ON incremental_migration.documentation_unit USING btree (documentation_office_id);

--
-- Name: documentation_unit_field_of_law_field_of_law_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  documentation_unit_field_of_law_field_of_law_id_idx ON incremental_migration.documentation_unit_field_of_law USING btree (field_of_law_id);

--- CREATE INDEX documentation_unit_field_of_law_documentation_unit_id_idx ON incremental_migration.documentation_unit_field_of_law USING btree (documentation_unit_id);
--
-- Name: documentation_unit_jurisdiction_type_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  documentation_unit_jurisdiction_type_id_idx ON incremental_migration.documentation_unit USING btree (jurisdiction_type_id);

--
-- Name: documentation_unit_keyword_keyword_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  documentation_unit_keyword_keyword_id_idx ON incremental_migration.documentation_unit_keyword USING btree (keyword_id);

--- CREATE INDEX documentation_unit_keyword_documentation_unit_id_idx ON incremental_migration.documentation_unit_keyword USING btree (documentation_unit_id);
--
-- Name: documentation_unit_procedure_documentation_unit_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  documentation_unit_procedure_documentation_unit_id_idx ON incremental_migration.documentation_unit_procedure USING btree (documentation_unit_id);

--
-- Name: documentation_unit_procedure_procedure_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  documentation_unit_procedure_procedure_id_idx ON incremental_migration.documentation_unit_procedure USING btree (procedure_id);

--
-- Name: documentation_unit_region_region_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  documentation_unit_region_region_id_idx ON incremental_migration.documentation_unit_region USING btree (region_id);

--- CREATE INDEX documentation_unit_region_documentation_unit_id_idx ON incremental_migration.documentation_unit_region USING btree (documentation_unit_id);
--
-- Name: field_of_law_field_of_law_keyword_field_of_law_keyword_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  field_of_law_field_of_law_keyword_field_of_law_keyword_id_idx ON incremental_migration.field_of_law_field_of_law_keyword USING btree (field_of_law_keyword_id);

--
-- Name: field_of_law_field_of_law_nav_field_of_law_navigation_term__idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  field_of_law_field_of_law_nav_field_of_law_navigation_term__idx ON incremental_migration.field_of_law_field_of_law_navigation_term USING btree (field_of_law_navigation_term_id);

--
-- Name: field_of_law_field_of_law_parent_field_of_law_parent_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  field_of_law_field_of_law_parent_field_of_law_parent_id_idx ON incremental_migration.field_of_law_field_of_law_parent USING btree (field_of_law_parent_id);

--
-- Name: field_of_law_field_of_law_tex_field_of_law_text_reference_i_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  field_of_law_field_of_law_tex_field_of_law_text_reference_i_idx ON incremental_migration.field_of_law_field_of_law_text_reference USING btree (field_of_law_text_reference_id);

--
-- Name: field_of_law_norm_field_of_law_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  field_of_law_norm_field_of_law_id_idx ON incremental_migration.field_of_law_norm USING btree (field_of_law_id);

--
-- Name: file_number_documentation_unit_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  file_number_documentation_unit_id_idx ON incremental_migration.file_number USING btree (documentation_unit_id);

--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  flyway_schema_history_s_idx ON incremental_migration.flyway_schema_history USING btree (success);

--
-- Name: independent_literature_citation_documentation_unit_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  independent_literature_citation_documentation_unit_id_idx ON incremental_migration.independent_literature_citation USING btree (documentation_unit_id);

--
-- Name: job_profile_documentation_unit_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  job_profile_documentation_unit_id_idx ON incremental_migration.job_profile USING btree (documentation_unit_id);

--
-- Name: judicial_body_court_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  judicial_body_court_id_idx ON incremental_migration.judicial_body USING btree (court_id);

--
-- Name: legal_force_documentation_unit_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  legal_force_documentation_unit_id_idx ON incremental_migration.legal_force USING btree (documentation_unit_id);

--
-- Name: legal_force_legal_force_type_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  legal_force_legal_force_type_id_idx ON incremental_migration.legal_force USING btree (legal_force_type_id);

--
-- Name: legal_force_norm_abbreviation_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  legal_force_norm_abbreviation_id_idx ON incremental_migration.legal_force USING btree (norm_abbreviation_id);

--
-- Name: legal_force_region_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  legal_force_region_id_idx ON incremental_migration.legal_force USING btree (region_id);

--
-- Name: legal_periodical_document_category_document_category_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  legal_periodical_document_category_document_category_id_idx ON incremental_migration.legal_periodical_document_category USING btree (document_category_id);

--
-- Name: legal_periodical_documentation_offi_documentation_office_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  legal_periodical_documentation_offi_documentation_office_id_idx ON incremental_migration.legal_periodical_documentation_office USING btree (documentation_office_id);

--
-- Name: legal_periodical_legal_periodic_legal_periodical_keyword_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  legal_periodical_legal_periodic_legal_periodical_keyword_id_idx ON incremental_migration.legal_periodical_legal_periodical_keyword USING btree (legal_periodical_keyword_id);

--
-- Name: legal_periodical_region_region_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  legal_periodical_region_region_id_idx ON incremental_migration.legal_periodical_region USING btree (region_id);

--
-- Name: legal_periodical_responsible_documentation_office_caselaw_i_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  legal_periodical_responsible_documentation_office_caselaw_i_idx ON incremental_migration.legal_periodical USING btree (responsible_documentation_office_caselaw_id);

--
-- Name: legal_periodical_synonym_legal_periodical_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  legal_periodical_synonym_legal_periodical_id_idx ON incremental_migration.legal_periodical_synonym USING btree (legal_periodical_id);

--
-- Name: norm_abbreviation_abbreviation_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  norm_abbreviation_abbreviation_idx ON incremental_migration.norm_abbreviation USING btree (abbreviation);

--
-- Name: norm_abbreviation_document_type_document_type_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  norm_abbreviation_document_type_document_type_id_idx ON incremental_migration.norm_abbreviation_document_type USING btree (document_type_id);

--
-- Name: norm_abbreviation_document_type_norm_abbreviation_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  norm_abbreviation_document_type_norm_abbreviation_id_idx ON incremental_migration.norm_abbreviation_document_type USING btree (norm_abbreviation_id);

--
-- Name: norm_abbreviation_region_norm_abbreviation_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  norm_abbreviation_region_norm_abbreviation_id_idx ON incremental_migration.norm_abbreviation_region USING btree (norm_abbreviation_id);

--
-- Name: norm_abbreviation_region_region_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  norm_abbreviation_region_region_id_idx ON incremental_migration.norm_abbreviation_region USING btree (region_id);

--
-- Name: norm_abbreviation_search_migration_weighted_vector_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  norm_abbreviation_search_migration_weighted_vector_idx ON incremental_migration.norm_abbreviation_search_migration USING gin (weighted_vector);

--
-- Name: norm_abbreviation_to_tsvector_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  norm_abbreviation_to_tsvector_idx ON incremental_migration.norm_abbreviation USING gin (
    to_tsvector(
      'german'::regconfig,
      (
        (
          (
            (
              (
                (
                  (COALESCE(abbreviation, ''::character varying))::text || ' '::text
                ) || COALESCE(official_long_title, ''::text)
              ) || ' '::text
            ) || COALESCE(official_short_title, ''::text)
          ) || ' '::text
        ) || (
          COALESCE(
            official_letter_abbreviation,
            ''::character varying
          )
        )::text
      )
    )
  );

--
-- Name: norm_abbreviation_upper_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  norm_abbreviation_upper_idx ON incremental_migration.norm_abbreviation USING gin (upper((abbreviation)::text) public.gin_trgm_ops);

--
-- Name: norm_abbreviation_upper_idx1; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  norm_abbreviation_upper_idx1 ON incremental_migration.norm_abbreviation USING gin (
    upper((official_letter_abbreviation)::text) public.gin_trgm_ops
  );

--
-- Name: norm_element_document_category_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  norm_element_document_category_id_idx ON incremental_migration.norm_element USING btree (document_category_id);

--
-- Name: norm_reference_documentation_unit_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  norm_reference_documentation_unit_id_idx ON incremental_migration.norm_reference USING btree (documentation_unit_id);

--
-- Name: norm_reference_legacy_doc_unit_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  norm_reference_legacy_doc_unit_id_idx ON incremental_migration.norm_reference USING btree (legacy_doc_unit_id);

--
-- Name: norm_reference_norm_abbreviation_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  norm_reference_norm_abbreviation_id_idx ON incremental_migration.norm_reference USING btree (norm_abbreviation_id);

--
-- Name: participating_judge_documentation_unit_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  participating_judge_documentation_unit_id_idx ON incremental_migration.participating_judge USING btree (documentation_unit_id);

--
-- Name: procedure_documentation_office_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  procedure_documentation_office_id_idx ON incremental_migration.procedure USING btree (documentation_office_id);

--
-- Name: reference_documentation_unit_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  reference_documentation_unit_id_idx ON incremental_migration.reference USING btree (documentation_unit_id);

--
-- Name: reference_legal_periodical_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  reference_legal_periodical_id_idx ON incremental_migration.reference USING btree (legal_periodical_id);

--
-- Name: related_documentation_citation_type_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  related_documentation_citation_type_id_idx ON incremental_migration.related_documentation USING btree (citation_type_id);

--
-- Name: related_documentation_court_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  related_documentation_court_id_idx ON incremental_migration.related_documentation USING btree (court_id);

--
-- Name: related_documentation_document_type_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  related_documentation_document_type_id_idx ON incremental_migration.related_documentation USING btree (document_type_id);

--
-- Name: related_documentation_documentation_unit_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  related_documentation_documentation_unit_id_idx ON incremental_migration.related_documentation USING btree (documentation_unit_id);

--- CREATE INDEX related_documentation_referenced_documentation_unit_id_idx ON incremental_migration.related_documentation USING btree (referenced_documentation_unit_id);
--
-- Name: related_documentation_dtype_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  related_documentation_dtype_idx ON incremental_migration.related_documentation USING btree (dtype);

--
-- Name: responsible_documentation_office_litera_legal_periodical_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  responsible_documentation_office_litera_legal_periodical_id_idx ON incremental_migration.responsible_documentation_office_literature USING btree (legal_periodical_id);

--
-- Name: status_documentation_unit_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  status_documentation_unit_id_idx ON incremental_migration.status USING btree (documentation_unit_id);

--
-- Name: status_publication_status_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  status_publication_status_idx ON incremental_migration.status USING btree (publication_status);

--
-- Name: submission_documentation_unit_id_idx; Type: INDEX; Schema: incremental_migration; Owner: -
--
CREATE INDEX
  submission_documentation_unit_id_idx ON incremental_migration.submission USING btree (documentation_unit_id);

--
-- Name: court address_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.court
ADD
  CONSTRAINT address_fkey FOREIGN KEY (address_id) REFERENCES incremental_migration.address (id);

--
-- Name: citation_type citation_document_category_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.citation_type
ADD
  CONSTRAINT citation_document_category_fkey FOREIGN KEY (citation_document_category_id) REFERENCES incremental_migration.document_category (id);

--
-- Name: administrative_regulation_citation citation_type_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.administrative_regulation_citation
ADD
  CONSTRAINT citation_type_fkey FOREIGN KEY (citation_type_id) REFERENCES incremental_migration.citation_type (id);

--
-- Name: related_documentation citation_type_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.related_documentation
ADD
  CONSTRAINT citation_type_fkey FOREIGN KEY (citation_type_id) REFERENCES incremental_migration.citation_type (id);

--
-- Name: court_synonym court_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.court_synonym
ADD
  CONSTRAINT court_fkey FOREIGN KEY (court_id) REFERENCES incremental_migration.court (id);

--
-- Name: judicial_body court_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.judicial_body
ADD
  CONSTRAINT court_fkey FOREIGN KEY (court_id) REFERENCES incremental_migration.court (id);

--
-- Name: documentation_unit court_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.documentation_unit
ADD
  CONSTRAINT court_fkey FOREIGN KEY (court_id) REFERENCES incremental_migration.court (id);

--
-- Name: court_region court_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.court_region
ADD
  CONSTRAINT court_fkey FOREIGN KEY (court_id) REFERENCES incremental_migration.court (id);

--
-- Name: related_documentation court_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.related_documentation
ADD
  CONSTRAINT court_fkey FOREIGN KEY (court_id) REFERENCES incremental_migration.court (id);

--
-- Name: document_type document_category_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.document_type
ADD
  CONSTRAINT document_category_fkey FOREIGN KEY (document_category_id) REFERENCES incremental_migration.document_category (id);

--
-- Name: legal_periodical_document_category document_category_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.legal_periodical_document_category
ADD
  CONSTRAINT document_category_fkey FOREIGN KEY (document_category_id) REFERENCES incremental_migration.document_category (id);

--
-- Name: norm_element document_category_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.norm_element
ADD
  CONSTRAINT document_category_fkey FOREIGN KEY (document_category_id) REFERENCES incremental_migration.document_category (id);

--
-- Name: documentation_unit document_type_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.documentation_unit
ADD
  CONSTRAINT document_type_fkey FOREIGN KEY (document_type_id) REFERENCES incremental_migration.document_type (id);

--
-- Name: dependent_literature_citation document_type_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.dependent_literature_citation
ADD
  CONSTRAINT document_type_fkey FOREIGN KEY (document_type_id) REFERENCES incremental_migration.document_type (id);

--
-- Name: norm_abbreviation_document_type document_type_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.norm_abbreviation_document_type
ADD
  CONSTRAINT document_type_fkey FOREIGN KEY (document_type_id) REFERENCES incremental_migration.document_type (id);

--
-- Name: related_documentation document_type_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.related_documentation
ADD
  CONSTRAINT document_type_fkey FOREIGN KEY (document_type_id) REFERENCES incremental_migration.document_type (id);

--
-- Name: documentation_unit documentation_office_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
--ALTER TABLE ONLY incremental_migration.documentation_unit
--    ADD CONSTRAINT documentation_office_fkey FOREIGN KEY (documentation_office_id) REFERENCES incremental_migration.documentation_office(id);
--
-- Name: legal_periodical documentation_office_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.legal_periodical
ADD
  CONSTRAINT documentation_office_fkey FOREIGN KEY (responsible_documentation_office_caselaw_id) REFERENCES incremental_migration.documentation_office (id);

--
-- Name: legal_periodical_documentation_office documentation_office_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.legal_periodical_documentation_office
ADD
  CONSTRAINT documentation_office_fkey FOREIGN KEY (documentation_office_id) REFERENCES incremental_migration.documentation_office (id);

--
-- Name: procedure documentation_office_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.procedure
ADD
  CONSTRAINT documentation_office_fkey FOREIGN KEY (documentation_office_id) REFERENCES incremental_migration.documentation_office (id);

--
-- Name: citation_type documentation_unit_document_category_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.citation_type
ADD
  CONSTRAINT documentation_unit_document_category_fkey FOREIGN KEY (documentation_unit_document_category_id) REFERENCES incremental_migration.document_category (id);

--
-- Name: country_of_origin documentation_unit_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.country_of_origin
ADD
  CONSTRAINT documentation_unit_fkey FOREIGN KEY (documentation_unit_id) REFERENCES incremental_migration.documentation_unit (id);

--
-- Name: decision_name documentation_unit_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.decision_name
ADD
  CONSTRAINT documentation_unit_fkey FOREIGN KEY (documentation_unit_id) REFERENCES incremental_migration.documentation_unit (id);

--
-- Name: definition documentation_unit_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.definition
ADD
  CONSTRAINT documentation_unit_fkey FOREIGN KEY (documentation_unit_id) REFERENCES incremental_migration.documentation_unit (id);

--
-- Name: deviating_court documentation_unit_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.deviating_court
ADD
  CONSTRAINT documentation_unit_fkey FOREIGN KEY (documentation_unit_id) REFERENCES incremental_migration.documentation_unit (id);

--
-- Name: deviating_date documentation_unit_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.deviating_date
ADD
  CONSTRAINT documentation_unit_fkey FOREIGN KEY (documentation_unit_id) REFERENCES incremental_migration.documentation_unit (id);

--
-- Name: deviating_document_number documentation_unit_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.deviating_document_number
ADD
  CONSTRAINT documentation_unit_fkey FOREIGN KEY (documentation_unit_id) REFERENCES incremental_migration.documentation_unit (id);

--
-- Name: deviating_ecli documentation_unit_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.deviating_ecli
ADD
  CONSTRAINT documentation_unit_fkey FOREIGN KEY (documentation_unit_id) REFERENCES incremental_migration.documentation_unit (id);

--
-- Name: deviating_file_number documentation_unit_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.deviating_file_number
ADD
  CONSTRAINT documentation_unit_fkey FOREIGN KEY (documentation_unit_id) REFERENCES incremental_migration.documentation_unit (id);

--
-- Name: documentation_unit_field_of_law documentation_unit_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.documentation_unit_field_of_law
ADD
  CONSTRAINT documentation_unit_fkey FOREIGN KEY (documentation_unit_id) REFERENCES incremental_migration.documentation_unit (id);

--
-- Name: file_number documentation_unit_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.file_number
ADD
  CONSTRAINT documentation_unit_fkey FOREIGN KEY (documentation_unit_id) REFERENCES incremental_migration.documentation_unit (id);

--
-- Name: independent_literature_citation documentation_unit_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.independent_literature_citation
ADD
  CONSTRAINT documentation_unit_fkey FOREIGN KEY (documentation_unit_id) REFERENCES incremental_migration.documentation_unit (id);

--
-- Name: job_profile documentation_unit_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.job_profile
ADD
  CONSTRAINT documentation_unit_fkey FOREIGN KEY (documentation_unit_id) REFERENCES incremental_migration.documentation_unit (id);

--
-- Name: documentation_unit_keyword documentation_unit_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.documentation_unit_keyword
ADD
  CONSTRAINT documentation_unit_fkey FOREIGN KEY (documentation_unit_id) REFERENCES incremental_migration.documentation_unit (id);

--
-- Name: administrative_regulation_citation documentation_unit_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.administrative_regulation_citation
ADD
  CONSTRAINT documentation_unit_fkey FOREIGN KEY (documentation_unit_id) REFERENCES incremental_migration.documentation_unit (id);

--
-- Name: dependent_literature_citation documentation_unit_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.dependent_literature_citation
ADD
  CONSTRAINT documentation_unit_fkey FOREIGN KEY (documentation_unit_id) REFERENCES incremental_migration.documentation_unit (id);

--
-- Name: documentation_unit_region documentation_unit_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.documentation_unit_region
ADD
  CONSTRAINT documentation_unit_fkey FOREIGN KEY (documentation_unit_id) REFERENCES incremental_migration.documentation_unit (id);

--
-- Name: legal_force documentation_unit_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.legal_force
ADD
  CONSTRAINT documentation_unit_fkey FOREIGN KEY (documentation_unit_id) REFERENCES incremental_migration.documentation_unit (id);

--
-- Name: norm_reference documentation_unit_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.norm_reference
ADD
  CONSTRAINT documentation_unit_fkey FOREIGN KEY (documentation_unit_id) REFERENCES incremental_migration.documentation_unit (id);

--
-- Name: original_xml documentation_unit_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.original_xml
ADD
  CONSTRAINT documentation_unit_fkey FOREIGN KEY (documentation_unit_id) REFERENCES incremental_migration.documentation_unit (id);

--
-- Name: participating_judge documentation_unit_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.participating_judge
ADD
  CONSTRAINT documentation_unit_fkey FOREIGN KEY (documentation_unit_id) REFERENCES incremental_migration.documentation_unit (id);

--
-- Name: documentation_unit_procedure documentation_unit_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.documentation_unit_procedure
ADD
  CONSTRAINT documentation_unit_fkey FOREIGN KEY (documentation_unit_id) REFERENCES incremental_migration.documentation_unit (id);

--
-- Name: reference documentation_unit_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.reference
ADD
  CONSTRAINT documentation_unit_fkey FOREIGN KEY (documentation_unit_id) REFERENCES incremental_migration.documentation_unit (id);

--
-- Name: related_documentation documentation_unit_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.related_documentation
ADD
  CONSTRAINT documentation_unit_fkey FOREIGN KEY (documentation_unit_id) REFERENCES incremental_migration.documentation_unit (id);

--
-- Name: status documentation_unit_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.status
ADD
  CONSTRAINT documentation_unit_fkey FOREIGN KEY (documentation_unit_id) REFERENCES incremental_migration.documentation_unit (id);

--
-- Name: submission documentation_unit_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.submission
ADD
  CONSTRAINT documentation_unit_fkey FOREIGN KEY (documentation_unit_id) REFERENCES incremental_migration.documentation_unit (id);

--
-- Name: field_of_law_field_of_law_parent field_of_law_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.field_of_law_field_of_law_parent
ADD
  CONSTRAINT field_of_law_fkey FOREIGN KEY (field_of_law_id) REFERENCES incremental_migration.field_of_law (id);

--
-- Name: field_of_law_field_of_law_text_reference field_of_law_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.field_of_law_field_of_law_text_reference
ADD
  CONSTRAINT field_of_law_fkey FOREIGN KEY (field_of_law_id) REFERENCES incremental_migration.field_of_law (id);

--
-- Name: field_of_law_field_of_law_keyword field_of_law_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.field_of_law_field_of_law_keyword
ADD
  CONSTRAINT field_of_law_fkey FOREIGN KEY (field_of_law_id) REFERENCES incremental_migration.field_of_law (id);

--
-- Name: field_of_law_field_of_law_navigation_term field_of_law_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.field_of_law_field_of_law_navigation_term
ADD
  CONSTRAINT field_of_law_fkey FOREIGN KEY (field_of_law_id) REFERENCES incremental_migration.field_of_law (id);

--
-- Name: field_of_law_norm field_of_law_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.field_of_law_norm
ADD
  CONSTRAINT field_of_law_fkey FOREIGN KEY (field_of_law_id) REFERENCES incremental_migration.field_of_law (id);

--
-- Name: documentation_unit_field_of_law field_of_law_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.documentation_unit_field_of_law
ADD
  CONSTRAINT field_of_law_fkey FOREIGN KEY (field_of_law_id) REFERENCES incremental_migration.field_of_law (id);

--
-- Name: field_of_law_field_of_law_keyword field_of_law_keyword_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.field_of_law_field_of_law_keyword
ADD
  CONSTRAINT field_of_law_keyword_fkey FOREIGN KEY (field_of_law_keyword_id) REFERENCES incremental_migration.field_of_law_keyword (id);

--
-- Name: field_of_law_field_of_law_navigation_term field_of_law_navigation_term_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.field_of_law_field_of_law_navigation_term
ADD
  CONSTRAINT field_of_law_navigation_term_fkey FOREIGN KEY (field_of_law_navigation_term_id) REFERENCES incremental_migration.field_of_law_navigation_term (id);

--
-- Name: field_of_law_field_of_law_parent field_of_law_parent_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.field_of_law_field_of_law_parent
ADD
  CONSTRAINT field_of_law_parent_fkey FOREIGN KEY (field_of_law_parent_id) REFERENCES incremental_migration.field_of_law (id);

--
-- Name: field_of_law_field_of_law_text_reference field_of_law_text_reference_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.field_of_law_field_of_law_text_reference
ADD
  CONSTRAINT field_of_law_text_reference_fkey FOREIGN KEY (field_of_law_text_reference_id) REFERENCES incremental_migration.field_of_law (id);

--
-- Name: numeric_figure judicial_body_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.numeric_figure
ADD
  CONSTRAINT judicial_body_fkey FOREIGN KEY (judicial_body_id) REFERENCES incremental_migration.judicial_body (id);

--
-- Name: documentation_unit jurisdiction_type_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.documentation_unit
ADD
  CONSTRAINT jurisdiction_type_fkey FOREIGN KEY (jurisdiction_type_id) REFERENCES incremental_migration.jurisdiction_type (id);

--
-- Name: documentation_unit_keyword keyword_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.documentation_unit_keyword
ADD
  CONSTRAINT keyword_fkey FOREIGN KEY (keyword_id) REFERENCES incremental_migration.keyword (id);

--
-- Name: legal_force legal_force_type_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.legal_force
ADD
  CONSTRAINT legal_force_type_fkey FOREIGN KEY (legal_force_type_id) REFERENCES incremental_migration.legal_force_type (id);

--
-- Name: administrative_regulation_citation legal_periodical_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.administrative_regulation_citation
ADD
  CONSTRAINT legal_periodical_fkey FOREIGN KEY (legal_periodical_id) REFERENCES incremental_migration.legal_periodical (id);

--
-- Name: dependent_literature_citation legal_periodical_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.dependent_literature_citation
ADD
  CONSTRAINT legal_periodical_fkey FOREIGN KEY (legal_periodical_id) REFERENCES incremental_migration.legal_periodical (id);

--
-- Name: legal_periodical_document_category legal_periodical_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.legal_periodical_document_category
ADD
  CONSTRAINT legal_periodical_fkey FOREIGN KEY (legal_periodical_id) REFERENCES incremental_migration.legal_periodical (id);

--
-- Name: legal_periodical_documentation_office legal_periodical_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.legal_periodical_documentation_office
ADD
  CONSTRAINT legal_periodical_fkey FOREIGN KEY (legal_periodical_id) REFERENCES incremental_migration.legal_periodical (id);

--
-- Name: legal_periodical_legal_periodical_keyword legal_periodical_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.legal_periodical_legal_periodical_keyword
ADD
  CONSTRAINT legal_periodical_fkey FOREIGN KEY (legal_periodical_id) REFERENCES incremental_migration.legal_periodical (id);

--
-- Name: legal_periodical_synonym legal_periodical_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.legal_periodical_synonym
ADD
  CONSTRAINT legal_periodical_fkey FOREIGN KEY (legal_periodical_id) REFERENCES incremental_migration.legal_periodical (id);

--
-- Name: legal_periodical_region legal_periodical_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.legal_periodical_region
ADD
  CONSTRAINT legal_periodical_fkey FOREIGN KEY (legal_periodical_id) REFERENCES incremental_migration.legal_periodical (id);

--
-- Name: reference legal_periodical_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.reference
ADD
  CONSTRAINT legal_periodical_fkey FOREIGN KEY (legal_periodical_id) REFERENCES incremental_migration.legal_periodical (id);

--
-- Name: responsible_documentation_office_literature legal_periodical_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.responsible_documentation_office_literature
ADD
  CONSTRAINT legal_periodical_fkey FOREIGN KEY (legal_periodical_id) REFERENCES incremental_migration.legal_periodical (id);

--
-- Name: legal_periodical_legal_periodical_keyword legal_periodical_keyword_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.legal_periodical_legal_periodical_keyword
ADD
  CONSTRAINT legal_periodical_keyword_fkey FOREIGN KEY (legal_periodical_keyword_id) REFERENCES incremental_migration.legal_periodical_keyword (id);

--
-- Name: norm_abbreviation_document_type norm_abbreviation_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.norm_abbreviation_document_type
ADD
  CONSTRAINT norm_abbreviation_fkey FOREIGN KEY (norm_abbreviation_id) REFERENCES incremental_migration.norm_abbreviation (id);

--
-- Name: legal_force norm_abbreviation_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.legal_force
ADD
  CONSTRAINT norm_abbreviation_fkey FOREIGN KEY (norm_abbreviation_id) REFERENCES incremental_migration.norm_abbreviation (id);

--
-- Name: norm_abbreviation_region norm_abbreviation_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.norm_abbreviation_region
ADD
  CONSTRAINT norm_abbreviation_fkey FOREIGN KEY (norm_abbreviation_id) REFERENCES incremental_migration.norm_abbreviation (id);

--
-- Name: norm_reference norm_abbreviation_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.norm_reference
ADD
  CONSTRAINT norm_abbreviation_fkey FOREIGN KEY (norm_abbreviation_id) REFERENCES incremental_migration.norm_abbreviation (id);

--
-- Name: documentation_unit_procedure procedure_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.documentation_unit_procedure
ADD
  CONSTRAINT procedure_fkey FOREIGN KEY (procedure_id) REFERENCES incremental_migration.procedure (id);

--
-- Name: court_region region_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.court_region
ADD
  CONSTRAINT region_fkey FOREIGN KEY (region_id) REFERENCES incremental_migration.region (id);

--
-- Name: documentation_unit_region region_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.documentation_unit_region
ADD
  CONSTRAINT region_fkey FOREIGN KEY (region_id) REFERENCES incremental_migration.region (id);

--
-- Name: legal_force region_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.legal_force
ADD
  CONSTRAINT region_fkey FOREIGN KEY (region_id) REFERENCES incremental_migration.region (id);

--
-- Name: legal_periodical_region region_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.legal_periodical_region
ADD
  CONSTRAINT region_fkey FOREIGN KEY (region_id) REFERENCES incremental_migration.region (id);

--
-- Name: norm_abbreviation_region region_fkey; Type: FK CONSTRAINT; Schema: incremental_migration; Owner: -
--
ALTER TABLE ONLY
  incremental_migration.norm_abbreviation_region
ADD
  CONSTRAINT region_fkey FOREIGN KEY (region_id) REFERENCES incremental_migration.region (id);

--- Caselaw tables
CREATE TABLE IF NOT EXISTS
  public.xml_publication (
    mail_subject character varying(256) COLLATE pg_catalog."default" NOT NULL,
    xml text COLLATE pg_catalog."default",
    status_code character varying(20) COLLATE pg_catalog."default",
    status_messages text COLLATE pg_catalog."default",
    file_name character varying(50) COLLATE pg_catalog."default" NOT NULL,
    publish_date timestamp without time zone NOT NULL,
    receiver_address character varying(256) COLLATE pg_catalog."default",
    id uuid NOT NULL,
    documentation_unit_id uuid,
    CONSTRAINT xml_publication_pkey PRIMARY KEY (id)
  );

CREATE TABLE IF NOT EXISTS
  public.publication_report (
    id uuid NOT NULL,
    content text COLLATE pg_catalog."default",
    received_date timestamp without time zone NOT NULL,
    document_unit_id uuid,
    CONSTRAINT publish_report_attachment_pkey PRIMARY KEY (id)
  );

CREATE TABLE IF NOT EXISTS
  public.original_file_document (
    id uuid NOT NULL,
    upload_timestamp timestamp without time zone NOT NULL,
    extension character varying(10) COLLATE pg_catalog."default" NOT NULL,
    filename character varying(255) COLLATE pg_catalog."default" NOT NULL,
    s3_object_path character varying(255) COLLATE pg_catalog."default" NOT NULL,
    documentation_unit_id uuid NOT NULL,
    CONSTRAINT original_file_document_pkey PRIMARY KEY (id)
  );

CREATE TABLE IF NOT EXISTS
  document_number_counter (
    id SERIAL NOT NULL PRIMARY KEY,
    nextnumber INT NOT NULL,
    currentyear INT NOT NULL
  );

INSERT INTO
  document_number_counter (nextnumber, currentyear)
SELECT
  1,
  date_part('year', CURRENT_DATE)
WHERE
  NOT EXISTS (
    SELECT
      id
    FROM
      document_number_counter
  );

--- Test Dokstellen
INSERT INTO
  incremental_migration.documentation_office (id, abbreviation)
VALUES
  ('41e62dbc-e5b6-414f-91e2-0cfe559447d1', 'BGH'),
  ('f13c2fdb-5323-49aa-bc6d-09fa68c3acb9', 'CC-RIS'),
  ('ba90a851-3c54-4858-b4fa-7742ffbe8f05', 'DS');
