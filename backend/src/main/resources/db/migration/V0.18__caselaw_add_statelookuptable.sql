CREATE TABLE IF NOT EXISTS
  lookuptable_state (
    id BIGSERIAL NOT NULL PRIMARY KEY,
    changeindicator CHAR(1),
    version VARCHAR(255),
    jurisshortcut VARCHAR(255),
    label VARCHAR(255)
  );
