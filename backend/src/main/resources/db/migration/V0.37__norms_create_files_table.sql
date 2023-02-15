CREATE TABLE
  files (
    id SERIAL PRIMARY KEY,
    hash VARCHAR(255),
    name VARCHAR(255),
    norm_id int
  );
