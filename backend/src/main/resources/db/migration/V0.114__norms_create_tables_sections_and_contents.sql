CREATE TABLE IF NOT EXISTS
  sections (
    guid uuid NOT NULL PRIMARY KEY,
    type
      varchar(255) NOT NULL,
      designation varchar(255) NOT NULL,
      header varchar(255),
      order_number INT NOT NULL,
      norm_guid uuid,
      section_guid uuid,
      CONSTRAINT fk_sections_norms FOREIGN KEY (norm_guid) REFERENCES norms (guid) ON DELETE CASCADE
  );

ALTER TABLE IF EXISTS
  sections
ADD
  CONSTRAINT fk_parent_to_child_sections_elements FOREIGN KEY (section_guid) REFERENCES sections (guid) ON DELETE CASCADE;

CREATE TABLE IF NOT EXISTS
  contents (
    guid uuid NOT NULL PRIMARY KEY,
    type
      varchar(255) NOT NULL,
      marker varchar(255),
      text TEXT NOT NULL,
      order_number INT NOT NULL,
      norm_guid uuid,
      section_guid uuid,
      CONSTRAINT fk_contents_norms FOREIGN KEY (norm_guid) REFERENCES norms (guid) ON DELETE CASCADE,
      CONSTRAINT fk_content_section FOREIGN KEY (section_guid) REFERENCES sections (guid) ON DELETE CASCADE
  );
