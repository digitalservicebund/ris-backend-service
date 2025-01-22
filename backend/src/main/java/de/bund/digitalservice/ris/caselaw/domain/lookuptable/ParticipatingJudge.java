package de.bund.digitalservice.ris.caselaw.domain.lookuptable;

import java.util.UUID;
import lombok.Builder;

@Builder
public record ParticipatingJudge(
    UUID id, boolean newEntry, String name, String referencedOpinions) {}
