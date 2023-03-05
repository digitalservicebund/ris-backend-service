CREATE INDEX
  IF NOT EXISTS file_number_document_unit_id_idx ON file_number (document_unit_id);

CREATE INDEX
  IF NOT EXISTS deviating_decision_date_document_unit_id_idx ON deviating_decision_date (document_unit_id);

CREATE INDEX
  IF NOT EXISTS deviating_ecli_document_unit_id_idx ON deviating_ecli (document_unit_id);

CREATE INDEX
  IF NOT EXISTS incorrect_court_document_unit_id_idx ON incorrect_court (document_unit_id);

CREATE INDEX
  IF NOT EXISTS previous_decision_document_unit_id_idx ON previous_decision (document_unit_id);

CREATE INDEX
  IF NOT EXISTS previous_decision_document_number_idx ON previous_decision (document_number);

CREATE INDEX
  IF NOT EXISTS document_unit_field_of_law_field_of_law_id_idx ON document_unit_field_of_law (field_of_law_id);

CREATE INDEX
  IF NOT EXISTS field_of_law_link_field_id_idx ON field_of_law_link (field_id);

CREATE INDEX
  IF NOT EXISTS field_of_law_link_linked_field_id_idx ON field_of_law_link (linked_field_id);

CREATE INDEX
  IF NOT EXISTS lookuptable_subject_field_parent_id_idx ON lookuptable_subject_field (parent_id);

CREATE INDEX
  IF NOT EXISTS lookuptable_subject_field_keyword_subject_field_id_idx ON lookuptable_subject_field_keyword (subject_field_id);

CREATE INDEX
  IF NOT EXISTS lookuptable_subject_field_norm_subject_field_id_idx ON lookuptable_subject_field_norm (subject_field_id);
