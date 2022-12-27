ALTER TABLE
  norms
ADD
  ris_abbreviation_international_law VARCHAR(255),
ADD
  document_number VARCHAR(255),
ADD
  divergent_document_number VARCHAR(255),
ADD
  document_category VARCHAR(255),
ADD
  entry_into_force_date DATE,
ADD
  entry_into_force_date_state VARCHAR(255),
ADD
  principle_entry_into_force_date DATE,
ADD
  principle_entry_into_force_date_state VARCHAR(255),
ADD
  divergent_entry_into_force_date DATE,
ADD
  divergent_entry_into_force_date_state VARCHAR(255),
ADD
  expiration_date DATE,
ADD
  expiration_date_state VARCHAR(255),
ADD
  is_expiration_date_temp BOOLEAN,
ADD
  principle_expiration_date DATE,
ADD
  principle_expiration_date_state VARCHAR(255),
ADD
  divergent_expiration_date DATE,
ADD
  divergent_expiration_date_state VARCHAR(255),
ADD
  expiration_norm_category VARCHAR(255),
ADD
  print_announcement_gazette VARCHAR(255),
ADD
  print_announcement_year VARCHAR(255),
ADD
  print_announcement_number VARCHAR(255),
ADD
  print_announcement_page VARCHAR(255),
ADD
  print_announcement_info VARCHAR(255),
ADD
  print_announcement_explanations VARCHAR(255),
ADD
  digital_announcement_medium VARCHAR(255),
ADD
  digital_accouncement_date DATE,
ADD
  digital_announcement_edition VARCHAR(255),
ADD
  digital_announcement_year VARCHAR(255),
ADD
  digital_announcement_page VARCHAR(255),
ADD
  digital_announcement_area VARCHAR(255),
ADD
  digital_announcement_area_number VARCHAR(255),
ADD
  digital_announcement_info VARCHAR(255),
ADD
  digital_announcement_explanations VARCHAR(255),
ADD
  eu_announcement_gazette VARCHAR(255),
ADD
  eu_announcement_year VARCHAR(255),
ADD
  eu_announcement_series VARCHAR(255),
ADD
  eu_announcement_number VARCHAR(255),
ADD
  eu_announcement_page VARCHAR(255),
ADD
  eu_announcement_info VARCHAR(255),
ADD
  eu_announcement_explanations VARCHAR(255),
ADD
  other_official_announcement VARCHAR(255),
ADD
  unofficial_reference VARCHAR(255),
ADD
  complete_citation VARCHAR(255),
ADD
  status_note VARCHAR(255),
ADD
  status_description VARCHAR(255),
ADD
  status_date DATE,
ADD
  status_reference VARCHAR(255),
ADD
  repeal_note VARCHAR(255),
ADD
  repeal_article VARCHAR(255),
ADD
  repeal_date DATE,
ADD
  repeal_references VARCHAR(255),
ADD
  reissue_note VARCHAR(255),
ADD
  reissue_article VARCHAR(255),
ADD
  reissue_date DATE,
ADD
  reissue_reference VARCHAR(255),
ADD
  other_status_note VARCHAR(255),
ADD
  document_status_work_note VARCHAR(255),
ADD
  document_status_description VARCHAR(255),
ADD
  document_status_date DATE,
ADD
  document_status_reference VARCHAR(255),
ADD
  document_status_entry_into_force_date DATE,
ADD
  document_status_proof VARCHAR(255),
ADD
  document_text_proof VARCHAR(255),
ADD
  other_document_note VARCHAR(255),
ADD
  application_scope_area VARCHAR(255),
ADD
  application_scope_start_date DATE,
ADD
  application_scope_end_date DATE,
ADD
  categorized_reference VARCHAR(255),
ADD
  other_footnote VARCHAR(255),
ADD
  validity_rule VARCHAR(255),
ADD
  digital_evidence_link VARCHAR(255),
ADD
  digital_evidence_related_data VARCHAR(255),
ADD
  digital_evidence_external_data_note VARCHAR(255),
ADD
  digital_evidence_appendix VARCHAR(255),
ADD
  european_legal_identifier VARCHAR(255),
ADD
  celex_number VARCHAR(255),
ADD
  age_indication_start VARCHAR(255),
ADD
  age_indication_end VARCHAR(255),
ADD
  definition VARCHAR(255),
ADD
  age_of_majority_indication VARCHAR(255),
ADD
  text VARCHAR(255);
