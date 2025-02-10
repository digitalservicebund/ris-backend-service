package de.bund.digitalservice.ris.caselaw.adapter;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentNumberRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.domain.DateUtil;
import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberRecyclingService;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentNumberFormatterException;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentNumberPatternException;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitExistsException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class)
@EnableConfigurationProperties(value = DocumentNumberPatternConfig.class)
@Import(DatabaseDocumentNumberGeneratorService.class)
class DatabaseDocumentNumberGeneratorServiceTest {

  @Autowired DocumentNumberPatternConfig documentNumberPatternConfig;

  @MockitoBean DatabaseDocumentNumberRepository databaseDocumentNumberRepository;

  @MockitoBean DatabaseDocumentationUnitRepository databaseDocumentationUnitRepository;

  @Autowired DatabaseDocumentNumberGeneratorService service;

  @MockitoBean DocumentNumberRecyclingService documentNumberRecyclingService;

  private static final String DEFAULT_ABBREVIATION = "BGH";

  private static String generateDefaultDocumentNumber() {
    return "KORE70001" + DateUtil.getYear();
  }

  @Test
  void shouldThrowErrorIfDocumentAlreadyExists() {
    var nextDocumentNumber = generateDefaultDocumentNumber();

    DocumentationUnitDTO documentationUnitDTO =
        DecisionDTO.builder().id(UUID.randomUUID()).documentNumber(nextDocumentNumber).build();

    when(databaseDocumentationUnitRepository.findByDocumentNumber(nextDocumentNumber))
        .thenReturn(Optional.of(documentationUnitDTO));

    assertThatThrownBy(() -> service.generateDocumentNumber(DEFAULT_ABBREVIATION))
        .isInstanceOf(DocumentationUnitExistsException.class);
  }

  @Test
  void shouldRecycleDocumentNumber()
      throws DocumentNumberPatternException,
          DocumentNumberFormatterException,
          DocumentationUnitExistsException {
    var nextDocumentNumber = generateDefaultDocumentNumber();

    Assertions.assertEquals(
        service.generateDocumentNumber(DEFAULT_ABBREVIATION), nextDocumentNumber);
  }

  @Test
  void shouldThrowException_ifDocumentNumberExists() {
    var nextDocumentNumber = generateDefaultDocumentNumber();

    DocumentationUnitDTO documentationUnitDTO =
        DecisionDTO.builder().id(UUID.randomUUID()).documentNumber(nextDocumentNumber).build();

    when(databaseDocumentationUnitRepository.findByDocumentNumber(nextDocumentNumber))
        .thenReturn(Optional.of(documentationUnitDTO));

    assertThatThrownBy(() -> service.generateDocumentNumber(DEFAULT_ABBREVIATION))
        .isInstanceOf(DocumentationUnitExistsException.class)
        .hasMessageContaining("Document number already exists: " + nextDocumentNumber);
  }

  @Test
  void shouldStopTrying_ifPatternIsInvalid() {
    var docOfficeAbbreviation = "NOT_IN_NUMBER_PATTERN_PROPERTIES";

    assertThatThrownBy(() -> service.generateDocumentNumber(docOfficeAbbreviation))
        .isInstanceOf(DocumentNumberPatternException.class)
        .hasMessageContaining(
            "Could not " + "find pattern for abbreviation " + docOfficeAbbreviation);
  }

  @Test
  void getOrCreateDocumentNumberDTO_startsByZero_ifYearAndAbbreviationDoNotExist() {

    var documentNumberDto = service.getOrCreateDocumentNumberDTO(DEFAULT_ABBREVIATION);

    Assertions.assertEquals(0, documentNumberDto.getLastNumber());
    Assertions.assertEquals(DateUtil.getYear(), documentNumberDto.getYear());
    Assertions.assertEquals(
        DEFAULT_ABBREVIATION, documentNumberDto.getDocumentationOfficeAbbreviation());

    verify(databaseDocumentNumberRepository)
        .findByDocumentationOfficeAbbreviationAndYear(DEFAULT_ABBREVIATION, DateUtil.getYear());
  }

  @Test
  void getOrCreateDocumentNumberDTO_shouldContinuesFromLast_ifYearAndAbbreviationExist() {
    when(databaseDocumentNumberRepository.findByDocumentationOfficeAbbreviationAndYear(
            DEFAULT_ABBREVIATION, DateUtil.getYear()))
        .thenReturn(
            Optional.of(
                DocumentNumberDTO.builder()
                    .lastNumber(4)
                    .year(DateUtil.getYear())
                    .documentationOfficeAbbreviation(DEFAULT_ABBREVIATION)
                    .build()));

    var documentNumberDto = service.getOrCreateDocumentNumberDTO(DEFAULT_ABBREVIATION);

    Assertions.assertEquals(4, documentNumberDto.getLastNumber());
    Assertions.assertEquals(DateUtil.getYear(), documentNumberDto.getYear());
    Assertions.assertEquals(
        DEFAULT_ABBREVIATION, documentNumberDto.getDocumentationOfficeAbbreviation());

    verify(databaseDocumentNumberRepository)
        .findByDocumentationOfficeAbbreviationAndYear(DEFAULT_ABBREVIATION, DateUtil.getYear());
  }
}
