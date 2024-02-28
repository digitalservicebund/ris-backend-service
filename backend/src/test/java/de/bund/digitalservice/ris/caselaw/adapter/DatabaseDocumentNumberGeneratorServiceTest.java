package de.bund.digitalservice.ris.caselaw.adapter;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentNumberRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.domain.DateUtil;
import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberPatternException;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitException;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitExistsException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class)
@EnableConfigurationProperties(value = DocumentNumberPatternConfig.class)
@Import(DatabaseDocumentNumberGeneratorService.class)
class DatabaseDocumentNumberGeneratorServiceTest {

  @Autowired DocumentNumberPatternConfig documentNumberPatternConfig;
  @MockBean DatabaseDocumentNumberRepository databaseDocumentNumberRepository;
  @MockBean DatabaseDocumentationUnitRepository databaseDocumentationUnitRepository;

  @Autowired DatabaseDocumentNumberGeneratorService service;

  @Test
  void shouldThrowErrorIfDocumentAlreadyExists() {
    var nextDocumentNumber = "KORE70001" + DateUtil.getYear();

    var abbreviation = "BGH";

    DocumentationUnitDTO documentationUnitDTO =
        DocumentationUnitDTO.builder()
            .id(UUID.randomUUID())
            .documentNumber(nextDocumentNumber)
            .build();
    when(databaseDocumentationUnitRepository.findByDocumentNumber(nextDocumentNumber))
        .thenReturn(Optional.of(documentationUnitDTO));

    assertThatThrownBy(() -> service.generateDocumentNumber(abbreviation))
        .isInstanceOf(DocumentationUnitExistsException.class);
  }

  @Test
  void shouldKeepTrying_ifDocumentNumberExists() {
    var nextDocumentNumber = "KORE70001" + DateUtil.getYear();

    var documentationOffice = "BGH";
    int attempts = 3;

    DocumentationUnitDTO documentationUnitDTO =
        DocumentationUnitDTO.builder()
            .id(UUID.randomUUID())
            .documentNumber(nextDocumentNumber)
            .build();

    when(databaseDocumentationUnitRepository.findByDocumentNumber(nextDocumentNumber))
        .thenReturn(Optional.of(documentationUnitDTO));

    assertThatThrownBy(() -> service.generateDocumentNumber(documentationOffice, attempts))
        .isInstanceOf(DocumentationUnitException.class)
        .hasMessageContaining("Could not generate Document number");
  }

  @Test
  void shouldStopTrying_ifPatternIsInvalid() {
    var docOfficeAbbreviation = "NOT_IN_NUMBER_PATTERN_PROPERTIES";
    int attempts = 3;

    assertThatThrownBy(() -> service.generateDocumentNumber(docOfficeAbbreviation, attempts))
        .isInstanceOf(DocumentNumberPatternException.class)
        .hasMessageContaining(
            "Could not " + "find pattern for abbreviation " + docOfficeAbbreviation);
  }
}
