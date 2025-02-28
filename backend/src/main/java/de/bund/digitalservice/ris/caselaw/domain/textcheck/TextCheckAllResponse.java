package de.bund.digitalservice.ris.caselaw.domain.textcheck;

import java.util.List;
import java.util.Set;
import lombok.Builder;

@Builder
public record TextCheckAllResponse(
    List<Suggestion> suggestions, Set<CategoryType> categoryTypes, int totalTextCheckErrors) {}
