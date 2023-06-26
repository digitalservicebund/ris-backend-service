CREATE TABLE IF NOT EXISTS
  publish_report_attachment (
    id uuid PRIMARY KEY,
    content TEXT,
    received_date TIMESTAMP WITH TIME ZONE NOT NULL,
    document_unit_id UUID,
    CONSTRAINT fk_document_unit FOREIGN KEY (document_unit_id) REFERENCES doc_unit (uuid) ON DELETE CASCADE
  );
