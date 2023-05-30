CREATE INDEX
  norm_abbreviation_fs_idx ON norm_abbreviation USING GIN (
    to_tsvector(
      'german',
      coalesce(abbreviation, '') || ' ' || coalesce(official_long_title, '') || ' ' || coalesce(official_short_title, '') || ' ' || coalesce(official_letter_abbreviation, '')
    )
  );
