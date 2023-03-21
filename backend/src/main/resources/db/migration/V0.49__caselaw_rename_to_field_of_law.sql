ALTER TABLE
  lookuptable_subject_field
RENAME COLUMN
  subject_field_number TO identifier;

ALTER TABLE
  lookuptable_subject_field
RENAME COLUMN
  subject_field_text TO text;

ALTER TABLE
  lookuptable_subject_field
RENAME TO
  lookuptable_field_of_law;

ALTER TABLE
  lookuptable_subject_field_keyword
RENAME COLUMN
  subject_field_id TO field_of_law_id;

ALTER TABLE
  lookuptable_subject_field_keyword
RENAME TO
  lookuptable_field_of_law_keyword;

ALTER TABLE
  lookuptable_subject_field_norm
RENAME COLUMN
  subject_field_id TO field_of_law_id;

ALTER TABLE
  lookuptable_subject_field_norm
RENAME TO
  lookuptable_field_of_law_norm;

ALTER TABLE
  field_of_law_link
RENAME COLUMN
  field_id TO field_of_law_id;

ALTER TABLE
  field_of_law_link
RENAME COLUMN
  linked_field_id TO linked_field_of_law_id;

ALTER TABLE
  field_of_law_link
RENAME TO
  lookuptable_field_of_law_link;
