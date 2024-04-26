ALTER TABLE IF EXISTS
  original_file_document
RENAME TO
  attachment;

ALTER TABLE IF EXISTS
  attachment
RENAME COLUMN
  extension TO format;
