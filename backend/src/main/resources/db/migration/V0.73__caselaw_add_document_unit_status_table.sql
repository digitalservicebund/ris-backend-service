CREATE TABLE IF NOT EXISTS
  document_unit_status (
    id uuid NOT NULL PRIMARY KEY,
    created_at TIMESTAMP DEFAULT (now()),
    status varchar(255),
    document_unit_id UUID,
    CONSTRAINT fk_document_unit FOREIGN KEY (document_unit_id) REFERENCES doc_unit (uuid) ON DELETE CASCADE
  );

CREATE VIEW
  document_unit_with_latest_status AS
SELECT
  doc_unit.*,
  latest_status.status,
  latest_status.created_at AS status_created_at
FROM
  doc_unit
  LEFT JOIN LATERAL (
    SELECT
      status,
      created_at
    FROM
      document_unit_status
    WHERE
      document_unit_status.document_unit_id = doc_unit.uuid
    ORDER BY
      document_unit_status.created_at DESC
    LIMIT
      1
  ) AS latest_status ON TRUE;
