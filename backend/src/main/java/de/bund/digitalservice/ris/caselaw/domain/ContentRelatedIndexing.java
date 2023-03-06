package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.FieldOfLaw;
import java.util.List;

public record ContentRelatedIndexing(List<String> keywords, List<FieldOfLaw> fieldsOfLaw) {}
