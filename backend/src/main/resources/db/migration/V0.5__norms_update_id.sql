ALTER TABLE
  paragraphs
DROP
  CONSTRAINT paragraphs_article_id_fkey;

ALTER TABLE
  paragraphs
DROP
  CONSTRAINT paragraphs_pkey;

ALTER TABLE
  articles
DROP
  CONSTRAINT articles_norm_id_fkey;

ALTER TABLE
  articles
DROP
  CONSTRAINT articles_pkey;

ALTER TABLE
  norms
DROP
  CONSTRAINT norms_pkey;

ALTER TABLE
  paragraphs
ADD COLUMN
  id SERIAL PRIMARY KEY;

ALTER TABLE
  paragraphs
DROP COLUMN
  article_id;

ALTER TABLE
  paragraphs
ADD COLUMN
  article_id INT;

ALTER TABLE
  articles
ADD COLUMN
  id SERIAL PRIMARY KEY;

ALTER TABLE
  articles
DROP COLUMN
  norm_id;

ALTER TABLE
  articles
ADD COLUMN
  norm_id INT;

ALTER TABLE
  norms
ADD COLUMN
  id SERIAL PRIMARY KEY;

ALTER TABLE
  articles
ADD
  CONSTRAINT articles_norm_id_fkey FOREIGN KEY (norm_id) REFERENCES norms (id);

ALTER TABLE
  paragraphs
ADD
  CONSTRAINT paragraphs_article_id_fkey FOREIGN KEY (article_id) REFERENCES norms (id);
