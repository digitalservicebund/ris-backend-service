INSERT INTO incremental_migration.deleted_documents (
    document_number,
    year,
    abbreviation
)
SELEct
    dd.document_number,
    dd.year,
    dd.abbreviation
FROM
    deleted_documents AS dd;

INSERT INTO incremental_migration.document_number (
    documentation_office_abbreviation,
    last_number,
    id,
    year
)
SELEct
    dn.documentation_office_abbreviation,
    dn.last_number,
    dn.id,
    dn.year
FROM
    document_number AS dn;

INSERT INTO incremental_migration.handover_mail (
    mail_subject,
    status_code,
    status_messages,
    sent_date,
    receiver_address,
    id,
    entity_id,
    issuer_address,
    attached_images
)
SELECT
    hm.mail_subject,
    hm.status_code,
    hm.status_messages,
    hm.sent_date,
    hm.receiver_address,
    hm.id,
    hm.entity_id,
    hm.issuer_address,
    hm.attached_images
FROM handover_mail AS hm;

INSERT INTO incremental_migration.handover_mail_attachments (
    id,
    handover_mail_id,
    xml,
    file_name
)
SELECT
    hma.id,
    hma.handover_mail_id,
    hma.xml,
    hma.file_name
FROM handover_mail_attachments AS hma;

INSERT INTO incremental_migration.handover_report (
    id,
    content,
    received_date,
    entity_id
)
SELECT
    hr.id,
    hr.content,
    hr.received_date,
    hr.entity_id
FROM handover_report AS hr;

INSERT INTO incremental_migration.shedlock (
    name,
    lock_until,
    locked_at,
    locked_by
)
SELECT
    s.name,
    s.lock_until,
    s.locked_at,
    s.locked_by
FROM shedlock AS s;


