INSERT INTO
  incremental_migration.user (id, first_name, last_name, documentation_office_id, external_id)
VALUES
  (
    '9b733549-d2cc-40f0-b7f3-9bfa9f3c1b79',
    'Kathryn',
   'Janeway',
    'bd350c93-7ff0-4409-9c62-371e3d0c749e',
   '1be0bb1a-c196-484a-addf-822f2ab557f7'
  ) ON CONFLICT DO NOTHING;
