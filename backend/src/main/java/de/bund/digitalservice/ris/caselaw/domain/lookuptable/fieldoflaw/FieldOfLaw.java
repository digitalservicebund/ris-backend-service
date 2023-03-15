package de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw;

import java.util.List;
import lombok.Builder;

@Builder
public record FieldOfLaw(
    Long id,
    Integer childrenCount,
    String identifier,
    String text,
    List<String> linkedFields,
    List<Keyword> keywords,
    List<Norm> norms,
    List<FieldOfLaw> children) {}
