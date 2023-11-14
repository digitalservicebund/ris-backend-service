CREATE materialized VIEW IF NOT EXISTS
  norm_abbreviation_search_migration AS
SELECT
  na.*,
  r.code,
  r.id AS region_id,
  setweight(to_tsvector('german', na.abbreviation), 'A') || setweight(
    to_tsvector(
      'german',
      coalesce(na.official_letter_abbreviation, '')
    ),
    'B'
  ) || setweight(
    to_tsvector('german', coalesce(na.official_short_title, '')),
    'B'
  ) || setweight(
    to_tsvector('german', coalesce(na.official_long_title, '')),
    'B'
  ) || setweight(to_tsvector('german', coalesce(r.code, '')), 'B') weighted_vector
FROM
  incremental_migration.norm_abbreviation na
  LEFT JOIN incremental_migration.norm_abbreviation_region nar ON na.id = nar.norm_abbreviation_id
  LEFT JOIN incremental_migration.region r ON nar.region_id = r.id;

CREATE INDEX
  IF NOT EXISTS norm_abbreviation_search_migration_idx ON norm_abbreviation_search_migration USING GIN (weighted_vector);
