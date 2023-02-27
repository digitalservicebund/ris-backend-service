package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.SubjectFieldRepository;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.FieldOfLaw;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class FieldOfLawService {
  private static final String ROOT_ID = "root";

  private final SubjectFieldRepository repository;

  public FieldOfLawService(SubjectFieldRepository repository) {
    this.repository = repository;
  }

  public Flux<FieldOfLaw> getFieldsOfLawBySearchQuery(Optional<String> searchStr) {
    if (searchStr.isEmpty() || searchStr.get().isBlank()) {
      return repository.findAllByOrderBySubjectFieldNumberAsc();
    }

    return repository.findBySearchStr(searchStr.get().trim());
  }

  public Flux<FieldOfLaw> getChildrenOfFieldOfLaw(String subjectFieldNumber) {
    if (subjectFieldNumber.equalsIgnoreCase(ROOT_ID)) {
      return repository.getTopLevelNodes();
    }

    return repository.findAllByParentSubjectFieldNumberOrderBySubjectFieldNumberAsc(
        subjectFieldNumber);
  }

  public Mono<FieldOfLaw> getTreeForFieldOfLaw(String subjectFieldId) {
    return repository.findBySubjectFieldNumber(subjectFieldId).flatMap(this::findParent);
  }

  private Mono<FieldOfLaw> findParent(FieldOfLaw child) {

    return repository
        .findParentByChild(child)
        .flatMap(
            parent -> {
              if (child.identifier().equals(parent.identifier())) {
                return Mono.just(child);
              }

              parent.children().add(child);

              return findParent(parent);
            });
  }

  public Flux<FieldOfLaw> getFieldsOfLawForDocumentUnit(UUID documentUnitUuid) {
    return repository.findAllForDocumentUnit(documentUnitUuid);
  }

  public Flux<FieldOfLaw> addFieldOfLawToDocumentUnit(UUID documentUnitUuid, String identifier) {
    return repository.addFieldOfLawToDocumentUnit(documentUnitUuid, identifier);
  }

  public Flux<FieldOfLaw> removeFieldOfLawToDocumentUnit(UUID documentUnitUuid, String identifier) {
    return repository.removeFieldOfLawToDocumentUnit(documentUnitUuid, identifier);
  }
}
