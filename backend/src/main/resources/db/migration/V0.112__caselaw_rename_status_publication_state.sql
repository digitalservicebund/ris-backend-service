ALTER TABLE IF EXISTS
  publication_status
RENAME TO
  status;

ALTER TABLE
  status
RENAME COLUMN
  status TO publication_status;
