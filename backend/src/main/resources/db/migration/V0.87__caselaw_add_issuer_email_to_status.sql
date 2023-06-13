ALTER TABLE
  document_unit_status
ADD COLUMN IF NOT EXISTS
  issuer_address varchar(255);
