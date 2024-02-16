INSERT INTO
  incremental_migration.document_category (id, label)
VALUES
  ('dd315130-1fda-46d5-bccd-d587cf51c664', 'R');

INSERT INTO
  incremental_migration.citation_type (
    id,
    abbreviation,
    label,
    documentation_unit_document_category_id,
    citation_document_category_id,
    juris_id
  )
VALUES
  (
    '4e768071-1a19-43a1-8ab9-c185adec94bf',
    'Abgrenzung',
    'Abgrenzung',
    'dd315130-1fda-46d5-bccd-d587cf51c664',
    'dd315130-1fda-46d5-bccd-d587cf51c664',
    48
  ),
  (
    '6b4bd747-fce9-4e49-8af4-3fb4f1d3663c',
    'Anschluss',
    'Anschluss',
    'dd315130-1fda-46d5-bccd-d587cf51c664',
    'dd315130-1fda-46d5-bccd-d587cf51c664',
    49
  );
