insert into
  documentation_unit_link (
    parent_documentation_unit_uuid,
    child_documentation_unit_uuid,
    type
  )
select
  dp.uuid,
  dc.uuid,
  'PREVIOUS_DECISION'
from
  proceeding_decision_link pdl
  inner join doc_unit dc on pdl.child_document_unit_id = dc.id
  inner join doc_unit dp on pdl.parent_document_unit_id = dp.id
  left outer join documentation_unit_link dul on dc.uuid = dul.child_documentation_unit_uuid
  and dp.uuid = dul.parent_documentation_unit_uuid
  and dul.type = 'PREVIOUS_DECISION'
where
  dul.id is null;
