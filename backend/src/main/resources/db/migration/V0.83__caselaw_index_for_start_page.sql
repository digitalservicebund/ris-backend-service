CREATE INDEX
  IF NOT EXISTS document_unit_creationtimestamp_idx ON doc_unit (creationtimestamp);

CREATE INDEX
  IF NOT EXISTS document_unit_datasource_idx ON doc_unit (data_source);
