--- Test Dokstellen
INSERT INTO
  incremental_migration.documentation_office (id, abbreviation)
VALUES
  ('41e62dbc-e5b6-414f-91e2-0cfe559447d1', 'BGH'),
  ('0ccb67d9-73ca-4e26-aa49-3ec382cb7667', 'BAG'),
  ('d67795b6-7ce7-4976-8032-daad608dd04e', 'BFH'),
  ('ae04fd78-a78c-470e-b017-622fbbc13dbd', 'BPatG'),
  ('f1177fc4-746f-4fc0-9529-56d2fce36306', 'BSG'),
  ('3cf7625f-c81c-4c30-9649-412e7e978f7f', 'BVerfG'),
  ('33e15b43-1ab1-4c09-a22a-32a3157c2c9d', 'BVerwG'),
  ('f3ddb0e4-fe75-4fee-acf0-79efc24a9bf7', 'BZSt'),
  ('d896f13c-652f-4e88-b279-695403bf9ef7', 'OVGNW'),
  ('f13c2fdb-5323-49aa-bc6d-09fa68c3acb9', 'CC-RIS'),
  ('ba90a851-3c54-4858-b4fa-7742ffbe8f05', 'DS') ON CONFLICT
DO
  NOTHING;
