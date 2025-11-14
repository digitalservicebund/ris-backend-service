package de.bund.digitalservice.ris.caselaw.domain;

import java.util.UUID;
import lombok.Builder;

/** DE: Tarifvertrag */
@Builder
public record CollectiveAgreement(
    UUID id,
    boolean newEntry,
    String name,
    String date,
    String norm,
    CollectiveAgreementIndustry industry) {}
