-- This timestamp updates the checksum, so flyway registers an update and migrates each time
-- ${flyway:timestamp}

INSERT INTO
  documentation_unit (
    id,
    court_id,
    date,
    document_number,
    document_type_id,
    documentation_office_id,
    judicial_body,
    last_publication_date_time,
    scheduled_publication_date_time,
    inbox_status
  )
VALUES
  (
    gen_random_uuid (),
    (
      SELECT
        id
      FROM
        court
      WHERE
      type
        = 'BAG'
    ),
    '1963-01-02',
    'YYTestDoc0001',
    (
      SELECT
        id
      FROM
        document_type
      WHERE
        abbreviation = 'ÄN'
    ),
    (
      SELECT
        id
      FROM
        documentation_office
      WHERE
        abbreviation = 'BAG'
    ),
    '1.Senat, 2. Kammer',
    '2000-11-21 09:42:49.385920',
    NULL,
    NULL
  ),
  (
    gen_random_uuid (),
    NULL,
    '2024-02-02',
    'YYTestDoc0002',
    NULL,
    (
      SELECT
        id
      FROM
        documentation_office
      WHERE
        abbreviation = 'DS'
    ),
    NULL,
    NULL,
    '2100-11-21 09:42:49.385920',
    NULL
  ),
  (
    gen_random_uuid (),
    (
      SELECT
        id
      FROM
        court
      WHERE
      type
        = 'BGH'
    ),
    '1964-10-10',
    'YYTestDoc0003',
    (
      SELECT
        id
      FROM
        document_type
      WHERE
        abbreviation = 'Entscheidung'
    ),
    (
      SELECT
        id
      FROM
        documentation_office
      WHERE
        abbreviation = 'DS'
    ),
    '1.Senat, 2. Kammer',
    NULL,
    '2100-11-30 19:46:49.385920',
    NULL
  ),
  (
    gen_random_uuid (),
    (
      SELECT
        id
      FROM
        court
      WHERE
      type
        = 'AG'
        AND location = 'Bremen'
    ),
    '2022-02-02',
    'YYTestDoc0004',
    (
      SELECT
        id
      FROM
        document_type
      WHERE
        abbreviation = 'Entscheidung'
    ),
    (
      SELECT
        id
      FROM
        documentation_office
      WHERE
        abbreviation = 'DS'
    ),
    '1.Senat, 2. Kammer',
    NULL,
    NULL,
    NULL
  ),
  (
    gen_random_uuid (),
    (
      SELECT
        id
      FROM
        court
      WHERE
      type
        = 'BGH'
    ),
    NULL,
    'YYTestDoc0005',
    (
      SELECT
        id
      FROM
        document_type
      WHERE
        abbreviation = 'Entscheidung'
    ),
    (
      SELECT
        id
      FROM
        documentation_office
      WHERE
        abbreviation = 'DS'
    ),
    '1.Senat, 2. Kammer',
    '2001-11-21 09:42:49.385920',
    NULL,
    NULL
  ),
  (
    gen_random_uuid (),
    NULL,
    '2024-01-02',
    'YYTestDoc0006',
    NULL,
    (
      SELECT
        id
      FROM
        documentation_office
      WHERE
        abbreviation = 'DS'
    ),
    NULL,
    '2002-11-21 09:42:49.385920',
    NULL,
    NULL
  ),
  (
    gen_random_uuid (),
    NULL,
    '1935-12-02',
    'YYTestDoc0007',
    NULL,
    (
      SELECT
        id
      FROM
        documentation_office
      WHERE
        abbreviation = 'DS'
    ),
    NULL,
    NULL,
    NULL,
    NULL
  ),
  (
    gen_random_uuid (),
    NULL,
    '1978-05-28',
    'YYTestDoc0008',
    (
      SELECT
        id
      FROM
        document_type
      WHERE
        abbreviation = 'Entscheidung'
    ),
    (
      SELECT
        id
      FROM
        documentation_office
      WHERE
        abbreviation = 'DS'
    ),
    '1.Senat, 2. Kammer',
    NULL,
    NULL,
    NULL
  ),
  (
    gen_random_uuid (),
    NULL,
    '2022-10-01',
    'YYTestDoc0009',
    (
      SELECT
        id
      FROM
        document_type
      WHERE
        abbreviation = 'BE'
    ),
    (
      SELECT
        id
      FROM
        documentation_office
      WHERE
        abbreviation = 'DS'
    ),
    '1.Senat, 2. Kammer',
    NULL,
    NULL,
    NULL
  ),
  (
    gen_random_uuid (),
    (
      SELECT
        id
      FROM
        court
      WHERE
      type
        = 'BSG'
    ),
    '1990-02-03',
    'YYTestDoc0010',
    (
      SELECT
        id
      FROM
        document_type
      WHERE
        abbreviation = 'BE'
    ),
    (
      SELECT
        id
      FROM
        documentation_office
      WHERE
        abbreviation = 'DS'
    ),
    '1.Senat, 2. Kammer',
    NULL,
    NULL,
    NULL
  ),
  (
    gen_random_uuid (),
    (
      SELECT
        id
      FROM
        court
      WHERE
      type
        = 'BVerfG'
    ),
    '2022-02-03',
    'YYTestDoc0011',
    (
      SELECT
        id
      FROM
        document_type
      WHERE
        abbreviation = 'BR'
    ),
    (
      SELECT
        id
      FROM
        documentation_office
      WHERE
        abbreviation = 'DS'
    ),
    '1.Senat, 2. Kammer',
    NULL,
    NULL,
    NULL
  ),
  (
    gen_random_uuid (),
    (
      SELECT
        id
      FROM
        court
      WHERE
      type
        = 'BVerwG'
    ),
    '1987-09-09',
    'YYTestDoc0012',
    (
      SELECT
        id
      FROM
        document_type
      WHERE
        abbreviation = 'EU'
    ),
    (
      SELECT
        id
      FROM
        documentation_office
      WHERE
        abbreviation = 'DS'
    ),
    '1.Senat, 2. Kammer',
    NULL,
    NULL,
    'EU'
  ),
  (
    gen_random_uuid (),
    (
      SELECT
        id
      FROM
        court
      WHERE
      type
        = 'BVerfG'
    ),
    '2080-02-02',
    'YYTestDoc0013',
    (
      SELECT
        id
      FROM
        document_type
      WHERE
        abbreviation = 'GB'
    ),
    (
      SELECT
        id
      FROM
        documentation_office
      WHERE
        abbreviation = 'DS'
    ),
    '1.Senat, 2. Kammer',
    NULL,
    '2100-11-21 19:42:49.385920',
    NULL
  ),
  (
    gen_random_uuid (),
    (
      SELECT
        id
      FROM
        court
      WHERE
      type
        = 'AG'
        AND location = 'Aachen'
    ),
    '1989-01-01',
    'YYTestDoc0014',
    (
      SELECT
        id
      FROM
        document_type
      WHERE
        abbreviation = 'BE'
    ),
    (
      SELECT
        id
      FROM
        documentation_office
      WHERE
        abbreviation = 'DS'
    ),
    '1.Senat, 2. Kammer',
    NULL,
    '2100-11-21 10:42:49.385920',
    NULL
  ),
  (
    gen_random_uuid (),
    NULL,
    '2001-07-24',
    'YYTestDoc0015',
    (
      SELECT
        id
      FROM
        document_type
      WHERE
        abbreviation = 'BE'
    ),
    (
      SELECT
        id
      FROM
        documentation_office
      WHERE
        abbreviation = 'DS'
    ),
    '1.Senat, 2. Kammer',
    '2005-11-21 09:42:49.385920',
    NULL,
    NULL
  ),
  (
      gen_random_uuid (),
      NULL,
      '2002-08-24',
      'YYTestDoc0016',
      (
          SELECT
              id
          FROM
              document_type
          WHERE
              abbreviation = 'BE'
      ),
      (
          SELECT
              id
          FROM
              documentation_office
          WHERE
              abbreviation = 'BGH'
      ),
      NULL,
      NULL,
      NULL,
    NULL
  ),
    --- pending proceeding ---
    (
      gen_random_uuid (),
      (
          SELECT
              id
          FROM
              court
          WHERE
              type
                  = 'BFH'
      ),
      '2025-02-24',
      'YYTestDoc0017',
      (
          SELECT
              id
          FROM
              document_type
          WHERE
              abbreviation = 'Anh'
      ),
      (
          SELECT
              id
          FROM
              documentation_office
          WHERE
              abbreviation = 'BFH'
      ),
      NULL,
      NULL,
      NULL,
    NULL
  ),
    (
        gen_random_uuid (),
        (
            SELECT
                id
            FROM
                court
            WHERE
                type
                    = 'BFH'
        ),
    '2025-02-24',
    'YYTestDoc0018',
  (
      SELECT
      id
      FROM
      document_type
      WHERE
      abbreviation = 'Anh'
  ),
  (
      SELECT
      id
      FROM
      documentation_office
      WHERE
      abbreviation = 'DS'
  ),
    NULL,
    NULL,
    NULL,
    NULL
    ),
  (
      gen_random_uuid (),
      (
          SELECT
              id
          FROM
              court
          WHERE
              type
                  = 'EuGH'
      ),
      '2002-09-09',
      'YYTestDoc0019',
      (
          SELECT
              id
          FROM
              document_type
          WHERE
              abbreviation = 'EU'
      ),
      (
          SELECT
              id
          FROM
              documentation_office
          WHERE
              abbreviation = 'DS'
      ),
      '1.Senat, 2. Kammer',
      NULL,
      NULL,
      'EU'
  ),
  (
      gen_random_uuid (),
      (
          SELECT
              id
          FROM
              court
          WHERE
              type
                  = 'BVerfG'
      ),
      '2020-02-02',
      'YYTestDoc0020',
      (
          SELECT
              id
          FROM
              document_type
          WHERE
              abbreviation = 'Urt'
      ),
      (
          SELECT
              id
          FROM
              documentation_office
          WHERE
              abbreviation = 'DS'
      ),
      NULL,
      NULL,
      NULL,
      NULL
  );

