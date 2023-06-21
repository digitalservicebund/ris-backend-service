package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import java.util.List;
import lombok.Builder;

@Builder(toBuilder = true)
public record ContentRelatedIndexing(
    List<String> keywords,
    List<FieldOfLaw> fieldsOfLaw,
    List<DocumentUnitNorm> norms,
    List<ActiveCitation> activeCitations) {}
