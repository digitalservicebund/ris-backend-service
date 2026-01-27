package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CollectiveAgreementDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CollectiveAgreementIndustryDTO;
import de.bund.digitalservice.ris.caselaw.domain.CollectiveAgreement;
import de.bund.digitalservice.ris.caselaw.domain.CollectiveAgreementIndustry;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CollectiveAgreementTransformerTest {

  @Test
  void transformToDomain_withNullInput_returnsNull() {
    assertNull(CollectiveAgreementTransformer.transformToDomain(null));
  }

  @Test
  void transformToDomain_withValidDTO_transformsCorrectly() {
    // Arrange
    CollectiveAgreementDTO dto =
        CollectiveAgreementDTO.builder()
            .id(UUID.randomUUID())
            .name("Stehende Bühnen")
            .date("12.2002")
            .norm("§ 23")
            .industry(
                CollectiveAgreementIndustryDTO.builder()
                    .id(UUID.fromString("290b39dc-9368-4d1c-9076-7f96e05cb575"))
                    .value("Bühne, Theater, Orchester")
                    .build())
            .build();

    // Act
    CollectiveAgreement result = CollectiveAgreementTransformer.transformToDomain(dto);

    // Assert
    assertNotNull(result);
    assertEquals(dto.getId(), result.id());
    assertEquals(dto.getName(), result.name());
    assertEquals(dto.getDate(), result.date());
    assertEquals(dto.getNorm(), result.norm());
    assertEquals(dto.getIndustry().getId(), result.industry().id());
  }

  @Test
  void transformToDTO_withNullInput_returnsNull() {
    assertNull(CollectiveAgreementTransformer.transformToDTO(null));
  }

  @Test
  void transformToDTO_withValidDomain_transformsCorrectly() {
    // Arrange
    CollectiveAgreement domainObject =
        CollectiveAgreement.builder()
            .id(UUID.randomUUID())
            .name("Stehende Bühnen")
            .date("12.2002")
            .norm("§ 23")
            .industry(
                new CollectiveAgreementIndustry(
                    UUID.fromString("290b39dc-9368-4d1c-9076-7f96e05cb575"),
                    "Bühne, Theater, Orchester"))
            .build();

    // Act
    CollectiveAgreementDTO dto = CollectiveAgreementTransformer.transformToDTO(domainObject);

    // Assert
    assertNotNull(dto);
    assertEquals(domainObject.id(), dto.getId());
    assertEquals(domainObject.name(), dto.getName());
    assertEquals(domainObject.date(), dto.getDate());
    assertEquals(domainObject.norm(), dto.getNorm());
    assertEquals(domainObject.industry().id(), dto.getIndustry().getId());
  }
}
