package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DuplicateRelationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DuplicateRelationRepository;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateRelationStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DuplicateRelationService {

  private final DuplicateRelationRepository relationRepository;

  public DuplicateRelationService(
      DocumentationUnitService documentationUnitService,
      DuplicateRelationRepository relationRepository,
      DatabaseDocumentationUnitRepository documentationUnitRepository) {
    this.relationRepository = relationRepository;
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
    relationRepository.save(newRelation);
  }

  void setStatus(DuplicateRelationDTO duplicateRelation, DuplicateRelationStatus status) {
    duplicateRelation.setStatus(status);
    relationRepository.save(duplicateRelation);
  }

  void delete(DuplicateRelationDTO duplicateRelation) {
    relationRepository.delete(duplicateRelation);
  }
}
