package de.bund.digitalservice.ris.caselaw.domain.lookuptable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;
import lombok.Builder;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record ParticipatingJudge(UUID id, String name, String referencedOpinions) {}
