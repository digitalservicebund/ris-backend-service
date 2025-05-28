INSERT INTO incremental_migration.attachment (
    id,
    upload_timestamp,
    format,
    filename,
    s3_object_path,
    documentation_unit_id,
    content
)
SELECT
    pa.id,
    pa.upload_timestamp,
    pa.format,
    pa.filename,
    pa.s3_object_path,
    pa.documentation_unit_id,
    NULL AS content -- Explicitly set content to NULL
FROM
    attachment AS pa
        INNER JOIN
    incremental_migration.documentation_unit AS du ON pa.documentation_unit_id = du.id;