INSERT INTO
    decision (
    id,
    creating_documentation_office_id,
    guiding_principle,
    procedure,
    tenor
)
VALUES
    (
        (SELECT
        id
        FROM
        documentation_unit
        WHERE
        document_number
        = 'YYTestDoc0001'),
        NULL,
        'guiding principle',
        NULL,
        'tenor'
    ),
    (
        (SELECT
             id
         FROM
             documentation_unit
         WHERE
             document_number
                 = 'YYTestDoc0002'),
        NULL,
        NULL,
        NULL,
        NULL
    ),
    (
        (SELECT
             id
         FROM
             documentation_unit
         WHERE
             document_number
                 = 'YYTestDoc0003'),
        NULL,
        'guiding principle',
        NULL,
        'tenorx'
    ),
    (
        (SELECT
             id
         FROM
             documentation_unit
         WHERE
             document_number
                 = 'YYTestDoc0004'),
        NULL,
        'guiding principle',
        NULL,
        'tenorx'
    ),
    (
        (SELECT
             id
         FROM
             documentation_unit
         WHERE
             document_number
                 = 'YYTestDoc0005'),
        NULL,
        'guiding principle',
        NULL,
        'tenorx'
    ),
    (
        (SELECT
             id
         FROM
             documentation_unit
         WHERE
             document_number
                 = 'YYTestDoc0006'),
        NULL,
        NULL,
        NULL,
        'tenorx'
    ),
    (
        (SELECT
             id
         FROM
             documentation_unit
         WHERE
             document_number
                 = 'YYTestDoc0007'),
        NULL,
        NULL,
        NULL,
        'tenorx'
    ),
    (
        (SELECT
             id
         FROM
             documentation_unit
         WHERE
             document_number
                 = 'YYTestDoc0008'),
        NULL,
        'guiding principle',
        NULL,
        'tenorx'
    ),
    (
        (SELECT
             id
         FROM
             documentation_unit
         WHERE
             document_number
                 = 'YYTestDoc0009'),
        NULL,
        'guiding principle',
        NULL,
        'tenorx'
    ),
    (
        (SELECT
             id
         FROM
             documentation_unit
         WHERE
             document_number
                 = 'YYTestDoc0010'),
        NULL,
        'guiding principle',
        NULL,
        'tenorx'
    ),
    (
        (SELECT
             id
         FROM
             documentation_unit
         WHERE
             document_number
                 = 'YYTestDoc0011'),
        NULL,
        'guiding principle',
        NULL,
        'tenorx'
    ),
    (
        (SELECT
             id
         FROM
             documentation_unit
         WHERE
             document_number
                 = 'YYTestDoc0012'),
        NULL,
        'guiding principle',
        NULL,
        'tenorx'
    ),
    (
        (SELECT
             id
         FROM
             documentation_unit
         WHERE
             document_number
                 = 'YYTestDoc0013'),
        NULL,
        'guiding principle',
        NULL,
        'tenorx <img src="/api/v1/caselaw/documentunits/YYTestDoc0013/image/bild1.png" alt="Bild 1">'
    ),
    (
        (SELECT
             id
         FROM
             documentation_unit
         WHERE
             document_number
                 = 'YYTestDoc0014'),
        NULL,
        'guiding principle',
        NULL,
        'tenorx'
    ),
    (
        (SELECT
             id
         FROM
             documentation_unit
         WHERE
             document_number
                 = 'YYTestDoc0015'),
        NULL,
        'guiding principle',
        NULL,
        'tenorx'
    ),
    (
        (SELECT
             id
         FROM
             documentation_unit
         WHERE
             document_number
                 = 'YYTestDoc0016'),
        (
            SELECT
                id
            FROM
                documentation_office
            WHERE
                abbreviation = 'DS'
        ),
        NULL,
        NULL,
        NULL
    ),
    (
        (SELECT
             id
         FROM
             documentation_unit
         WHERE
             document_number
                 = 'YYTestDoc0019'),
        NULL,
        'guiding principle',
        NULL,
        'tenorx'
    ),
    (
        (SELECT
             id
         FROM
             documentation_unit
         WHERE
             document_number
                 = 'YYTestDoc0020'),
        NULL,
        'guiding principle',
        NULL,
        'tenorx <img src="/api/v1/caselaw/documentunits/YYTestDoc0020/image/bild2.wmf" alt="Bild 2">'
    );


