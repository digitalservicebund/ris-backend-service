package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDuplicateCheckRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitIdDuplicateCheckDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DuplicateRelationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DuplicateRelationRepository;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateRelationStatus;
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
  private final DocumentationUnitService documentationUnitService;

  private final DatabaseDuplicateCheckRepository repository;
  private final DuplicateRelationRepository relationRepository;

  public DuplicateCheckService(
      DatabaseDuplicateCheckRepository duplicateCheckRepository,
      DocumentationUnitService documentationUnitService,
      DuplicateRelationRepository relationRepository) {
    this.repository = duplicateCheckRepository;
    this.documentationUnitService = documentationUnitService;
    this.relationRepository = relationRepository;
  }

  List<DocumentationUnitIdDuplicateCheckDTO> getDuplicates(String docNumber) {
    try {
      var documentationUnit = documentationUnitService.getByDocumentNumber(docNumber);

      var fileNumbers = documentationUnit.coreData().fileNumbers();
      var deviatingFileNumbers = documentationUnit.coreData().deviatingFileNumbers();
      List<String> allFileNumbers = new ArrayList<>();
      if (fileNumbers != null) {
        allFileNumbers.addAll(fileNumbers.stream().map(String::toUpperCase).toList());
      }
      if (deviatingFileNumbers != null) {
        allFileNumbers.addAll(deviatingFileNumbers.stream().map(String::toUpperCase).toList());
      }

      if (allFileNumbers.isEmpty()) {
        return List.of();
      }

      var decisionDate = documentationUnit.coreData().decisionDate();
      var deviatingDecisionDates = documentationUnit.coreData().deviatingDecisionDates();
      List<LocalDate> allDates = new ArrayList<>();
      if (decisionDate != null) allDates.add(decisionDate);
      if (!deviatingDecisionDates.isEmpty()) allDates.addAll(deviatingDecisionDates);

      List<UUID> allCourtIds = new ArrayList<>();
      var court = documentationUnit.coreData().court();
      if (court != null) allCourtIds.add(court.id());

      List<String> allDeviatingCourts = new ArrayList<>();
      var deviatingCourts = documentationUnit.coreData().deviatingCourts();
      if (deviatingCourts != null)
        allDeviatingCourts.addAll(deviatingCourts.stream().map(String::toUpperCase).toList());

      var ecli = documentationUnit.coreData().ecli();
      var deviatingEclis = documentationUnit.coreData().deviatingEclis();
      List<String> allEclis = new ArrayList<>();
      if (ecli != null) allEclis.add(ecli.toUpperCase());
      if (deviatingEclis != null)
        allEclis.addAll(deviatingEclis.stream().map(String::toUpperCase).toList());

      List<UUID> allDocTypeIds = new ArrayList<>();
      var documentationType = documentationUnit.coreData().documentType();
      if (documentationType != null) allDocTypeIds.add(documentationType.uuid());

      var duplicates =
          repository.findDuplicates(
              allFileNumbers, allDates, allCourtIds, allDeviatingCourts, allEclis, allDocTypeIds);

      for (var dup : duplicates) {
        if (dup.getId().equals(documentationUnit.uuid())) {
          // TODO: filter dup list in the beginning
          continue;
        }
        DuplicateRelationDTO.DuplicateRelationId duplicateRelationId =
            new DuplicateRelationDTO.DuplicateRelationId(documentationUnit.uuid(), dup.getId());
        Optional<DuplicateRelationDTO> existingRelation =
            relationRepository.findById(duplicateRelationId);
        var status =
            Boolean.FALSE.equals(dup.getIsJdvDuplicateCheckActive())
                ? DuplicateRelationStatus.IGNORED
                : DuplicateRelationStatus.PENDING;
        if (existingRelation.isEmpty()) {
          var newRelation =
              DuplicateRelationDTO.builder().status(status).id(duplicateRelationId).build();
          relationRepository.save(newRelation);
        } else if (Boolean.FALSE.equals(
            dup.getIsJdvDuplicateCheckActive()
                && DuplicateRelationStatus.PENDING.equals(existingRelation.get().getStatus()))) {
          existingRelation.get().setStatus(status);
          relationRepository.save(existingRelation.get());
        }
      }

      var existingDuplicates = relationRepository.findAllByDocUnitId(documentationUnit.uuid());
      for (var existingDuplicate : existingDuplicates) {
        var isDuplicate =
            duplicates.stream()
                .map(
                    dup ->
                        new DuplicateRelationDTO.DuplicateRelationId(
                            documentationUnit.uuid(), dup.getId()))
                .anyMatch(dup -> dup.equals(existingDuplicate.getId()));
        if (!isDuplicate) {
          relationRepository.delete(existingDuplicate);
        }
      }

      return duplicates;
    } catch (Exception e) {
      log.debug(e.getMessage());
      return List.of();
    }
  }
}
