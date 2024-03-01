CREATE TABLE IF NOT EXISTS
  deleted_documents (
    document_number VARCHAR(255) NOT NULL,
    year INTEGER NOT NULL,
    abbreviation VARCHAR(255) NOT NULL,
    PRIMARY KEY (document_number)
  );
