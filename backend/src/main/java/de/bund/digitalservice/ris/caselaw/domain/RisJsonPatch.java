package de.bund.digitalservice.ris.caselaw.domain;

import com.gravity9.jsonpatch.JsonPatch;
import java.util.List;
import lombok.Builder;

@Builder(toBuilder = true)
public record RisJsonPatch(
    Long documentationUnitVersion, JsonPatch patch, List<String> errorPaths) {}
