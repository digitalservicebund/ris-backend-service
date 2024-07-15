ALTER TABLE
  xml_publication
RENAME TO
  handover_mail;

ALTER TABLE
  publication_report
RENAME TO
  handover_report;

ALTER TABLE
  handover_mail
RENAME COLUMN
  publish_date TO sent_date;
