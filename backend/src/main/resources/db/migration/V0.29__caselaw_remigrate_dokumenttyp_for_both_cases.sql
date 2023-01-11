UPDATE
  doc_unit
SET
  document_type_id = (
    SELECT
      id
    FROM
      lookuptable_documenttype ldoctype
    WHERE
      (
        ldoctype.juris_shortcut = doc_unit.dokumenttyp
        OR ldoctype.label = doc_unit.dokumenttyp
      )
      AND ldoctype.document_type = 'R'
    LIMIT
      1
  );
