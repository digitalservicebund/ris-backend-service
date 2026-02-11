DELETE FROM court_region WHERE court_id IN (SELECT id FROM court);
DELETE FROM court;
