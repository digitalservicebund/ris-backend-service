ALTER TABLE
  articles
DROP
  CONSTRAINT articles_norm_id_fkey;

ALTER TABLE
  paragraphs
DROP
  CONSTRAINT paragraphs_article_id_fkey;
