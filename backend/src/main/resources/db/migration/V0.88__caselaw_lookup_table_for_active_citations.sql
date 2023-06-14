CREATE TABLE if not exists
  citation_style (
    uuid uuid NOT NULL PRIMARY KEY,
    juris_id BIGINT NOT NULL,
    change_indicator VARCHAR(255) NOT NULL,
    change_date_mail DATE,
    version VARCHAR(255) NOT NULL,
    document_type CHAR(1) NOT NULL,
    citation_document_type CHAR(1) NOT NULL,
    juris_shortcut VARCHAR(255) NOT NULL,
    label VARCHAR(255) NOT NULL,
    UNIQUE (juris_id)
  );
