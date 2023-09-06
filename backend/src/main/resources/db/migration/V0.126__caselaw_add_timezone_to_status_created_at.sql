DROP VIEW
  IF EXISTS search_documentation_unit;

ALTER TABLE
  status
ALTER COLUMN
  created_at
TYPE
  TIMESTAMP WITH TIME ZONE;

CREATE VIEW
  search_documentation_unit AS (
    WITH
      last_status AS (
        SELECT DISTINCT
          ON (document_unit_id) publication_status,
          with_error,
          document_unit_id,
          created_at
        FROM
          status
        ORDER BY
          document_unit_id DESC,
          created_at DESC
      ),
      first_file_number AS (
        SELECT DISTINCT
          ON (document_unit_id) id,
          document_unit_id,
          file_number.file_number
        FROM
          file_number
        ORDER BY
          document_unit_id DESC,
          id
      )
    SELECT
      du.id,
      du.uuid,
      du.documentnumber AS document_number,
      du.data_source,
      du.filename AS file_name,
      du.gerichtstyp AS court_type,
      du.gerichtssitz AS court_location,
      du.decision_date,
      du.documentation_office_id,
      s.publication_status,
      s.with_error,
      fn.file_number AS first_file_number,
      dt.juris_shortcut AS document_type
    FROM
      doc_unit du
      LEFT OUTER JOIN last_status s ON du.uuid = s.document_unit_id
      LEFT OUTER JOIN first_file_number fn ON du.id = fn.document_unit_id
      LEFT OUTER JOIN lookuptable_documenttype dt ON du.document_type_id = dt.id
    WHERE
      du.data_source IN ('MIGRATION', 'NEURIS')
    ORDER BY
      du.id DESC
  );
