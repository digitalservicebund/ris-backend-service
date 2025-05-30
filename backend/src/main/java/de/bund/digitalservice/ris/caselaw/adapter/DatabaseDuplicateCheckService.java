package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseCourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDuplicateCheckRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingDateDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitIdDuplicateCheckDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DuplicateRelationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.StatusDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.CourtTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateCheckService;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateRelationStatus;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DatabaseDuplicateCheckService implements DuplicateCheckService {
  private final DatabaseDuplicateCheckRepository repository;
  private final DuplicateRelationService duplicateRelationService;
  private final DatabaseDocumentationUnitRepository documentationUnitRepository;
  private final DatabaseCourtRepository databaseCourtRepository;

  public DatabaseDuplicateCheckService(
      DatabaseDuplicateCheckRepository duplicateCheckRepository,
      DuplicateRelationService duplicateRelationService,
      DatabaseDocumentationUnitRepository documentationUnitRepository,
      DatabaseCourtRepository databaseCourtRepository) {
    this.repository = duplicateCheckRepository;
    this.duplicateRelationService = duplicateRelationService;
    this.documentationUnitRepository = documentationUnitRepository;
    this.databaseCourtRepository = databaseCourtRepository;
  }

  @Override
  @Transactional
  public void checkDuplicates(String docNumber) {
    try {
      var documentationUnit =
          documentationUnitRepository.findByDocumentNumber(docNumber).orElseThrow();

      if (documentationUnit instanceof DecisionDTO decisionDTO) {
        List<DocumentationUnitIdDuplicateCheckDTO> duplicates =
            findPotentialDuplicates(decisionDTO);
        processDuplicates(decisionDTO, duplicates);
        removeObsoleteDuplicates(decisionDTO, duplicates);
      }

    } catch (Exception e) {
      log.error("Could not check duplicates for doc unit {}", docNumber, e);
    }
  }

  @Scheduled(cron = "10 5 5 * * *", zone = "Europe/Berlin")
  @SchedulerLock(name = "duplicate-check-job", lockAtMostFor = "PT15M")
  @Transactional
  @Override
  public void checkAllDuplicates() {
    try {
      this.duplicateRelationService.updateAllDuplicates();
    } catch (Exception e) {
      log.error("Error while updating duplicate relations", e);
    }
  }

  private List<DocumentationUnitIdDuplicateCheckDTO> findPotentialDuplicates(
      DecisionDTO decisionDTO) {
    var allFileNumbers = collectFileNumbers(decisionDTO);
    var allEclis = collectEclis(decisionDTO);

    if (allFileNumbers.isEmpty() && allEclis.isEmpty()) {
      // As duplicates depend on either fileNumber/ECLI, without these attributes -> no duplicates
      return List.of();
    }

    var allDates = collectDecisionDates(decisionDTO);
    var allCourtIds = collectCourtIds(decisionDTO);
    var allDeviatingCourts = collectDeviatingCourts(decisionDTO);
    var allDocTypeIds = collectDocumentTypeIds(decisionDTO);

    return findPotentialDuplicates(
        decisionDTO,
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

  private List<String> collectFileNumbers(DecisionDTO decisionDTO) {
    var fileNumbers = decisionDTO.getFileNumbers();
    var deviatingFileNumbers = decisionDTO.getDeviatingFileNumbers();
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

  private List<String> collectEclis(DecisionDTO decisionDTO) {
    var ecli = decisionDTO.getEcli();
    var deviatingEclis = decisionDTO.getDeviatingEclis();
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

  private List<LocalDate> collectDecisionDates(DecisionDTO decisionDTO) {
    var decisionDate = decisionDTO.getDate();
    var deviatingDecisionDates = decisionDTO.getDeviatingDates();
    List<LocalDate> allDates = new ArrayList<>();
    if (decisionDate != null) {
      allDates.add(decisionDate);
    }
    if (!deviatingDecisionDates.isEmpty()) {
      allDates.addAll(deviatingDecisionDates.stream().map(DeviatingDateDTO::getValue).toList());
    }
    return allDates;
  }

  private List<UUID> collectCourtIds(DecisionDTO decisionDTO) {
    List<UUID> allCourtIds = new ArrayList<>();
    var court = decisionDTO.getCourt();
    if (court != null) {
      allCourtIds.add(court.getId());
    }
    for (var deviatingCourt : decisionDTO.getDeviatingCourts()) {
      var courtDTOs = databaseCourtRepository.findBySearchStr(deviatingCourt.getValue(), 2);
      // The label of the deviating court must be unique to be considered
      if (courtDTOs.size() == 1) {
        allCourtIds.add(courtDTOs.getFirst().getId());
      }
    }
    return allCourtIds;
  }

  private List<String> collectDeviatingCourts(DecisionDTO decisionDTO) {
    List<String> allDeviatingCourts = new ArrayList<>();
    var deviatingCourts = decisionDTO.getDeviatingCourts();
    if (deviatingCourts != null) {
      allDeviatingCourts.addAll(
          deviatingCourts.stream()
              .map(deviatingCourtDTO -> deviatingCourtDTO.getValue().toUpperCase())
              .toList());
    }
    // If doc unit A has deviating court "AG Aachen" (string)
    // and doc unit B court "AG Aachen" (object) -> should still lead to a duplicate warning.
    var court = decisionDTO.getCourt();
    if (court != null) {
      allDeviatingCourts.add(
          CourtTransformer.generateLabel(court.getType(), court.getLocation()).toUpperCase());
    }
    return allDeviatingCourts;
  }

  private List<UUID> collectDocumentTypeIds(DecisionDTO decisionDTO) {
    List<UUID> allDocTypeIds = new ArrayList<>();
    var documentationType = decisionDTO.getDocumentType();
    if (documentationType != null) {
      allDocTypeIds.add(documentationType.getId());
    }
    return allDocTypeIds;
  }

  private List<DocumentationUnitIdDuplicateCheckDTO> findPotentialDuplicates(
      DecisionDTO decisionDTO,
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
        .filter(dup -> !decisionDTO.getId().equals(dup.getId()))
        .toList();
  }

  private void processDuplicates(
      DecisionDTO decisionDTO, List<DocumentationUnitIdDuplicateCheckDTO> duplicates) {
    for (var dup : duplicates) {
      Optional<DuplicateRelationDTO> existingRelation =
          duplicateRelationService.findByDocUnitIds(decisionDTO.getId(), dup.getId());

      var duplicateRelationStatus = getRelationStatus(decisionDTO, dup);

      if (existingRelation.isEmpty()) {
        createDuplicateRelation(decisionDTO, dup, duplicateRelationStatus);
      } else if (shouldUpdateRelationStatus(duplicateRelationStatus, existingRelation.get())) {
        duplicateRelationService.setStatus(existingRelation.get(), duplicateRelationStatus);
      }
    }
  }

  private void createDuplicateRelation(
      DecisionDTO decisionDTO,
      DocumentationUnitIdDuplicateCheckDTO dup,
      DuplicateRelationStatus status) {
    var identifiedDuplicate = documentationUnitRepository.findById(dup.getId());
    var currentDocUnit = documentationUnitRepository.findById(decisionDTO.getId());

    if (identifiedDuplicate.isPresent()
        && currentDocUnit.isPresent()
        && currentDocUnit.get() instanceof DecisionDTO currentDecision
        && identifiedDuplicate.get() instanceof DecisionDTO duplicateDecision) {
      duplicateRelationService.create(currentDecision, duplicateDecision, status);
    }
  }

  /**
   * If the duplicate relationship was already ignored in the jDV (either "Dupcode ausschalten" or
   * by setting the doc unit state), the status must be set to IGNORED in NeuRIS as well.
   */
  private DuplicateRelationStatus getRelationStatus(
      DecisionDTO decisionDTO, DocumentationUnitIdDuplicateCheckDTO dup) {

    var isJDVDuplicateCheckActive =
        !Boolean.FALSE.equals(dup.getIsJdvDuplicateCheckActive())
            && !Boolean.FALSE.equals(decisionDTO.getIsJdvDuplicateCheckActive());

    var isIgnored =
        isDuplicatedOrLocked(decisionDTO)
            || isDuplicatedOrLocked(dup)
            || !isJDVDuplicateCheckActive;

    return isIgnored ? DuplicateRelationStatus.IGNORED : DuplicateRelationStatus.PENDING;
  }

  private boolean isDuplicatedOrLocked(DocumentationUnitIdDuplicateCheckDTO dup) {
    return Optional.ofNullable(dup.getStatus()).map(this::isDuplicatedOrLocked).orElse(false);
  }

  private boolean isDuplicatedOrLocked(DecisionDTO dup) {
    return Optional.ofNullable(dup.getStatus())
        .map(StatusDTO::getPublicationStatus)
        .map(this::isDuplicatedOrLocked)
        .orElse(false);
  }

  private boolean isDuplicatedOrLocked(PublicationStatus status) {
    return status == PublicationStatus.DUPLICATED || status == PublicationStatus.LOCKED;
  }

  /**
   * If the relationship is currently PENDING, but should be IGNORED according to jDV it must be set
   * to IGNORED.
   */
  private boolean shouldUpdateRelationStatus(
      DuplicateRelationStatus duplicateRelationStatus, DuplicateRelationDTO existingRelation) {
    return duplicateRelationStatus == DuplicateRelationStatus.IGNORED
        && DuplicateRelationStatus.PENDING.equals(existingRelation.getRelationStatus());
  }

  private void removeObsoleteDuplicates(
      DecisionDTO decisionDTO, List<DocumentationUnitIdDuplicateCheckDTO> duplicates) {
    var existingDuplicates = duplicateRelationService.findAllByDocUnitId(decisionDTO.getId());
    for (var existingDuplicate : existingDuplicates) {
      var isDuplicate =
          duplicates.stream()
              .map(
                  dup ->
                      new DuplicateRelationDTO.DuplicateRelationId(
                          decisionDTO.getId(), dup.getId()))
              .anyMatch(dupId -> dupId.equals(existingDuplicate.getId()));

      if (!isDuplicate) {
        duplicateRelationService.delete(existingDuplicate);
      }
    }
  }
}
