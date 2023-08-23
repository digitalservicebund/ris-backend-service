update
  status
set
  publication_status = 'TEST_DOC_UNIT'
where
  document_unit_id in (
    select
      du.uuid
    from
      doc_unit du
      left join status s on du.uuid = s.document_unit_id
    where
      du.data_source = 'NEURIS'
      and s.publication_status is null
  );

update
  status
set
  publication_status = 'JURIS_PUBLISHED'
where
  document_unit_id in (
    select
      du.uuid
    from
      doc_unit du
      left join status s on du.uuid = s.document_unit_id
    where
      du.data_source = 'MIGRATION'
      and s.publication_status is null
  );
