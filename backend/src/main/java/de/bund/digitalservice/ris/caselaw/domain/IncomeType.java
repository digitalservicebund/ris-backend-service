package de.bund.digitalservice.ris.caselaw.domain;

import java.util.UUID;
import lombok.Builder;

@Builder
public record IncomeType(
    UUID id, boolean newEntry, String terminology, TypeOfIncome typeOfIncome) {}
