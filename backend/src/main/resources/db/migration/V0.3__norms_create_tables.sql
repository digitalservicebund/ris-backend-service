CREATE TABLE
  norms (guid uuid PRIMARY KEY, long_title VARCHAR(255));

CREATE TABLE
  articles (
    guid uuid PRIMARY KEY,
    title VARCHAR(255),
    marker VARCHAR(20),
    norm_id uuid,
    FOREIGN KEY (norm_id) REFERENCES norms (guid)
  );

CREATE TABLE
  paragraphs (
    guid uuid PRIMARY KEY,
    text TEXT,
    marker VARCHAR(20),
    article_id uuid,
    FOREIGN KEY (article_id) REFERENCES articles (guid)
  );
