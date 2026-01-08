package de.bund.digitalservice.ris.caselaw.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;
import lombok.Builder;

/** DE: Tarifvertrag */
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record CollectiveAgreement(
    UUID id, String name, String date, String norm, CollectiveAgreementIndustry industry) {}
