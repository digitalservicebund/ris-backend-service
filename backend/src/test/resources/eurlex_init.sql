INSERT INTO
  incremental_migration.court (id, location, type, juris_id)
VALUES
    (
        'cccccccc-cccc-cccc-cccc-cccccccccccc',
        'Luxemburg',
        'EuGH',
        1
    ),
    (
        'cccccccc-cccc-cccc-cccc-cccccccccccd',
        'Luxemburg',
        'EuG',
        2
    );

INSERT INTO
  incremental_migration.eurlex (id, celex, file_number, court_id, status, decision_date, html_link, uri, created_at)
VALUES
  (
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    '62017TB0575',
    'T-66/25',
    'cccccccc-cccc-cccc-cccc-cccccccccccd',
    'NEW',
    '2025-05-10',
    'eurlex-url',
   'decision-uri',
   '2010-06-21 00:00:00'
  ),
  (
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaab',
    '62017CB0576',
    'C-77/25',
    'cccccccc-cccc-cccc-cccc-cccccccccccc',
    'NEW',
    '2025-05-11',
    'eurlex-url',
    'decision-uri',
    '2011-06-21 00:00:00'
  ),
  (
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaac',
    '62017TA0577',
    'T-88/25,T-89/25',
    'cccccccc-cccc-cccc-cccc-cccccccccccd',
    'NEW',
    '2025-05-12',
    'eurlex-url',
    'decision-uri',
    '2012-06-21 00:00:00'
  ),
  (
      'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaad',
      '62017CA0578',
      'C-99/25,C-98/25',
      'cccccccc-cccc-cccc-cccc-cccccccccccc',
      'NEW',
      '2025-05-13',
      'eurlex-url',
      'decision-uri',
      '2013-06-21 00:00:00'
  ),
  (
      'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaae',
      '62017CA0579',
      'C-11/25',
      'cccccccc-cccc-cccc-cccc-cccccccccccc',
      'ASSIGNED',
      '2025-05-14',
      'eurlex-url',
      'decision-uri',
      '2012-06-21 00:00:00'
  ),
  (
      'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaf',
      '62017CA0580',
      'C-12/25',
      'cccccccc-cccc-cccc-cccc-cccccccccccc',
      'NEW',
      '2025-05-15',
      'eurlex-url',
      null,
      '2012-06-21 00:00:00'
  );
