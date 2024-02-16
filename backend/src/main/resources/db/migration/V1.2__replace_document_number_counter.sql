CREATE TABLE IF NOT EXISTS
  document_number (
    documentation_office_abbreviation TEXT not null, --- e.g. bgh, cc-ris
    last_number INT not null default 0,
    constraint document_number_pkey primary key (documentation_office_abbreviation)
  );

/* Todo: drop table if new document number counter for each docoffice implemented
DROP TABLE IF EXISTS
document_number_counter CASCADE;
 */
