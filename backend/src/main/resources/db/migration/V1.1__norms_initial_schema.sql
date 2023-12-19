CREATE TABLE IF NOT EXISTS
  recitals (
    guid uuid NOT NULL,
    marker character varying(255),
    heading character varying(255),
    text text NOT NULL
  );

CREATE TABLE IF NOT EXISTS
  conclusions (
    guid uuid NOT NULL,
    text text NOT NULL,
    CONSTRAINT conclusions_pkey PRIMARY KEY (guid)
  );

CREATE TABLE IF NOT EXISTS
  formulas (
    guid uuid NOT NULL,
    text text NOT NULL,
    CONSTRAINT formulas_pkey PRIMARY KEY (guid)
  );

CREATE TABLE IF NOT EXISTS
  norms (
    guid uuid NOT NULL,
    e_gesetzgebung boolean DEFAULT false NOT NULL,
    recitals_guid uuid,
    formula_guid uuid,
    conclusion_guid uuid,
    CONSTRAINT norms_pkey PRIMARY KEY (guid),
    CONSTRAINT fk_conclusions_guid FOREIGN KEY (conclusion_guid) REFERENCES conclusions (guid) ON DELETE CASCADE,
    CONSTRAINT fk_formulas_guid FOREIGN KEY (formula_guid) REFERENCES formulas (guid) ON DELETE CASCADE,
    CONSTRAINT fk_recitals_guid FOREIGN KEY (recitals_guid) REFERENCES recitals (guid) ON DELETE CASCADE
  );

CREATE TABLE IF NOT EXISTS
  metadata_sections (
    name character varying(255) NOT NULL,
    order_number integer NOT NULL,
    guid uuid NOT NULL,
    section_guid uuid,
    norm_guid uuid NOT NULL,
    CONSTRAINT metadata_sections_pkey PRIMARY KEY (guid),
    CONSTRAINT fk_norms_metadata_sections FOREIGN KEY (norm_guid) REFERENCES norms (guid) ON DELETE CASCADE,
    CONSTRAINT fk_parent_to_child_sections FOREIGN KEY (section_guid) REFERENCES metadata_sections (guid) ON DELETE CASCADE
  );

CREATE TABLE IF NOT EXISTS
  metadata (
    value text NOT NULL,
    type
      character varying(255) NOT NULL,
      order_number integer NOT NULL,
      guid uuid NOT NULL,
      section_guid uuid NOT NULL,
      CONSTRAINT metadata_pkey PRIMARY KEY (guid),
      CONSTRAINT fk_sections_metadata FOREIGN KEY (section_guid) REFERENCES metadata_sections (guid) ON DELETE CASCADE
  );

CREATE TABLE IF NOT EXISTS
  files (
    hash character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    norm_guid uuid NOT NULL,
    guid uuid NOT NULL,
    CONSTRAINT files_pkey PRIMARY KEY (guid),
    CONSTRAINT fk_norms_files FOREIGN KEY (norm_guid) REFERENCES norms (guid) ON DELETE CASCADE
  );

CREATE TABLE IF NOT EXISTS
  document_section (
    guid uuid NOT NULL,
    order_number integer NOT NULL,
    type
      character varying(255) NOT NULL,
      marker character varying(255) NOT NULL,
      heading text NOT NULL,
      norm_guid uuid NOT NULL,
      parent_section_guid uuid,
      CONSTRAINT document_section_pkey PRIMARY KEY (guid),
      CONSTRAINT fk_norm_guid FOREIGN KEY (norm_guid) REFERENCES norms (guid) ON DELETE CASCADE,
      CONSTRAINT fk_parent_section_guid FOREIGN KEY (parent_section_guid) REFERENCES document_section (guid) ON DELETE CASCADE
  );

CREATE TABLE IF NOT EXISTS
  articles (
    guid uuid NOT NULL,
    heading text,
    marker character varying(255) NOT NULL,
    norm_guid uuid,
    order_number integer DEFAULT 0 NOT NULL,
    document_section_guid uuid,
    CONSTRAINT articles_pkey PRIMARY KEY (guid),
    CONSTRAINT fk_document_section_guid FOREIGN KEY (document_section_guid) REFERENCES document_section (guid) ON DELETE CASCADE,
    CONSTRAINT fk_norms_articles FOREIGN KEY (norm_guid) REFERENCES norms (guid) ON DELETE CASCADE
  );

CREATE TABLE IF NOT EXISTS
  paragraphs (
    guid uuid NOT NULL,
    text text NOT NULL,
    marker character varying(255),
    article_guid uuid NOT NULL,
    CONSTRAINT paragraphs_pkey PRIMARY KEY (guid),
    CONSTRAINT fk_articles_paragraphs FOREIGN KEY (article_guid) REFERENCES articles (guid) ON DELETE CASCADE
  );
