package de.bund.digitalservice.ris.caselaw.domain;

import java.util.UUID;
import lombok.Builder;

@Builder
public record AbuseFee(
    UUID id, boolean newEntry, int amount, CurrencyCode currencyCode, Addressee addressee) {}
