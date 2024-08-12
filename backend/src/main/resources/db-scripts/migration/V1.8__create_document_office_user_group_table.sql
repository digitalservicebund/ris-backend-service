CREATE TABLE IF NOT EXISTS
  documentation_office_user_group (
    id UUID NOT NULL DEFAULT(uuid_generate_v4()),
    user_group_path_name VARCHAR(255) UNIQUE NOT NULL,
    documentation_office_id UUID NOT NULL,
    is_internal BOOL NOT NULL,
    CONSTRAINT fk_documentation_office_id FOREIGN KEY(documentation_office_id) REFERENCES incremental_migration.documentation_office(id),
    PRIMARY KEY (id)
  );

INSERT INTO documentation_office_user_group (user_group_path_name, is_internal, documentation_office_id)
VALUES
    ('/caselaw/BGH', True, (SELECT id FROM incremental_migration.documentation_office WHERE abbreviation = 'BGH')),
    ('/caselaw/BVerfG', True, (SELECT id FROM incremental_migration.documentation_office WHERE abbreviation = 'BVerfG')),
    ('/caselaw/BAG', True, (SELECT id FROM incremental_migration.documentation_office WHERE abbreviation = 'BAG')),
    ('/caselaw/BFH', True, (SELECT id FROM incremental_migration.documentation_office WHERE abbreviation = 'BFH')),
    ('/caselaw/BPatG', True, (SELECT id FROM incremental_migration.documentation_office WHERE abbreviation = 'BPatG')),
    ('/caselaw/BSG', True, (SELECT id FROM incremental_migration.documentation_office WHERE abbreviation = 'BSG')),
    ('/caselaw/BVerwG', True, (SELECT id FROM incremental_migration.documentation_office WHERE abbreviation = 'BVerwG')),
    ('/caselaw/OVG_NRW', True, (SELECT id FROM incremental_migration.documentation_office WHERE abbreviation = 'OVGNW')),
    ('/caselaw/BZSt', True, (SELECT id FROM incremental_migration.documentation_office WHERE abbreviation = 'BZSt')),
    ('/DS', True, (SELECT id FROM incremental_migration.documentation_office WHERE abbreviation = 'DS')),
    ('/DS/Intern', True, (SELECT id FROM incremental_migration.documentation_office WHERE abbreviation = 'DS')),
    ('/DS/Extern', False, (SELECT id FROM incremental_migration.documentation_office WHERE abbreviation = 'DS')),
    ('/CC-RIS', True, (SELECT id FROM incremental_migration.documentation_office WHERE abbreviation = 'CC-RIS'));
