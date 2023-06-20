--- NORM --
-- 1. Dropping the original primary key of id
ALTER TABLE
  norms
DROP
  CONSTRAINT norms_pkey;

-- 2. Creating new primary key for guid column
ALTER TABLE
  norms
ADD
  PRIMARY KEY (guid);

--- ARTICLES --
-- 1. Dropping the original primary key of id
ALTER TABLE
  articles
DROP
  CONSTRAINT articles_pkey;

-- 2. Creating new primary key for guid column
ALTER TABLE
  articles
ADD
  PRIMARY KEY (guid);

-- 3. Drop old column id
ALTER TABLE
  articles
DROP COLUMN
  id;

-- 4. Drop old column norm_id
ALTER TABLE
  articles
DROP COLUMN
  norm_id;

--- PARAGRAPHS --
-- 1. Dropping the original primary key of id
ALTER TABLE
  paragraphs
DROP
  CONSTRAINT paragraphs_pkey;

-- 2. Creating new primary key for guid column
ALTER TABLE
  paragraphs
ADD
  PRIMARY KEY (guid);

-- 3. Drop old column id
ALTER TABLE
  paragraphs
DROP COLUMN
  id;

-- 4. Drop old column article_id
ALTER TABLE
  paragraphs
DROP COLUMN
  article_id;

--- METADATA_SECTIONS --
-- 1. Dropping the original primary key of id
ALTER TABLE
  metadata_sections
DROP
  CONSTRAINT metadata_sections_pkey;

-- 2. Creating new primary key for guid column
ALTER TABLE
  metadata_sections
ADD
  PRIMARY KEY (guid);

-- 3. Creating new column norm_guid
ALTER TABLE
  metadata_sections
ADD COLUMN
  norm_guid uuid;

-- 4. Set norm_guid by id
UPDATE
  metadata_sections
SET
  norm_guid = (
    SELECT
      guid
    FROM
      norms
    WHERE
      norms.id = metadata_sections.norm_id
  );

-- 5. Drop old column id
ALTER TABLE
  metadata_sections
DROP COLUMN
  id;

-- 6. Drop old column section_id
ALTER TABLE
  metadata_sections
DROP COLUMN
  section_id;

-- 7. Drop old column norm_id
ALTER TABLE
  metadata_sections
DROP COLUMN
  norm_id;

-- 8. Drop also id column of norms table
ALTER TABLE
  norms
DROP COLUMN
  id;

--- METADATA --
-- 1. Dropping the original primary key of id
ALTER TABLE
  metadata
DROP
  CONSTRAINT metadata_pkey;

-- 2. Creating new primary key for guid column
ALTER TABLE
  metadata
ADD
  PRIMARY KEY (guid);

-- 3. Drop old column id
ALTER TABLE
  metadata
DROP COLUMN
  id;

-- 4. Drop old column section_id
ALTER TABLE
  metadata
DROP COLUMN
  section_id;
