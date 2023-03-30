CREATE TABLE IF NOT EXISTS
  metadata_sections (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    order_number INT,
    norm_id INT,
    section_id INT NULL
  );
