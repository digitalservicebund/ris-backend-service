ALTER TABLE
  procedure_link
ADD COLUMN IF NOT EXISTS
  rank int,
DROP COLUMN IF EXISTS
  created_at;
