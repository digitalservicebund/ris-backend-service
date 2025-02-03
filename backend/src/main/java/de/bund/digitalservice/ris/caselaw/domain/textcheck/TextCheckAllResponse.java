package de.bund.digitalservice.ris.caselaw.domain.textcheck;

import java.util.List;
import lombok.Builder;

@Builder
public record TextCheckAllResponse(List<Suggestion> suggestions) {}
