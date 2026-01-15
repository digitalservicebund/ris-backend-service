ALTER TABLE incremental_migration.attachment ADD COLUMN attachment_type VARCHAR(30);

UPDATE incremental_migration.attachment
SET attachment_type =
    CASE
        WHEN upper(format) IN ('DOCX', 'FMX')
            THEN 'ORIGINATING'
            ELSE 'OTHER'
    END;

ALTER TABLE incremental_migration.attachment ALTER COLUMN attachment_type SET DEFAULT 'OTHER';

ALTER TABLE incremental_migration.attachment ALTER COLUMN attachment_type SET NOT NULL;

ALTER TABLE incremental_migration.attachment ADD CONSTRAINT attachment_type_check CHECK (attachment_type IN ('ORIGINATING', 'OTHER'));
