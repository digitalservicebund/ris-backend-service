ALTER TABLE
  previous_decision
RENAME COLUMN
  gerichtstyp to court_type;

ALTER TABLE
  previous_decision
RENAME COLUMN
  gerichtsort to court_location;

ALTER TABLE
  previous_decision
RENAME COLUMN
  datum to decision_date;

ALTER TABLE
  previous_decision
RENAME COLUMN
  aktenzeichen to file_number;

ALTER TABLE
  previous_decision
RENAME COLUMN
  documentnumber to document_number;

ALTER TABLE
  previous_decision
ADD COLUMN
  document_unit_id BIGINT;

ALTER TABLE
  previous_decision
ADD
  FOREIGN KEY (document_unit_id) REFERENCES doc_unit (id) ON DELETE CASCADE;
