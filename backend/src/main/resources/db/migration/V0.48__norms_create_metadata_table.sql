CREATE TABLE IF NOT EXISTS
  metadata (
    id SERIAL PRIMARY KEY,
    value TEXT,
    type
      VARCHAR(255),
      order_number INT,
      norm_id INT
  );
