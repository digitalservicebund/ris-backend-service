ALTER TABLE
  norms
ADD
  officialShortTitle VARCHAR(255) NULL,
ADD
  officialAbbreviation VARCHAR(255) NULL,
ADD
  referenceNumber VARCHAR(255) NULL,
ADD
  publicationDate DATE NULL,
ADD
  announcementDate DATE NULL,
ADD
  citationDate DATE NULL,
ADD
  frameKeywords VARCHAR(255) NULL,
ADD
  authorEntity VARCHAR(255) NULL,
ADD
  authorDecidingBody VARCHAR(255) NULL,
ADD
  authorIsResolutionMajority BOOLEAN DEFAULT FALSE,
ADD
  leadJurisdiction VARCHAR(255) NULL,
ADD
  leadUnit VARCHAR(255) NULL,
ADD
  participationType VARCHAR(255) NULL,
ADD
  participationInstitution VARCHAR(255) NULL,
ADD
  documentTypeName VARCHAR(255) NULL,
ADD
  documentNormCategory VARCHAR(255) NULL,
ADD
  documentTemplateName VARCHAR(255) NULL,
ADD
  subjectFna VARCHAR(255) NULL,
ADD
  subjectPreviousFna VARCHAR(255) NULL,
ADD
  subjectGesta VARCHAR(255) NULL,
ADD
  subjectBgb3 VARCHAR(255) NULL;
