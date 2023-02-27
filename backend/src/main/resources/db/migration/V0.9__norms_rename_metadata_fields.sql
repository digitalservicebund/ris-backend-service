ALTER TABLE
  norms
RENAME COLUMN
  officialShortTitle TO official_short_title;

ALTER TABLE
  norms
RENAME COLUMN
  officialAbbreviation TO official_abbreviation;

ALTER TABLE
  norms
RENAME COLUMN
  referenceNumber TO reference_number;

ALTER TABLE
  norms
RENAME COLUMN
  publicationDate TO publication_date;

ALTER TABLE
  norms
RENAME COLUMN
  announcementDate TO announcement_date;

ALTER TABLE
  norms
RENAME COLUMN
  citationDate TO citation_date;

ALTER TABLE
  norms
RENAME COLUMN
  frameKeywords TO frame_keywords;

ALTER TABLE
  norms
RENAME COLUMN
  authorEntity TO author_entity;

ALTER TABLE
  norms
RENAME COLUMN
  authorDecidingBody TO author_deciding_body;

ALTER TABLE
  norms
RENAME COLUMN
  authorIsResolutionMajority TO author_is_resolution_majority;

ALTER TABLE
  norms
RENAME COLUMN
  leadJurisdiction TO lead_jurisdiction;

ALTER TABLE
  norms
RENAME COLUMN
  leadUnit TO lead_unit;

ALTER TABLE
  norms
RENAME COLUMN
  participationType TO participation_type;

ALTER TABLE
  norms
RENAME COLUMN
  participationInstitution TO participation_institution;

ALTER TABLE
  norms
RENAME COLUMN
  documentTypeName TO document_type_name;

ALTER TABLE
  norms
RENAME COLUMN
  documentNormCategory TO document_norm_category;

ALTER TABLE
  norms
RENAME COLUMN
  documentTemplateName TO document_template_name;

ALTER TABLE
  norms
RENAME COLUMN
  subjectFna TO subject_fna;

ALTER TABLE
  norms
RENAME COLUMN
  subjectPreviousFna TO subject_previous_fna;

ALTER TABLE
  norms
RENAME COLUMN
  subjectGesta TO subject_gesta;

ALTER TABLE
  norms
RENAME COLUMN
  subjectBgb3 TO subject_bgb3;