INSERT INTO
    pending_proceeding (
    id,
    resolution_note,
    is_resolved,
    legal_issue,
    admission_of_appeal,
    appellant,
    resolution_date
)
VALUES
    (
        (SELECT
        id
        FROM
        documentation_unit
        WHERE
        document_number
        = 'YYTestDoc0017'),
        '<p>Verfahren ist erledigt durch: Zurücknahme der Klage. Das erstinstanzliche Urteil ist gegenstandslos.</p>',
        true,
        '<p>Gewerbesteuerpflicht des Bäderbetriebs einer Gemeinde als Betrieb gewerblicher Art (Gewinnerzielungsabsicht)? Ist ein Betrieb gewerblicher Art einer juristischen Person des öffentlichen Rechts nur gewerbesteuerpflichtig, wenn er mit der Absicht der Gewinnerzielung betrieben wird?</p>',
        'Zulassung durch BFH',
        'Verwaltung',
        '2025-06-06'
    ),
    (
        (SELECT
             id
         FROM
             documentation_unit
         WHERE
             document_number
                 = 'YYTestDoc0018'),
        NULL,
        false,
        '<p>Unveröffentlichtes anhängiges Verfahren</p>',
        NULL,
        NULL,
        NULL
    );

INSERT INTO
    source (
    id,
    value,
    documentation_unit_id,
    rank,
    source_raw_value
)
VALUES
    (
        gen_random_uuid (),
        null,
        (
            SELECT
                id
            FROM
                documentation_unit
            WHERE
                document_number = 'YYTestDoc0001'
        ),
     1,
     'legacy value'
    );

