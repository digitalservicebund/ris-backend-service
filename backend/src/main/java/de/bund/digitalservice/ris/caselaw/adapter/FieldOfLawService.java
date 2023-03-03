package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.SubjectFieldRepository;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.FieldOfLaw;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

  public Mono<Page<FieldOfLaw>> getFieldsOfLawBySearchQuery(
      Optional<String> searchStr, Pageable pageable) {
    if (searchStr.isEmpty() || searchStr.get().isBlank()) {
      return repository
          .findAllByOrderBySubjectFieldNumberAsc(pageable)
          .collectList()
          .zipWith(repository.count())
          .map(t -> new PageImpl<>(t.getT1(), pageable, t.getT2()));
    }

    String str = searchStr.get().trim();
    return repository
        .findBySearchStr(str, pageable)
        .collectList()
        .zipWith(repository.countBySearchStr(str))
        .map(t -> new PageImpl<>(t.getT1(), pageable, t.getT2()));
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
