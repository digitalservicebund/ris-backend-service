package de.bund.digitalservice.ris.caselaw.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;
import lombok.Builder;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record IncomeType(UUID id, String terminology, TypeOfIncome typeOfIncome) {}
