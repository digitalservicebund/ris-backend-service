package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import lombok.Builder;

@Builder(toBuilder = true)
public record RisJsonPatch(
    Long documentationUnitVersion, MergeableJsonPatch patch, List<String> errorPaths) {}