INSERT INTO
  status (
    id,
    created_at,
    publication_status,
    with_error,
    documentation_unit_id
  )
VALUES
  (
    gen_random_uuid (),
    '2024-03-14 18:38:43.043877 +00:00',
    'PUBLISHED',
    false,
    (
      SELECT
        id
      FROM
        documentation_unit
      WHERE
        document_number = 'YYTestDoc0001'
    )
  ),
  (
    gen_random_uuid (),
    '2024-03-14 18:38:43.043877 +00:00',
    'PUBLISHED',
    true,
    (
      SELECT
        id
      FROM
        documentation_unit
      WHERE
        document_number = 'YYTestDoc0002'
    )
  ),
  (
    gen_random_uuid (),
    '2024-03-14 18:38:43.043877 +00:00',
    'UNPUBLISHED',
    false,
    (
      SELECT
        id
      FROM
        documentation_unit
      WHERE
        document_number = 'YYTestDoc0003'
    )
  ),
  (
    gen_random_uuid (),
    '2024-03-14 18:38:43.043877 +00:00',
    'PUBLISHED',
    false,
    (
      SELECT
        id
      FROM
        documentation_unit
      WHERE
        document_number = 'YYTestDoc0004'
    )
  ),
  (
    gen_random_uuid (),
    '2024-03-14 18:38:43.043877 +00:00',
    'PUBLISHING',
    false,
    (
      SELECT
        id
      FROM
        documentation_unit
      WHERE
        document_number = 'YYTestDoc0005'
    )
  ),
  (
    gen_random_uuid (),
    '2024-03-14 18:38:43.043877 +00:00',
    'UNPUBLISHED',
    false,
    (
      SELECT
        id
      FROM
        documentation_unit
      WHERE
        document_number = 'YYTestDoc0006'
    )
  ),
  (
    gen_random_uuid (),
    '2024-03-14 18:38:43.043877 +00:00',
    'UNPUBLISHED',
    false,
    (
      SELECT
        id
      FROM
        documentation_unit
      WHERE
        document_number = 'YYTestDoc0007'
    )
  ),
  (
    gen_random_uuid (),
    '2024-03-14 18:38:43.043877 +00:00',
    'UNPUBLISHED',
    false,
    (
      SELECT
        id
      FROM
        documentation_unit
      WHERE
        document_number = 'YYTestDoc0008'
    )
  ),
  (
    gen_random_uuid (),
    '2024-03-14 18:38:43.043877 +00:00',
    'UNPUBLISHED',
    false,
    (
      SELECT
        id
      FROM
        documentation_unit
      WHERE
        document_number = 'YYTestDoc0009'
    )
  ),
  (
    gen_random_uuid (),
    '2024-03-14 18:38:43.043877 +00:00',
    'PUBLISHING',
    false,
    (
      SELECT
        id
      FROM
        documentation_unit
      WHERE
        document_number = 'YYTestDoc0010'
    )
  ),
  (
    gen_random_uuid (),
    '2024-03-14 18:38:43.043877 +00:00',
    'PUBLISHED',
    true,
    (
      SELECT
        id
      FROM
        documentation_unit
      WHERE
        document_number = 'YYTestDoc0011'
    )
  ),
  (
    gen_random_uuid (),
    '2024-03-14 18:38:43.043877 +00:00',
    'PUBLISHED',
    false,
    (
      SELECT
        id
      FROM
        documentation_unit
      WHERE
        document_number = 'YYTestDoc0012'
    )
  ),
  (
    gen_random_uuid (),
    '2024-03-14 18:38:43.043877 +00:00',
    'PUBLISHED',
    false,
    (
      SELECT
        id
      FROM
        documentation_unit
      WHERE
        document_number = 'YYTestDoc0013'
    )
  ),
  (
    gen_random_uuid (),
    '2024-03-14 18:38:43.043877 +00:00',
    'PUBLISHED',
    true,
    (
      SELECT
        id
      FROM
        documentation_unit
      WHERE
        document_number = 'YYTestDoc0014'
    )
  ),
  (
    gen_random_uuid (),
    '2024-03-13 18:38:43.043877 +00:00',
    'PUBLISHED',
    false,
    (
      SELECT
        id
      FROM
        documentation_unit
      WHERE
        document_number = 'YYTestDoc0015'
    )
  ),
  (
    gen_random_uuid (),
    '2024-03-14 18:38:43.043877 +00:00',
    'UNPUBLISHED',
    false,
    (
      SELECT
          id
      FROM
          documentation_unit
      WHERE
          document_number = 'YYTestDoc0015'
    )
  ),
  (
    gen_random_uuid (),
    '2024-03-12 18:38:43.043877 +00:00',
    'PUBLISHED',
    false,
    (
      SELECT
          id
      FROM
          documentation_unit
      WHERE
          document_number = 'YYTestDoc0015'
    )
    ),(
      gen_random_uuid (),
      '2024-03-14 18:38:43.043877 +00:00',
      'EXTERNAL_HANDOVER_PENDING',
      false,
      (
          SELECT
              id
          FROM
              documentation_unit
          WHERE
              document_number = 'YYTestDoc0016'
      )
    ),
    (
      gen_random_uuid (),
      '2025-02-25 18:38:43.043877 +00:00',
      'PUBLISHED',
      false,
      (
          SELECT
              id
          FROM
              documentation_unit
          WHERE
              document_number = 'YYTestDoc0017'
      )
    ),
  (
      gen_random_uuid (),
      '2025-02-26 18:38:43.043877 +00:00',
      'UNPUBLISHED',
      false,
      (
          SELECT
              id
          FROM
              documentation_unit
          WHERE
              document_number = 'YYTestDoc0018'
      )
  ),
  (
      gen_random_uuid (),
      '2024-03-14 18:38:43.043877 +00:00',
      'PUBLISHED',
      false,
      (
          SELECT
              id
          FROM
              documentation_unit
          WHERE
              document_number = 'YYTestDoc0019'
      )
  ),
  (
      gen_random_uuid (),
      '2024-03-14 18:38:43.043877 +00:00',
      'UNPUBLISHED',
      false,
      (
          SELECT
              id
          FROM
              documentation_unit
          WHERE
              document_number = 'YYTestDoc0020'
      )
  )
    ;

