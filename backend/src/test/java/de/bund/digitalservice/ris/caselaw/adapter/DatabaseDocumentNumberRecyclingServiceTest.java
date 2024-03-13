package de.bund.digitalservice.ris.caselaw.adapter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDeletedDocumentationIdsRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseStatusRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeletedDocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.StatusDTO;
import de.bund.digitalservice.ris.caselaw.domain.DateUtil;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import java.time.Instant;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
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
@Import(DatabaseDocumentNumberRecyclingService.class)
class DatabaseDocumentNumberRecyclingServiceTest {

  @MockBean DatabaseDeletedDocumentationIdsRepository repository;

  @MockBean DatabaseStatusRepository statusRepository;

  @Autowired DatabaseDocumentNumberRecyclingService service;

  private static final String DEFAULT_DOCUMENTATION_OFFICE = "BGH";

  private static String generateDefaultDocumentNumber() {
    return "KORE70001" + DateUtil.getYear();
  }

  @Test
  void shouldNotSaveInvalidPrefix() {
    var documentationUnitDTO =
        DocumentationUnitDTO.builder()
            .id(UUID.randomUUID())
            .documentNumber("KORE" + Year.now() + "00037")
            .build();

    var unpublished = generateStatus(documentationUnitDTO, PublicationStatus.UNPUBLISHED);

    var outdatedDeletedId =
        DeletedDocumentationUnitDTO.builder()
            .documentNumber(documentationUnitDTO.getDocumentNumber())
            .year(Year.now())
            .abbreviation(DEFAULT_DOCUMENTATION_OFFICE)
            .build();

    when(repository.findFirstByAbbreviationAndYear(DEFAULT_DOCUMENTATION_OFFICE, Year.now()))
        .thenReturn(Optional.of(outdatedDeletedId));
    when(statusRepository.findAllByDocumentationUnitDTO_Id(documentationUnitDTO.getId()))
        .thenReturn(List.of(unpublished));
    when(repository.save(any())).thenReturn(generateDeletedDocumentationUnitDTO());

    var saved =
        service.addForRecycling(
            documentationUnitDTO.getId(),
            documentationUnitDTO.getDocumentNumber(),
            DEFAULT_DOCUMENTATION_OFFICE);
    Assertions.assertTrue(saved.isEmpty());
  }

  @Test
  void shouldNotOfferInvalidPrefix() {
    var outdatedDeletedId =
        DeletedDocumentationUnitDTO.builder()
            .documentNumber("KORE" + Year.now() + "00037")
            .year(Year.now())
            .abbreviation(DEFAULT_DOCUMENTATION_OFFICE)
            .build();

    when(repository.findFirstByAbbreviationAndYear(DEFAULT_DOCUMENTATION_OFFICE, Year.now()))
        .thenReturn(Optional.of(outdatedDeletedId));

    Assertions.assertTrue(
        service.findDeletedDocumentNumber(DEFAULT_DOCUMENTATION_OFFICE, Year.now()).isEmpty());
  }

  @Test
  void shouldSaveIfOnly_unpublished() {
    var documentationUnitDTO =
        DocumentationUnitDTO.builder()
            .id(UUID.randomUUID())
            .documentNumber(generateDefaultDocumentNumber())
            .build();

    var unpublished = generateStatus(documentationUnitDTO, PublicationStatus.UNPUBLISHED);

    when(statusRepository.findAllByDocumentationUnitDTO_Id(documentationUnitDTO.getId()))
        .thenReturn(List.of(unpublished));

    when(repository.save(any())).thenReturn(generateDeletedDocumentationUnitDTO());

    var saved =
        service.addForRecycling(
            documentationUnitDTO.getId(),
            documentationUnitDTO.getDocumentNumber(),
            DEFAULT_DOCUMENTATION_OFFICE);

    Assertions.assertTrue(saved.isPresent());
  }

  @Test
  void shouldNotSaveIf_published() {
    var documentationUnitDTO = generateDocumentationUnitDto();

    var published = generateStatus(documentationUnitDTO, PublicationStatus.PUBLISHED);

    when(statusRepository.findAllByDocumentationUnitDTO_Id(documentationUnitDTO.getId()))
        .thenReturn(List.of(published));

    when(repository.save(any())).thenReturn(generateDeletedDocumentationUnitDTO());

    var saved =
        service.addForRecycling(
            documentationUnitDTO.getId(),
            documentationUnitDTO.getDocumentNumber(),
            DEFAULT_DOCUMENTATION_OFFICE);

    Assertions.assertTrue(saved.isEmpty());
  }

  @Test
  void shouldNotSaveIf_multipleStatus() {
    var documentationUnitDto = generateDocumentationUnitDto();

    var unpublished = generateStatus(documentationUnitDto, PublicationStatus.UNPUBLISHED);
    var published = generateStatus(documentationUnitDto, PublicationStatus.PUBLISHED);

    when(statusRepository.findAllByDocumentationUnitDTO_Id(any()))
        .thenReturn(List.of(published, unpublished));

    when(repository.save(any())).thenReturn(generateDeletedDocumentationUnitDTO());

    var saved =
        service.addForRecycling(
            documentationUnitDto.getId(),
            documentationUnitDto.getDocumentNumber(),
            DEFAULT_DOCUMENTATION_OFFICE);

    Assertions.assertTrue(saved.isEmpty());
  }

  private static StatusDTO generateStatus(
      DocumentationUnitDTO documentationUnitDTO, PublicationStatus publicationStatus) {
    return StatusDTO.builder()
        .documentationUnitDTO(documentationUnitDTO)
        .publicationStatus(publicationStatus)
        .createdAt(Instant.now())
        .withError(false)
        .build();
  }

  private static DocumentationUnitDTO generateDocumentationUnitDto() {
    return DocumentationUnitDTO.builder()
        .id(UUID.randomUUID())
        .documentNumber(generateDefaultDocumentNumber())
        .build();
  }

  private static DeletedDocumentationUnitDTO generateDeletedDocumentationUnitDTO() {
    return DeletedDocumentationUnitDTO.builder()
        .documentNumber(generateDefaultDocumentNumber())
        .year(Year.now())
        .abbreviation(DEFAULT_DOCUMENTATION_OFFICE)
        .build();
  }
}
