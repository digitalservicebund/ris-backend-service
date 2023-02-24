ALTER TABLE
  xml_mail
ADD COLUMN IF NOT EXISTS
  publish_state VARCHAR(255) default 'UNKNOWN';
