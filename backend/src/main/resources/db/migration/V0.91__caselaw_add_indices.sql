CREATE INDEX
  IF NOT EXISTS document_unit_status_document_unit_id_idx ON document_unit_status (document_unit_id);

CREATE INDEX
  IF NOT EXISTS document_unit_status_created_at_idx ON document_unit_status (created_at);

CREATE INDEX
  IF NOT EXISTS document_unit_decision_date_idx ON doc_unit (decision_date);
