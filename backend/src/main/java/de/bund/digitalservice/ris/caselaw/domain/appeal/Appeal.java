package de.bund.digitalservice.ris.caselaw.domain.appeal;

import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record Appeal(
    UUID id,
    List<Appellant> appellants,
    List<AppealStatus> revisionDefendantStatuses,
    List<AppealStatus> revisionPlaintiffStatuses,
    List<AppealStatus> jointRevisionDefendantStatuses,
    List<AppealStatus> jointRevisionPlaintiffStatuses,
    List<AppealStatus> nzbDefendantStatuses,
    List<AppealStatus> nzbPlaintiffStatuses,
    AppealWithdrawal appealWithdrawal,
    PkhPlaintiff pkhPlaintiff) {}
