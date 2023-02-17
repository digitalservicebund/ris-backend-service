package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.SubjectFieldRepository;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.SubjectField;
import java.util.Optional;
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

  public Flux<SubjectField> getFieldsOfLawBySearchQuery(Optional<String> searchStr) {
    if (searchStr.isEmpty() || searchStr.get().isBlank()) {
      return repository.findAllByOrderBySubjectFieldNumberAsc();
    }

    return repository.findBySearchStr(searchStr.get().trim());
  }

  public Flux<SubjectField> getChildrenOfFieldOfLaw(String subjectFieldNumber) {
    if (subjectFieldNumber.equalsIgnoreCase(ROOT_ID)) {
      return repository.getTopLevelNodes();
    }

    return repository.findAllByParentSubjectFieldNumberOrderBySubjectFieldNumberAsc(
        subjectFieldNumber);
  }

  public Mono<SubjectField> getTreeForFieldOfLaw(String subjectFieldId) {
    return repository.findBySubjectFieldNumber(subjectFieldId).flatMap(this::findParent);
  }

  private Mono<SubjectField> findParent(SubjectField child) {

    return repository
        .findParentByChild(child)
        .flatMap(
            parent -> {
              if (child.subjectFieldNumber().equals(parent.subjectFieldNumber())) {
                return Mono.just(child);
              }

              parent.children().add(child);

              return findParent(parent);
            });
  }
}
