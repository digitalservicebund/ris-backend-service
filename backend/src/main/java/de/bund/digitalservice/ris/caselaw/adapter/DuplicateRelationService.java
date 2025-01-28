package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DuplicateRelationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DuplicateRelationRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DuplicateRelationViewRepository;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateRelationStatus;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DuplicateRelationService {

  private final DuplicateRelationRepository relationRepository;
  private final DuplicateRelationViewRepository relationViewRepository;

  public DuplicateRelationService(
      DuplicateRelationRepository relationRepository,
      DuplicateRelationViewRepository relationViewRepository) {
    this.relationRepository = relationRepository;
    this.relationViewRepository = relationViewRepository;
  }

  Optional<DuplicateRelationDTO> findByDocUnitIds(UUID docUnitIdA, UUID docUnitIdB) {
    DuplicateRelationDTO.DuplicateRelationId duplicateRelationId =
        new DuplicateRelationDTO.DuplicateRelationId(docUnitIdA, docUnitIdB);

    return relationRepository.findById(duplicateRelationId);
  }

  List<DuplicateRelationDTO> findAllByDocUnitId(UUID docUnitId) {
    return relationRepository.findAllByDocUnitId(docUnitId);
  }

  void create(
      DocumentationUnitDTO docUnitA,
      DocumentationUnitDTO docUnitB,
      DuplicateRelationStatus status) {
    DuplicateRelationDTO.DuplicateRelationId duplicateRelationId =
        new DuplicateRelationDTO.DuplicateRelationId(docUnitA.getId(), docUnitB.getId());

    DocumentationUnitDTO docUnit1;
    DocumentationUnitDTO docUnit2;
    // duplicateRelationId determines the order of the two docUnits
    if (docUnitA.getId().equals(duplicateRelationId.getDocumentationUnitId1())) {
      docUnit1 = docUnitA;
      docUnit2 = docUnitB;
    } else {
      docUnit1 = docUnitB;
      docUnit2 = docUnitA;
    }

    var newRelation =
        DuplicateRelationDTO.builder()
            .status(status)
            .documentationUnit1(docUnit1)
            .documentationUnit2(docUnit2)
            .id(duplicateRelationId)
            .build();
    try {
      relationRepository.save(newRelation);
    } catch (ConstraintViolationException e) {
      // Duplicate relations might be created multiple times in parallel or while a deletion of a
      // doc unit is in progress (e2e tests). Instead of locking we choose to ignore this.
    }
  }

  void setStatus(DuplicateRelationDTO duplicateRelation, DuplicateRelationStatus status) {
    duplicateRelation.setStatus(status);
    relationRepository.save(duplicateRelation);
  }

  void delete(DuplicateRelationDTO duplicateRelation) {
    relationRepository.delete(duplicateRelation);
  }

  void updateAllDuplicates() {
    log.info("Updating all duplicate relations");
    var removedRelations = this.relationViewRepository.removeObsoleteDuplicateRelations();
    var insertedRelations = this.relationViewRepository.addMissingDuplicateRelations();
    var ignoredRelations =
        this.relationViewRepository.ignoreDuplicateRelationsWhenJdvDupCheckDisabled();
    log.info(
        "Updating duplicate relations finished: {} duplicates added, {} duplicates removed, {} duplicates set to ignored.",
        insertedRelations,
        removedRelations,
        ignoredRelations);
  }
}
