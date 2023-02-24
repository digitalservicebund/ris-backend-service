ALTER TABLE
  norms
RENAME COLUMN
  long_title TO official_long_title;

ALTER TABLE
  norms
RENAME COLUMN
  unofficial_title TO unofficial_long_title;

ALTER TABLE
  norms
RENAME COLUMN
  author_entity TO provider_entity;

ALTER TABLE
  norms
RENAME COLUMN
  author_deciding_body TO provider_deciding_body;

ALTER TABLE
  norms
RENAME COLUMN
  author_is_resolution_majority TO provider_is_resolution_majority;
