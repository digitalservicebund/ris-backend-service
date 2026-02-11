INSERT INTO
  documentation_office_user_group (id, user_group_path_name, documentation_office_id, is_internal)
VALUES
  (
    '2b733549-d2cc-40f0-b7f3-9bfa9f3c1b89',
    'DS/Extern',
    '6be0bb1a-c196-484a-addf-822f2ab557f7',
    false
  ),
  (
    '3b733549-d2cc-40f0-b7f3-9bfa9f3c1b89',
    'BGH/Extern',
    'bd350c93-7ff0-4409-9c62-371e3d0c749e',
    false
  ) ON CONFLICT DO NOTHING;
