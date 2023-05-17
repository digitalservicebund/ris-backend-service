package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import java.util.List;

public record ContentRelatedIndexing(
    List<String> keywords, List<FieldOfLaw> fieldsOfLaw, List<DocumentUnitNorm> norms) {}
