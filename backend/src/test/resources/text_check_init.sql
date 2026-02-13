INSERT INTO
  ignored_text_check_word (
    id,
    documentation_unit_id,
    word,
    juris_id,
    created_at
  )
VALUES
  (
      gen_random_uuid(),
    null,
    'uvw',
    400,
      current_timestamp
  ),
  (
      gen_random_uuid(),
    null,
    'xyz',
    null,
      current_timestamp
  );
