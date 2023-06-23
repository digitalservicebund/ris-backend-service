CREATE TABLE IF NOT EXISTS
  exporter_html_report (
    id uuid NOT NULL primary key,
    document_unit_id UUID NOT NULL,
    html TEXT,
    received_date TIMESTAMP WITH TIME ZONE NOT NULL
  );
