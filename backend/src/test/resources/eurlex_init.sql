INSERT INTO
  court (id, location, type, juris_id)
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
  eurlex (id, celex, file_number, court_id, status, decision_date, html_link, uri, created_at, updated_at)
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
    '2010-06-21 00:00:00',
    '2010-06-22 00:00:00'
  ),
  (
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaab',
    '62017CB0576',
    'C-77/25',
    'cccccccc-cccc-cccc-cccc-cccccccccccc',
    'NEW',
    '2025-05-09',
    'eurlex-url',
    'decision-uri',
    '2010-06-21 00:00:00',
    '2010-06-22 00:00:00'
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
    '2010-06-20 00:00:00',
    '2010-06-22 00:00:00'
  ),
  (
      'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaad',
      '62017CA0578',
      'C-99/25,C-98/25',
      'cccccccc-cccc-cccc-cccc-cccccccccccc',
      'NEW',
      '2025-05-11',
      'eurlex-url',
      'decision-uri',
      '2010-06-20 00:00:00',
      '2010-06-22 00:00:00'
  ),
  (
      'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaae',
      '62017BA0579',
      'X-01/99',
      'cccccccc-cccc-cccc-cccc-cccccccccccd',
      'NEW',
      '2025-06-12',
      'eurlex-url',
      'decision-uri',
      '2010-06-21 00:00:00',
      null
  ),
  (
      'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaf',
      '62017CA0580',
      'X-02/99',
      'cccccccc-cccc-cccc-cccc-cccccccccccc',
      'NEW',
      '2025-05-11',
      'eurlex-url',
      'decision-uri',
      '2010-06-21 00:00:00',
      null
  ),
  (
      'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaba',
      '62017BA0581',
      'X-03/99',
      'cccccccc-cccc-cccc-cccc-cccccccccccd',
      'NEW',
      '2025-06-12',
      'eurlex-url',
      'decision-uri',
      '2010-06-20 00:00:00',
      null
  ),
  (
      'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaabb',
      '62017CA0582',
      'X-04/99',
      'cccccccc-cccc-cccc-cccc-cccccccccccc',
      'NEW',
      '2025-05-11',
      'eurlex-url',
      'decision-uri',
      '2010-06-20 00:00:00',
      null
  ),
  (
      'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaabc',
      '62017CA0583',
      'C-11/25',
      'cccccccc-cccc-cccc-cccc-cccccccccccc',
      'ASSIGNED',
      '2025-05-14',
      'eurlex-url',
      'decision-uri',
      '2010-06-21 00:00:00',
      null
  ),
  (
      'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaabd',
      '62017CA0584',
      'C-12/25',
      'cccccccc-cccc-cccc-cccc-cccccccccccc',
      'NEW',
      '2025-05-15',
      'eurlex-url',
      null,
      '2010-06-21 00:00:00',
      '2010-06-21 00:00:00'
  );
