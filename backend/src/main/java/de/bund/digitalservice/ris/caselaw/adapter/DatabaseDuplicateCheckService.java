package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseCourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDuplicateCheckRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingDateDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitIdDuplicateCheckDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DuplicateRelationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.CourtTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateCheckService;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateRelationStatus;
import de.bund.digitalservice.ris.caselaw.domain.FeatureToggleService;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DatabaseDuplicateCheckService implements DuplicateCheckService {
  private final DatabaseDuplicateCheckRepository repository;
  private final DuplicateRelationService duplicateRelationService;
  private final DatabaseDocumentationUnitRepository documentationUnitRepository;
  private final DatabaseCourtRepository databaseCourtRepository;

  private final FeatureToggleService featureToggleService;

  public DatabaseDuplicateCheckService(
      DatabaseDuplicateCheckRepository duplicateCheckRepository,
      DuplicateRelationService duplicateRelationService,
      DatabaseDocumentationUnitRepository documentationUnitRepository,
      DatabaseCourtRepository databaseCourtRepository,
      FeatureToggleService featureToggleService) {
    this.repository = duplicateCheckRepository;
    this.duplicateRelationService = duplicateRelationService;
    this.documentationUnitRepository = documentationUnitRepository;
    this.databaseCourtRepository = databaseCourtRepository;
    this.featureToggleService = featureToggleService;
  }

  @Override
  @Transactional
  public void checkDuplicates(String docNumber) {
    try {
      if (!featureToggleService.isEnabled("neuris.duplicate-check")) {
        return;
      }

      var documentationUnit =
          documentationUnitRepository.findByDocumentNumber(docNumber).orElseThrow();

      List<DocumentationUnitIdDuplicateCheckDTO> duplicates =
          findPotentialDuplicates(documentationUnit);

      processDuplicates(documentationUnit, duplicates);
      removeObsoleteDuplicates(documentationUnit, duplicates);
    } catch (Exception e) {
      var errorMessage = String.format("Could not check duplicates for doc unit %s", docNumber);
      log.error(errorMessage, e);
    }
  }

  private List<DocumentationUnitIdDuplicateCheckDTO> findPotentialDuplicates(
      DocumentationUnitDTO documentationUnit) {
    var allFileNumbers = collectFileNumbers(documentationUnit);
    var allEclis = collectEclis(documentationUnit);

    if (allFileNumbers.isEmpty() && allEclis.isEmpty()) {
      // As duplicates depend on either fileNumber/ECLI, without these attributes -> no duplicates
      return List.of();
    }

    var allDates = collectDecisionDates(documentationUnit);
    var allCourtIds = collectCourtIds(documentationUnit);
    var allDeviatingCourts = collectDeviatingCourts(documentationUnit);
    var allDocTypeIds = collectDocumentTypeIds(documentationUnit);

    return findPotentialDuplicates(
        documentationUnit,
        allFileNumbers,
        allDates,
        allCourtIds,
        allDeviatingCourts,
        allEclis,
        allDocTypeIds);
  }

  @Override
  public String updateDuplicateStatus(
      String docNumberOrigin, String docNumberDuplicate, DuplicateRelationStatus status)
      throws DocumentationUnitNotExistsException {
    var originDocUnit =
        documentationUnitRepository
            .findByDocumentNumber(docNumberOrigin)
            .orElseThrow(DocumentationUnitNotExistsException::new);
    var duplicateDocUnit =
        documentationUnitRepository
            .findByDocumentNumber(docNumberDuplicate)
            .orElseThrow(DocumentationUnitNotExistsException::new);
    var duplicateRelation =
        duplicateRelationService
            .findByDocUnitIds(originDocUnit.getId(), duplicateDocUnit.getId())
            .orElseThrow(EntityNotFoundException::new);
    duplicateRelationService.setStatus(duplicateRelation, status);
    return "The duplicate status has been successfully updated to " + status;
  }

  private List<String> collectFileNumbers(DocumentationUnitDTO documentationUnit) {
    var fileNumbers = documentationUnit.getFileNumbers();
    var deviatingFileNumbers = documentationUnit.getDeviatingFileNumbers();
    List<String> allFileNumbers = new ArrayList<>();
    if (fileNumbers != null) {
      allFileNumbers.addAll(
          fileNumbers.stream()
              .map(fileNumberDTO -> fileNumberDTO.getValue().toUpperCase())
              .toList());
    }
    if (deviatingFileNumbers != null) {
      allFileNumbers.addAll(
          deviatingFileNumbers.stream()
              .map(deviatingFileNumberDTO -> deviatingFileNumberDTO.getValue().toUpperCase())
              .toList());
    }
    return allFileNumbers;
  }

  private List<String> collectEclis(DocumentationUnitDTO documentationUnit) {
    var ecli = documentationUnit.getEcli();
    var deviatingEclis = documentationUnit.getDeviatingEclis();
    List<String> allEclis = new ArrayList<>();
    if (ecli != null) {
      allEclis.add(ecli.toUpperCase());
    }
    if (deviatingEclis != null) {
      allEclis.addAll(
          deviatingEclis.stream()
              .map(deviatingEcliDTO -> deviatingEcliDTO.getValue().toUpperCase())
              .toList());
    }
    return allEclis;
  }

  private List<LocalDate> collectDecisionDates(DocumentationUnitDTO documentationUnit) {
    var decisionDate = documentationUnit.getDecisionDate();
    var deviatingDecisionDates = documentationUnit.getDeviatingDates();
    List<LocalDate> allDates = new ArrayList<>();
    if (decisionDate != null) {
      allDates.add(decisionDate);
    }
    if (!deviatingDecisionDates.isEmpty()) {
      allDates.addAll(deviatingDecisionDates.stream().map(DeviatingDateDTO::getValue).toList());
    }
    return allDates;
  }

  private List<UUID> collectCourtIds(DocumentationUnitDTO documentationUnit) {
    List<UUID> allCourtIds = new ArrayList<>();
    var court = documentationUnit.getCourt();
    if (court != null) {
      allCourtIds.add(court.getId());
    }
    for (var deviatingCourt : documentationUnit.getDeviatingCourts()) {
      var courtDTOs = databaseCourtRepository.findBySearchStr(deviatingCourt.getValue(), 2);
      // The label of the deviating court must be unique to be considered
      if (courtDTOs.size() == 1) {
        allCourtIds.add(courtDTOs.getFirst().getId());
      }
    }
    return allCourtIds;
  }

  private List<String> collectDeviatingCourts(DocumentationUnitDTO documentationUnit) {
    List<String> allDeviatingCourts = new ArrayList<>();
    var deviatingCourts = documentationUnit.getDeviatingCourts();
    if (deviatingCourts != null) {
      allDeviatingCourts.addAll(
          deviatingCourts.stream()
              .map(deviatingCourtDTO -> deviatingCourtDTO.getValue().toUpperCase())
              .toList());
    }
    // If doc unit A has deviating court "AG Aachen" (string)
    // and doc unit B court "AG Aachen" (object) -> should still lead to a duplicate warning.
    var court = documentationUnit.getCourt();
    if (court != null) {
      allDeviatingCourts.add(
          CourtTransformer.generateLabel(court.getType(), court.getLocation()).toUpperCase());
    }
    return allDeviatingCourts;
  }

  private List<UUID> collectDocumentTypeIds(DocumentationUnitDTO documentationUnit) {
    List<UUID> allDocTypeIds = new ArrayList<>();
    var documentationType = documentationUnit.getDocumentType();
    if (documentationType != null) {
      allDocTypeIds.add(documentationType.getId());
    }
    return allDocTypeIds;
  }

  private List<DocumentationUnitIdDuplicateCheckDTO> findPotentialDuplicates(
      DocumentationUnitDTO documentationUnit,
      List<String> allFileNumbers,
      List<LocalDate> allDates,
      List<UUID> allCourtIds,
      List<String> allDeviatingCourts,
      List<String> allEclis,
      List<UUID> allDocTypeIds) {
    return repository
        .findDuplicates(
            allFileNumbers, allDates, allCourtIds, allDeviatingCourts, allEclis, allDocTypeIds)
        .stream()
        // Should not contain itself
        .filter(dup -> !documentationUnit.getId().equals(dup.getId()))
        .toList();
  }

  private void processDuplicates(
      DocumentationUnitDTO documentationUnit,
      List<DocumentationUnitIdDuplicateCheckDTO> duplicates) {
    for (var dup : duplicates) {
      Optional<DuplicateRelationDTO> existingRelation =
          duplicateRelationService.findByDocUnitIds(documentationUnit.getId(), dup.getId());

      var isJDVDuplicateCheckActive =
          !Boolean.FALSE.equals(dup.getIsJdvDuplicateCheckActive())
              && !Boolean.FALSE.equals(documentationUnit.getIsJdvDuplicateCheckActive());

      var status =
          isJDVDuplicateCheckActive
              ? DuplicateRelationStatus.PENDING
              : DuplicateRelationStatus.IGNORED;

      if (existingRelation.isEmpty()) {
        createDuplicateRelation(documentationUnit, dup, status);
      } else if (shouldUpdateRelationStatus(isJDVDuplicateCheckActive, existingRelation)) {
        duplicateRelationService.setStatus(existingRelation.get(), status);
      }
    }
  }

  private void createDuplicateRelation(
      DocumentationUnitDTO documentationUnit,
      DocumentationUnitIdDuplicateCheckDTO dup,
      DuplicateRelationStatus status) {
    var identifiedDuplicate = documentationUnitRepository.findById(dup.getId());
    var currentDocUnit = documentationUnitRepository.findById(documentationUnit.getId());

    if (identifiedDuplicate.isPresent() && currentDocUnit.isPresent()) {
      duplicateRelationService.create(currentDocUnit.get(), identifiedDuplicate.get(), status);
    }
  }

  /**
   * If the relationship is PENDING and the legacy jdv "dup-code ausschalten" applies, the status
   * must be set to IGNORED.
   */
  private boolean shouldUpdateRelationStatus(
      boolean isJDVDuplicateCheckActive, Optional<DuplicateRelationDTO> existingRelation) {
    return !isJDVDuplicateCheckActive
        && existingRelation.isPresent()
        && DuplicateRelationStatus.PENDING.equals(existingRelation.get().getStatus());
  }

  private void removeObsoleteDuplicates(
      DocumentationUnitDTO documentationUnit,
      List<DocumentationUnitIdDuplicateCheckDTO> duplicates) {
    var existingDuplicates = duplicateRelationService.findAllByDocUnitId(documentationUnit.getId());
    for (var existingDuplicate : existingDuplicates) {
      var isDuplicate =
          duplicates.stream()
              .map(
                  dup ->
                      new DuplicateRelationDTO.DuplicateRelationId(
                          documentationUnit.getId(), dup.getId()))
              .anyMatch(dupId -> dupId.equals(existingDuplicate.getId()));

      if (!isDuplicate) {
        duplicateRelationService.delete(existingDuplicate);
      }
    }
  }
}
