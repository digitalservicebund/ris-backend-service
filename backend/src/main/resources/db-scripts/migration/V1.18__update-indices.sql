-- Needed according to query stats.
CREATE
    INDEX IF NOT EXISTS handover_mail_entity_id_idx ON
    handover_mail(entity_id);

-- Index has not been used once on Staging or Production in the last 90 days.
DROP INDEX IF EXISTS idx_documentation_office_abbreviation_year;
