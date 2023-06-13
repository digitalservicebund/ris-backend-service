INSERT INTO
  documentation_unit_link (
    parent_documentation_unit_uuid,
    child_documentation_unit_uuid,
    type
  )
SELECT
  dp.uuid,
  dc.uuid,
  'PREVIOUS_DECISION'
FROM
  proceeding_decision_link pdl
  INNER JOIN doc_unit dc ON pdl.child_document_unit_id = dc.id
  INNER JOIN doc_unit dp ON pdl.parent_document_unit_id = dp.id;
