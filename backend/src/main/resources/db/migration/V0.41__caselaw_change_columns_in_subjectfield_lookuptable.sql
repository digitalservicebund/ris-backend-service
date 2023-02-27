ALTER TABLE
  lookuptable_subject_field
DROP COLUMN
  depth_in_tree,
DROP COLUMN
  is_leaf_in_tree,
ADD COLUMN IF NOT EXISTS
  children_count BIGINT NOT NULL DEFAULT 0;
