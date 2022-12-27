INSERT INTO
  document_number_counter (nextnumber, currentyear)
SELECT
  1,
  date_part('year', CURRENT_DATE)
WHERE
  NOT EXISTS (
    SELECT
      id
    FROM
      document_number_counter
  );
