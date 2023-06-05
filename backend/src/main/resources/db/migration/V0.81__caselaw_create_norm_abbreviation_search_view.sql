create materialized view
  norm_abbreviation_search as
select
  na.*,
  r.label,
  r.code,
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
  ) || setweight(to_tsvector('german', coalesce(r.label, '')), 'B') || setweight(to_tsvector('german', coalesce(r.code, '')), 'B') weighted_vector
from
  norm_abbreviation na
  left join norm_abbreviation_region nar on na.id = nar.norm_abbreviation_id
  left join region r on nar.region_id = r.id;

CREATE INDEX
  norm_abbreviation_search_idx ON norm_abbreviation_search USING GIN (weighted_vector);