UPDATE documentation_unit SET current_status_id =
(SELECT id FROM status WHERE documentation_unit_id = (SELECT id FROM documentation_unit WHERE document_number = 'YYTestDoc0001'))
WHERE document_number = 'YYTestDoc0001';

UPDATE documentation_unit SET current_status_id =
    (SELECT id FROM status WHERE documentation_unit_id = (SELECT id FROM documentation_unit WHERE document_number = 'YYTestDoc0002'))
WHERE document_number = 'YYTestDoc0002';


UPDATE documentation_unit SET current_status_id =
    (SELECT id FROM status WHERE documentation_unit_id = (SELECT id FROM documentation_unit WHERE document_number = 'YYTestDoc0003'))
WHERE document_number = 'YYTestDoc0003';

UPDATE documentation_unit SET current_status_id =
    (SELECT id FROM status WHERE documentation_unit_id = (SELECT id FROM documentation_unit WHERE document_number = 'YYTestDoc0004'))
WHERE document_number = 'YYTestDoc0004';

UPDATE documentation_unit SET current_status_id =
    (SELECT id FROM status WHERE documentation_unit_id = (SELECT id FROM documentation_unit WHERE document_number = 'YYTestDoc0005'))
WHERE document_number = 'YYTestDoc0005';

UPDATE documentation_unit SET current_status_id =
    (SELECT id FROM status WHERE documentation_unit_id = (SELECT id FROM documentation_unit WHERE document_number = 'YYTestDoc0006'))
