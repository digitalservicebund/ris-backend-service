drop view
  if exists search_documentation_unit;

create view
  search_documentation_unit as (
    with
      last_status as (
        select distinct
          on (document_unit_id) publication_status,
          with_error,
          document_unit_id,
          created_at
        from
          status
        order by
          document_unit_id desc,
          created_at desc
      ),
      first_file_number as (
        select distinct
          on (document_unit_id) id,
          document_unit_id,
          file_number.file_number
        from
          file_number
        order by
          document_unit_id desc,
          id
      )
    select
      du.id,
      du.uuid,
      du.documentnumber as document_number,
      du.data_source,
      du.filename as file_name,
      du.gerichtstyp as court_type,
      du.gerichtssitz as court_location,
      du.decision_date,
      du.documentation_office_id,
      s.publication_status,
      s.with_error,
      fn.file_number as first_file_number,
      dt.juris_shortcut as document_type
    from
      doc_unit du
      left outer join last_status s on du.uuid = s.document_unit_id
      left outer join first_file_number fn on du.id = fn.document_unit_id
      left outer join lookuptable_documenttype dt on du.document_type_id = dt.id
    where
      du.data_source in ('MIGRATION', 'NEURIS')
    order by
      du.id desc
  );
