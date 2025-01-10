package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDuplicateCheckRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitIdDuplicateCheckDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DuplicateRelationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationUnitTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateRelationStatus;
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
public class DuplicateCheckService {
  private final DatabaseDuplicateCheckRepository repository;
  private final DuplicateRelationService duplicateRelationService;
  private final DatabaseDocumentationUnitRepository documentationUnitRepository;

  public DuplicateCheckService(
      DatabaseDuplicateCheckRepository duplicateCheckRepository,
      DuplicateRelationService duplicateRelationService,
      DatabaseDocumentationUnitRepository documentationUnitRepository) {
    this.repository = duplicateCheckRepository;
    this.duplicateRelationService = duplicateRelationService;
    this.documentationUnitRepository = documentationUnitRepository;
  }

  @Transactional
  public void checkDuplicates(String docNumber) {
    try {

      // TODO: Feature flag

      // TODO: Should we work on DTO instead of domain object?
      var docUnitDto = documentationUnitRepository.findByDocumentNumber(docNumber).orElseThrow();
      var documentationUnit = DocumentationUnitTransformer.transformToDomain(docUnitDto);

      var allFileNumbers = collectFileNumbers(documentationUnit);
      var allEclis = collectEclis(documentationUnit);

      // A duplicate depends on filenumber/ecli
      if (allFileNumbers.isEmpty() && allEclis.isEmpty()) {
        return;
      }

      var allDates = collectDecisionDates(documentationUnit);
      var allCourtIds = collectCourtIds(documentationUnit);
      var allDeviatingCourts = collectDeviatingCourts(documentationUnit);
      var allDocTypeIds = collectDocumentTypeIds(documentationUnit);

      var duplicates =
          findPotentialDuplicates(
              documentationUnit,
              allFileNumbers,
              allDates,
              allCourtIds,
              allDeviatingCourts,
              allEclis,
              allDocTypeIds);

      processDuplicates(documentationUnit, duplicates);
      removeObsoleteDuplicates(documentationUnit, duplicates);
    } catch (Exception e) {
      var errorMessage = String.format("Could not check duplicates for doc unit %s", docNumber);
      log.error(errorMessage, e);
    }
  }

  private List<String> collectFileNumbers(DocumentationUnit documentationUnit) {
    var fileNumbers = documentationUnit.coreData().fileNumbers();
    var deviatingFileNumbers = documentationUnit.coreData().deviatingFileNumbers();
    List<String> allFileNumbers = new ArrayList<>();
    if (fileNumbers != null) {
      allFileNumbers.addAll(fileNumbers.stream().map(String::toUpperCase).toList());
    }
    if (deviatingFileNumbers != null) {
      allFileNumbers.addAll(deviatingFileNumbers.stream().map(String::toUpperCase).toList());
    }
    return allFileNumbers;
  }

  private List<String> collectEclis(DocumentationUnit documentationUnit) {
    var ecli = documentationUnit.coreData().ecli();
    var deviatingEclis = documentationUnit.coreData().deviatingEclis();
    List<String> allEclis = new ArrayList<>();
    if (ecli != null) {
      allEclis.add(ecli.toUpperCase());
    }
    if (deviatingEclis != null) {
      allEclis.addAll(deviatingEclis.stream().map(String::toUpperCase).toList());
    }
    return allEclis;
  }

  private List<LocalDate> collectDecisionDates(DocumentationUnit documentationUnit) {
    var decisionDate = documentationUnit.coreData().decisionDate();
    var deviatingDecisionDates = documentationUnit.coreData().deviatingDecisionDates();
    List<LocalDate> allDates = new ArrayList<>();
    if (decisionDate != null) {
      allDates.add(decisionDate);
    }
    if (!deviatingDecisionDates.isEmpty()) {
      allDates.addAll(deviatingDecisionDates);
    }
    return allDates;
  }

  private List<UUID> collectCourtIds(DocumentationUnit documentationUnit) {
    List<UUID> allCourtIds = new ArrayList<>();
    var court = documentationUnit.coreData().court();
    if (court != null) {
      allCourtIds.add(court.id());
    }
    return allCourtIds;
  }

  private List<String> collectDeviatingCourts(DocumentationUnit documentationUnit) {
    List<String> allDeviatingCourts = new ArrayList<>();
    var deviatingCourts = documentationUnit.coreData().deviatingCourts();
    if (deviatingCourts != null) {
      allDeviatingCourts.addAll(deviatingCourts.stream().map(String::toUpperCase).toList());
    }
    return allDeviatingCourts;
  }

  private List<UUID> collectDocumentTypeIds(DocumentationUnit documentationUnit) {
    List<UUID> allDocTypeIds = new ArrayList<>();
    var documentationType = documentationUnit.coreData().documentType();
    if (documentationType != null) {
      allDocTypeIds.add(documentationType.uuid());
    }
    return allDocTypeIds;
  }

  private List<DocumentationUnitIdDuplicateCheckDTO> findPotentialDuplicates(
      DocumentationUnit documentationUnit,
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
        .filter(dup -> !documentationUnit.uuid().equals(dup.getId()))
        .toList();
  }

  private void processDuplicates(
      DocumentationUnit documentationUnit, List<DocumentationUnitIdDuplicateCheckDTO> duplicates) {
    for (var dup : duplicates) {
      Optional<DuplicateRelationDTO> existingRelation =
          duplicateRelationService.findByDocUnitIds(documentationUnit.uuid(), dup.getId());

      var status =
          Boolean.FALSE.equals(dup.getIsJdvDuplicateCheckActive())
              ? DuplicateRelationStatus.IGNORED
              : DuplicateRelationStatus.PENDING;

      if (existingRelation.isEmpty()) {
        createDuplicateRelation(documentationUnit, dup, status);
      } else if (shouldUpdateRelationStatus(dup, existingRelation)) {
        duplicateRelationService.setStatus(existingRelation.get(), status);
      }
    }
  }

  private void createDuplicateRelation(
      DocumentationUnit documentationUnit,
      DocumentationUnitIdDuplicateCheckDTO dup,
      DuplicateRelationStatus status) {
    var identifiedDuplicate = documentationUnitRepository.findById(dup.getId());
    var currentDocUnit = documentationUnitRepository.findById(documentationUnit.uuid());

    if (identifiedDuplicate.isPresent() && currentDocUnit.isPresent()) {
      duplicateRelationService.create(currentDocUnit.get(), identifiedDuplicate.get(), status);
    }
  }

  private boolean shouldUpdateRelationStatus(
      DocumentationUnitIdDuplicateCheckDTO dup, Optional<DuplicateRelationDTO> existingRelation) {
    return dup.getIsJdvDuplicateCheckActive() != null
        && Boolean.FALSE.equals(
            dup.getIsJdvDuplicateCheckActive()
                && existingRelation.isPresent()
                && DuplicateRelationStatus.PENDING.equals(existingRelation.get().getStatus()));
  }

  private void removeObsoleteDuplicates(
      DocumentationUnit documentationUnit, List<DocumentationUnitIdDuplicateCheckDTO> duplicates) {
    var existingDuplicates = duplicateRelationService.findAllByDocUnitId(documentationUnit.uuid());
    for (var existingDuplicate : existingDuplicates) {
      var isDuplicate =
          duplicates.stream()
              .map(
                  dup ->
                      new DuplicateRelationDTO.DuplicateRelationId(
                          documentationUnit.uuid(), dup.getId()))
              .anyMatch(dupId -> dupId.equals(existingDuplicate.getId()));

      if (!isDuplicate) {
        duplicateRelationService.delete(existingDuplicate);
      }
    }
  }
}
