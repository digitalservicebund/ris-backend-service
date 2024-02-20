package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ApiKeyDTO;
import de.bund.digitalservice.ris.caselaw.domain.ApiKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class ApiKeyTransformer {
  private ApiKeyTransformer() {}

  public static ApiKey transformToDomain(ApiKeyDTO dto) {
    return ApiKey.builder()
        .apiKey(dto.getApiKey())
        .validUntil(LocalDateTime.ofInstant(dto.getValidUntil(), ZoneId.of("Europe/Berlin")))
        .valid(dto.getValidUntil().isAfter(Instant.now()))
        .build();
  }
}