WHERE document_number = 'YYTestDoc0006';

UPDATE documentation_unit SET current_status_id =
    (SELECT id FROM status WHERE documentation_unit_id = (SELECT id FROM documentation_unit WHERE document_number = 'YYTestDoc0007'))
WHERE document_number = 'YYTestDoc0007';

UPDATE documentation_unit SET current_status_id =
    (SELECT id FROM status WHERE documentation_unit_id = (SELECT id FROM documentation_unit WHERE document_number = 'YYTestDoc0008'))
WHERE document_number = 'YYTestDoc0008';

UPDATE documentation_unit SET current_status_id =
    (SELECT id FROM status WHERE documentation_unit_id = (SELECT id FROM documentation_unit WHERE document_number = 'YYTestDoc0009'))
WHERE document_number = 'YYTestDoc0009';

UPDATE documentation_unit SET current_status_id =
    (SELECT id FROM status WHERE documentation_unit_id = (SELECT id FROM documentation_unit WHERE document_number = 'YYTestDoc0010'))
WHERE document_number = 'YYTestDoc0010';

UPDATE documentation_unit SET current_status_id =
    (SELECT id FROM status WHERE documentation_unit_id = (SELECT id FROM documentation_unit WHERE document_number = 'YYTestDoc0011'))
WHERE document_number = 'YYTestDoc0011';

