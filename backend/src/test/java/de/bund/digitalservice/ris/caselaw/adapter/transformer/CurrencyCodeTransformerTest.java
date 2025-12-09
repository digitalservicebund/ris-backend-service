package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CurrencyCodeDTO;
import de.bund.digitalservice.ris.caselaw.domain.CurrencyCode;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CurrencyCodeTransformerTest {

  @Test
  void transformToDomain_withNullInput_returnsNull() {
    assertNull(CurrencyCodeTransformer.transformToDomain(null));
  }

  @Test
  void transformToDomain_withValidDTO_transformsCorrectly() {
    // Arrange
    CurrencyCodeDTO currencyCodeDTO =
        CurrencyCodeDTO.builder()
            .id(UUID.randomUUID())
            .value("Euro (EUR)")
            .isoCode("EUR")
            .currency("Euro")
            .build();

    // Act
    CurrencyCode result = CurrencyCodeTransformer.transformToDomain(currencyCodeDTO);

    // Assert
    assertNotNull(result);
    assertEquals(currencyCodeDTO.getId(), result.id());
    assertEquals(currencyCodeDTO.getValue(), result.label());
    assertEquals(currencyCodeDTO.getIsoCode(), result.isoCode());
  }

  @Test
  void transformToDTO_withNullInput_returnsNull() {
    assertNull(CurrencyCodeTransformer.transformToDTO(null));
  }

  @Test
  void transformToDTO_withValidDomain_transformsCorrectly() {
    // Arrange
    CurrencyCode currencyCode =
        CurrencyCode.builder().id(UUID.randomUUID()).label("Euro (EUR)").isoCode("EUR").build();

    // Act
    CurrencyCodeDTO currencyCodeDTO = CurrencyCodeTransformer.transformToDTO(currencyCode);

    // Assert
    assertNotNull(currencyCodeDTO);
    assertEquals(currencyCode.id(), currencyCodeDTO.getId());
    assertEquals(currencyCode.label(), currencyCodeDTO.getValue());
    assertEquals(currencyCode.isoCode(), currencyCodeDTO.getIsoCode());
  }
}
