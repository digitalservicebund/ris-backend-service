package de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.Builder;

@Builder(toBuilder = true)
public record FieldOfLaw(
    UUID id,
    boolean hasChildren,
    String identifier,
    String text,
    String notation,
    List<String> linkedFields,
    List<Norm> norms,
    List<FieldOfLaw> children,
    @Nullable FieldOfLaw parent) {}
