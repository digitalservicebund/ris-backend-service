package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.ParticipatingJudge;
import java.util.List;
import lombok.Builder;

@Builder
public record LongTexts(
    String tenor,
    String reasons,
    String caseFacts,
    String decisionReasons,
    String dissentingOpinion,
    List<ParticipatingJudge> participatingJudges,
    String otherLongText,
    String outline) {}
