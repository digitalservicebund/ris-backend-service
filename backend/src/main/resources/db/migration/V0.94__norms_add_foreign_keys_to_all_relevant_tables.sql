-- ADD FOREIGN KEY CONSTRAINTS
--- NORM --
-- Articles
ALTER TABLE
  articles
ADD
  CONSTRAINT fk_norms_articles FOREIGN KEY (norm_guid) REFERENCES norms (guid) ON DELETE CASCADE;

-- Files
ALTER TABLE
  files
ADD
  CONSTRAINT fk_norms_files FOREIGN KEY (norm_guid) REFERENCES norms (guid) ON DELETE CASCADE;

-- Metadata_sections
ALTER TABLE
  metadata_sections
ADD
  CONSTRAINT fk_norms_metadata_sections FOREIGN KEY (norm_guid) REFERENCES norms (guid) ON DELETE CASCADE;

--- ARTICLES --
-- Paragraphs
ALTER TABLE
  paragraphs
ADD
  CONSTRAINT fk_articles_paragraphs FOREIGN KEY (article_guid) REFERENCES articles (guid) ON DELETE CASCADE;

--- METADATA_SECTIONS --
-- Metadata
ALTER TABLE
  metadata
ADD
  CONSTRAINT fk_sections_metadata FOREIGN KEY (section_guid) REFERENCES metadata_sections (guid) ON DELETE CASCADE;

-- 5. Add foreign key on section_guid to same table metadata_sections
ALTER TABLE
  metadata_sections
ADD
  CONSTRAINT fk_parent_to_child_sections FOREIGN KEY (section_guid) REFERENCES metadata_sections (guid) ON DELETE CASCADE;
