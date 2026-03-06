CREATE SCHEMA IF NOT EXISTS references_schema;

CREATE TABLE IF NOT EXISTS references_schema.ref_view_sli (
                                                              id UUID PRIMARY KEY,
                                                              document_number VARCHAR(255),
                                                              author VARCHAR(255),
                                                              book_title VARCHAR(255),
                                                              year_of_publication VARCHAR(255),
                                                              published_at TIMESTAMP
);

DELETE FROM references_schema.ref_view_sli WHERE id = 'c4c08674-c862-415c-afc6-ddc0dc185702';
INSERT INTO references_schema.ref_view_sli (id, document_number, author, book_title, year_of_publication, published_at)
VALUES ('c4c08674-c862-415c-afc6-ddc0dc185702', 'KSNR150060010', 'Beitel, Willibald', 'Rechtsprechung, Erlasse und Gesetzesänderungen (12)', '2005', CURRENT_TIMESTAMP)  ON CONFLICT DO NOTHING;

CREATE TABLE IF NOT EXISTS references_schema.ref_view_active_reference_sli_caselaw (
                                                              id UUID PRIMARY KEY,
                                                              source_documentation_unit_id UUID,
                                                              target_documentation_unit_id UUID
);

DELETE FROM references_schema.ref_view_sli WHERE id = 'b70221da-81a7-4eb9-8cc9-dd66cd11aa37';
INSERT INTO references_schema.ref_view_active_reference_sli_caselaw (id, source_documentation_unit_id, target_documentation_unit_id)
VALUES ('b70221da-81a7-4eb9-8cc9-dd66cd11aa37', 'c4c08674-c862-415c-afc6-ddc0dc185702', 'adb8408b-5a77-48f9-9ed0-b8dee4f2db02') ON CONFLICT DO NOTHING;



insert into
    incremental_migration.documentation_unit (id, document_number, documentation_office_id)
values
    (
        'adb8408b-5a77-48f9-9ed0-b8dee4f2db02',
        'YYTestDoc2000',
        '6be0bb1a-c196-484a-addf-822f2ab557f7'
    );

insert into
    incremental_migration.decision (id)
values
    (
        'adb8408b-5a77-48f9-9ed0-b8dee4f2db02'
    )
