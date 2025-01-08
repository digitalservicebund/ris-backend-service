-- Adding year to document number count
ALTER TABLE IF EXISTS document_number
    ADD year INTEGER;

-- Adding previous year to document number count
UPDATE document_number SET year = 2024 WHERE year IS NULL;

-- Setting required year for upcoming entries
ALTER TABLE document_number ALTER COLUMN year SET NOT NULL;

-- Create a combined index on 'documentation_office_abbreviation' and 'year'
CREATE INDEX idx_documentation_office_abbreviation_year
    ON document_number (documentation_office_abbreviation, year);
