CREATE TABLE IF NOT EXISTS
  exporter_html_report (
    id BIGSERIAL NOT NULL PRIMARY KEY,
    document_unit_id BIGINT NOT NULL,
    html TEXT,
    -- mail_subject VARCHAR(256) NOT NULL,
    -- file_name VARCHAR(50) NOT NULL,
    received_date TIMESTAMP NOT NULL
  );
