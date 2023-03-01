-- required for to_timestamp to correctly parse the date strings as UTC-timezoned
SET
  timezone = 'UTC';

-- doc_unit.entscheidungsdatum (string) -> doc_unit.decision_date (timestamp)
ALTER TABLE
  doc_unit
ADD COLUMN IF NOT EXISTS
  decision_date TIMESTAMP WITH TIME ZONE;

UPDATE
  doc_unit
SET
  decision_date = CASE
    WHEN entscheidungsdatum ~ '^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z$' THEN to_timestamp(entscheidungsdatum, 'YYYY-MM-DD"T"HH24:MI:SS"Z"')
    WHEN entscheidungsdatum ~ '^\d{2}.\d{2}.\d{4}$' THEN to_timestamp(entscheidungsdatum, 'DD.MM.YYYY')
  END;

-- previous_decision.decision_date (string) -> previous_decision.decision_date_timestamp (timestamp)
ALTER TABLE
  previous_decision
ADD COLUMN IF NOT EXISTS
  decision_date_timestamp TIMESTAMP WITH TIME ZONE;

UPDATE
  previous_decision
SET
  decision_date_timestamp = CASE
    WHEN decision_date ~ '^\d{2}.\d{2}.\d{4}$' THEN to_timestamp(decision_date, 'DD.MM.YYYY')
  END;