UPDATE documentation_unit SET current_status_id =
    (SELECT id FROM status WHERE documentation_unit_id = (SELECT id FROM documentation_unit WHERE document_number = 'YYTestDoc0012'))
WHERE document_number = 'YYTestDoc0012';

UPDATE documentation_unit SET current_status_id =
    (SELECT id FROM status WHERE documentation_unit_id = (SELECT id FROM documentation_unit WHERE document_number = 'YYTestDoc0013'))
WHERE document_number = 'YYTestDoc0013';

UPDATE documentation_unit SET current_status_id =
    (SELECT id FROM status WHERE documentation_unit_id = (SELECT id FROM documentation_unit WHERE document_number = 'YYTestDoc0014'))
WHERE document_number = 'YYTestDoc0014';

UPDATE documentation_unit SET current_status_id =
    (SELECT id FROM status WHERE documentation_unit_id = (SELECT id FROM documentation_unit WHERE document_number = 'YYTestDoc0015') ORDER BY created_at DESC LIMIT 1)
WHERE document_number = 'YYTestDoc0015';

UPDATE documentation_unit SET current_status_id =
    (SELECT id FROM status WHERE documentation_unit_id = (SELECT id FROM documentation_unit WHERE document_number = 'YYTestDoc0016'))
WHERE document_number = 'YYTestDoc0016';

UPDATE documentation_unit SET current_status_id =
    (SELECT id FROM status WHERE documentation_unit_id = (SELECT id FROM documentation_unit WHERE document_number = 'YYTestDoc0017'))
WHERE document_number = 'YYTestDoc0017';

UPDATE documentation_unit SET current_status_id =
    (SELECT id FROM status WHERE documentation_unit_id = (SELECT id FROM documentation_unit WHERE document_number = 'YYTestDoc0018'))
WHERE document_number = 'YYTestDoc0018';

UPDATE documentation_unit SET current_status_id =
    (SELECT id FROM status WHERE documentation_unit_id = (SELECT id FROM documentation_unit WHERE document_number = 'YYTestDoc0019'))
WHERE document_number = 'YYTestDoc0019';

UPDATE documentation_unit SET current_status_id =
    (SELECT id FROM status WHERE documentation_unit_id = (SELECT id FROM documentation_unit WHERE document_number = 'YYTestDoc0020'))
