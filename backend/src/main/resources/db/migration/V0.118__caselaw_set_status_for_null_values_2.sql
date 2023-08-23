insert into
  status (
    select
      uuid_generate_v4 (),
      now(),
      'TEST_DOC_UNIT',
      du.uuid,
      null,
      false
    from
      doc_unit du
      left join status s on du.uuid = s.document_unit_id
    where
      du.data_source = 'NEURIS'
      and s.publication_status is null
  );

insert into
  status (
    select
      uuid_generate_v4 (),
      now(),
      'JURIS_PUBLISHED',
      du.uuid,
      null,
      false
    from
      doc_unit du
      left join status s on du.uuid = s.document_unit_id
    where
      du.data_source = 'MIGRATION'
      and s.publication_status is null
  );
