CREATE INDEX
  IF NOT EXISTS norm_abbreviation_official_letter_gin_idx ON incremental_migration.norm_abbreviation USING gin (upper(official_letter_abbreviation) gin_trgm_ops);

CREATE INDEX
  IF NOT EXISTS norm_abbreviation_gin_idx ON incremental_migration.norm_abbreviation USING gin (upper(abbreviation) gin_trgm_ops);

CREATE INDEX
  IF NOT EXISTS norm_abbreviation_fs_idx ON incremental_migration.norm_abbreviation USING GIN (
    to_tsvector(
      'german',
      coalesce(abbreviation, '') || ' ' || coalesce(official_long_title, '') || ' ' || coalesce(official_short_title, '') || ' ' || coalesce(official_letter_abbreviation, '')
    )
  );

create materialized view if not exists
  norm_abbreviation_search_migration as
select
  na.*,
  r.code,
  r.id as region_id,
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
from
  incremental_migration.norm_abbreviation na
  left join incremental_migration.norm_abbreviation_region nar on na.id = nar.norm_abbreviation_id
  left join incremental_migration.region r on nar.region_id = r.id;

CREATE INDEX
  IF NOT EXISTS norm_abbreviation_search_migration_idx ON norm_abbreviation_search_migration USING GIN (weighted_vector);
