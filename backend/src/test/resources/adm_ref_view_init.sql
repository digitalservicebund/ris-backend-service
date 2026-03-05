CREATE SCHEMA IF NOT EXISTS references_schema;

CREATE TABLE IF NOT EXISTS references_schema.ref_view_adm (
                                                              id UUID PRIMARY KEY,
                                                              document_number VARCHAR(255),
                                                              juris_abbreviation VARCHAR(255),
                                                              published_at TIMESTAMP
);

DELETE FROM references_schema.ref_view_adm WHERE id = 'c5c6acf4-11d0-4586-9357-0913fa40d939';
INSERT INTO references_schema.ref_view_adm (id, document_number, juris_abbreviation, published_at)
VALUES ('c5c6acf4-11d0-4586-9357-0913fa40d939', 'KSNR004051608', 'VV DEU BMF 2004-11-03 IV B 2-S 2176-13/04', CURRENT_TIMESTAMP) ON CONFLICT DO NOTHING;

CREATE TABLE IF NOT EXISTS references_schema.ref_view_active_reference_adm_caselaw (
                                                              id UUID PRIMARY KEY,
                                                              source_documentation_unit_id UUID,
                                                              target_documentation_unit_id UUID,
                                                              citation_type VARCHAR(255)
);

DELETE FROM references_schema.ref_view_active_reference_adm_caselaw WHERE id = 'c5ccb919-d544-4897-8eaf-234383ca6c96';
INSERT INTO references_schema.ref_view_active_reference_adm_caselaw (id, source_documentation_unit_id, target_documentation_unit_id, citation_type)
VALUES ('c5ccb919-d544-4897-8eaf-234383ca6c96', 'c5c6acf4-11d0-4586-9357-0913fa40d939', 'adb8408b-5a77-48f9-9ed0-b8dee4f2db02', 'Vgl') ON CONFLICT DO NOTHING;



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
