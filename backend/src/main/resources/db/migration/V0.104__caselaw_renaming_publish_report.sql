ALTER TABLE IF EXISTS
  publish_report_attachment
RENAME TO
  publication_report;

ALTER TABLE IF EXISTS
  xml_mail
RENAME TO
  xml_publication;
