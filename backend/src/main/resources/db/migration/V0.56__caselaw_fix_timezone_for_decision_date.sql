UPDATE 
  doc_unit 
SET 
  decision_date =  date_trunc(
    'day', 
    decision_date AT TIME ZONE 'Europe/Berlin'
  ) AT TIME ZONE 'Europe/Berlin';