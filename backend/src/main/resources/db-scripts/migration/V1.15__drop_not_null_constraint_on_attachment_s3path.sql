-- Drop non-null constraint on attachment s3 path to allow for attachments outside of s3
ALTER TABLE attachment ALTER COLUMN s3_object_path DROP NOT NULL;
