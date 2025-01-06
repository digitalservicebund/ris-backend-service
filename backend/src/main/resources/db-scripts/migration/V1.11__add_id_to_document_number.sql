-- Adding unique id to document number table
ALTER TABLE document_number
    ADD COLUMN id UUID NOT NULL DEFAULT gen_random_uuid();

-- Drop the existing primary key constraint directing to documentation_office_abbreviation
ALTER TABLE document_number
DROP CONSTRAINT document_number_pkey;

-- Add a new primary key constraint on id column
ALTER TABLE document_number
    ADD CONSTRAINT document_number_pkey PRIMARY KEY (id);


