package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitIdDuplicateCheckDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DuplicateRelationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DuplicateRelationRepository;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateCheckRepository;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateRelationStatus;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DuplicateCheckService {
  private final DocumentationUnitService documentationUnitService;

  private final DuplicateCheckRepository repository;
  private final DuplicateRelationRepository relationRepository;

  public DuplicateCheckService(
      DuplicateCheckRepository duplicateCheckRepository,
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

      var documentType = documentationUnit.coreData().documentType();
      var duplicates =
          repository.findDuplicates(
              allFileNumbers,
              allDates,
              allCourtIds,
              allDeviatingCourts,
              allEclis,
              documentType.uuid());

      for (var dup : duplicates) {
        if (dup.getId().equals(documentationUnit.uuid())) {
          continue;
        }
        DuplicateRelationDTO.DuplicateRelationId duplicateRelationId =
            new DuplicateRelationDTO.DuplicateRelationId(documentationUnit.uuid(), dup.getId());
        var existingRelation = relationRepository.findById(duplicateRelationId);
        var status =
            Boolean.FALSE.equals(dup.getIsJdvDuplicateCheckActive())
                ? DuplicateRelationStatus.IGNORED
                : DuplicateRelationStatus.PENDING;
        if (existingRelation.isEmpty()) {
          var newRelation =
              DuplicateRelationDTO.builder().status(status).id(duplicateRelationId).build();
          relationRepository.save(newRelation);
        } else {
          existingRelation.get().setStatus(status);
          relationRepository.save(existingRelation.get());
        }
      }
      return duplicates;
    } catch (Exception e) {
      log.debug(e.getMessage());
      return List.of();
    }
  }
}
