INSERT INTO
  incremental_migration.norm_abbreviation (
    id,
    abbreviation,
    decision_date,
    document_id,
    document_number,
    official_letter_abbreviation,
    official_short_title,
    official_long_title,
    source
  )
VALUES
  (
    '33333333-2222-3333-4444-555555555555',
    'norm abbreviation 1',
    '2021-01-01',
    1,
    'document number 1',
    'official letter abbreviation 1',
    'official short title 1',
    'official long title 1',
    'R'
  ),
  (
    '33333333-2222-3333-4444-666666666666',
    'norm abbreviation 2',
    '2021-01-01',
    2,
    'document number 2',
    'official letter abbreviation 1',
    'official short title 1',
    'official long title 1',
    'R'
  );

INSERT INTO
  incremental_migration.legal_force_type (id, abbreviation, label, juris_id)
VALUES
  (
    '11111111-2222-3333-4444-555555555555',
    'legal force type 1',
    'legal force type 1',
    1
  ),
  (
    '11111111-2222-3333-4444-666666666666',
    'legal force type 2',
    'legal force type 2',
    2
  ),
  (
    '11111111-2222-3333-4444-777777777777',
    'legal force type 3',
    'legal force type 3',
    3
  );

INSERT INTO
  incremental_migration.region (id, code, long_text, applicability)
VALUES
  (
    '55555555-2222-3333-4444-555555555555',
    'BY',
    'Bayern',
    true
  ),
  (
    '55555555-2222-3333-4444-666666666666',
    'CH',
    'Schweiz',
    false
  );