WHERE document_number = 'YYTestDoc0020';

UPDATE
  decision
SET
  note = 'dies ist eine test notiz'
WHERE
  decision.id = (SELECT id FROM documentation_unit WHERE document_number = 'YYTestDoc0015');

UPDATE
    decision
SET
    legal_effect = 'JA'
WHERE
    decision.id = (SELECT id FROM documentation_unit WHERE document_number = 'YYTestDoc0013');

UPDATE
    decision
SET
    legal_effect = 'JA'
WHERE
    decision.id = (SELECT id FROM documentation_unit WHERE document_number = 'YYTestDoc0020');

INSERT INTO
  file_number (id, value, documentation_unit_id, rank)
VALUES
  (
    gen_random_uuid (),
    'fileNumber1',
    (
      SELECT
        id
      FROM
        documentation_unit
      WHERE
        document_number = 'YYTestDoc0001'
    ),
    1
  ),
  (
    gen_random_uuid (),
    'fileNumber2',
    (
      SELECT
        id
      FROM
        documentation_unit
      WHERE
        document_number = 'YYTestDoc0001'
    ),
    2
  ),
  (
    gen_random_uuid (),
    'fileNumber3',
    (
      SELECT
        id
      FROM
        documentation_unit
      WHERE
        document_number = 'YYTestDoc0004'
    ),
    1
  ),
  (
    gen_random_uuid (),
    'fileNumber4',
    (
      SELECT
        id
      FROM
        documentation_unit
      WHERE
        document_number = 'YYTestDoc0012'
    ),
    1
  ),
  (
    gen_random_uuid (),
    'fileNumber5',
    (
      SELECT
        id
      FROM
        documentation_unit
      WHERE
        document_number = 'YYTestDoc0013'
    ),
    1
  ),
    (
        gen_random_uuid (),
        'I R 20000/34',
        (
            SELECT
                id
            FROM
                documentation_unit
            WHERE
                document_number = 'YYTestDoc0017'
        ),
        1
    ),
  (
      gen_random_uuid (),
      'fileNumber0',
      (
          SELECT
              id
          FROM
              documentation_unit
          WHERE
              document_number = 'YYTestDoc0020'
      ),
      2
  );

INSERT INTO
    attachment_inline (id, upload_timestamp, format, filename, documentation_unit_id, content)
VALUES
    (
        gen_random_uuid (),
        now(),
     'png',
     'bild1.png',
        (
            SELECT
                id
            FROM
                documentation_unit
            WHERE
                document_number = 'YYTestDoc0013'
        ),
        '89504E470D0A1A0A0000000D494844520000000A0000000A08060000008D32CFBD000000154944415478DA6364F8CF50CF4004601C55485F8500084A0EF7A4B0E8E60000000049454E44AE426082'
    ),
    (
        gen_random_uuid (),
        now(),
        'wmf',
        'bild2.wmf',
        (
            SELECT
                id
            FROM
                documentation_unit
            WHERE
                document_number = 'YYTestDoc0020'
        ),
        '89504E470D0A1A0A0000000D494844520000000A0000000A08060000008D32CFBD000000154944415478DA636460F85FCF4004601C55485F8500FE450EF71F4585A00000000049454E44AE426082'
    );
;

INSERT INTO
    keyword (id, value)
VALUES
    (
        gen_random_uuid (),
        'keyword1'
    ),
    (
        gen_random_uuid (),
        'keyword2'
    ),
    (
        gen_random_uuid (),
        'keyword3'
    )
ON CONFLICT DO NOTHING;


-- TEXT CHECK IGNORED WORDS
INSERT INTO ignored_text_check_word (id,
                                                           documentation_unit_id,
                                                           word,
                                                           juris_id,
                                                           created_at)
VALUES (gen_random_uuid(),
        NULL,
        'Testgnorierteswort',
        -1001,
        NOW()
       )
ON CONFLICT DO NOTHING;
