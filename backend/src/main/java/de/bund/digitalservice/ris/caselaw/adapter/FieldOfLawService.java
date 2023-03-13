package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.SubjectFieldRepository;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.FieldOfLaw;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
  private static final Pattern NORMS_PATTERN = Pattern.compile("norm\\s?:\\s?\"([^\"]*)\"(.*)");

  private final SubjectFieldRepository repository;

  public FieldOfLawService(SubjectFieldRepository repository) {
    this.repository = repository;
  }

  public Mono<Page<FieldOfLaw>> getFieldsOfLawBySearchQuery(
      Optional<String> optionalSearchStr, Pageable pageable) {
    if (optionalSearchStr.isEmpty() || optionalSearchStr.get().isBlank()) {
      return repository
          .findAllByOrderBySubjectFieldNumberAsc(pageable)
          .collectList()
          .zipWith(repository.count())
          .map(t -> new PageImpl<>(t.getT1(), pageable, t.getT2()));
    }

    String searchStr = optionalSearchStr.get().trim();

    if (searchStr.startsWith("1")) {
      searchStr = searchStr.substring(1).trim();
    }
    if (searchStr.startsWith("2")) {
      return approachScores(searchStr.substring(1).trim(), pageable);
    }

    return approachSQLPrioClasses(searchStr, pageable);
  }

  public Mono<Page<FieldOfLaw>> approachSQLPrioClasses(String searchStr, Pageable pageable) {
    Matcher matcher = NORMS_PATTERN.matcher(searchStr);

    if (matcher.find()) {
      String normsStr = matcher.group(1).trim();
      String afterNormsSearchStr = matcher.group(2).trim();

      if (afterNormsSearchStr.isEmpty()) {
        return repository
            .findByNormsStr(normsStr, pageable)
            .collectList()
            .zipWith(repository.countByNormsStr(normsStr))
            .map(t -> new PageImpl<>(t.getT1(), pageable, t.getT2()));
      }

      return repository
          .findByNormsAndSearchStr(normsStr, afterNormsSearchStr, pageable)
          .collectList()
          .zipWith(repository.countByNormsAndSearchStr(normsStr, afterNormsSearchStr))
          .map(t -> new PageImpl<>(t.getT1(), pageable, t.getT2()));
    }

    return repository
        .findBySearchStr(searchStr, pageable)
        .collectList()
        .zipWith(repository.countBySearchStr(searchStr))
        .map(t -> new PageImpl<>(t.getT1(), pageable, t.getT2()));
  }

  public Mono<Page<FieldOfLaw>> approachScores(String searchStr, Pageable pageable) {
    String[] searchTerms =
        Arrays.stream(searchStr.split("\\s+")).map(String::trim).toArray(String[]::new);

    // TODO limit/offset

    AtomicInteger totalElements = new AtomicInteger();

    return repository
        .findAllBySearchTerms(searchTerms)
        .collectList()
        .flatMap(
            list -> {
              totalElements.set(list.size());
              Map<FieldOfLaw, Integer> scores = new HashMap<>();
              for (FieldOfLaw fieldOfLaw : list) {
                int score = 0;
                for (String searchTerm : searchTerms) {
                  score += getScoreContributionFromSearchTerm(fieldOfLaw, searchTerm);
                }
                scores.put(fieldOfLaw, score);
              }
              list.sort((f1, f2) -> scores.get(f2).compareTo(scores.get(f1)));
              return Mono.just(list);
            })
        .zipWith(Mono.just(totalElements.get()))
        .map(t -> new PageImpl<>(t.getT1(), pageable, t.getT2()));
  }

  private int getScoreContributionFromSearchTerm(FieldOfLaw fieldOfLaw, String searchTerm) {
    int score = 0;
    String term = searchTerm.toLowerCase();
    String identifier = fieldOfLaw.identifier().toLowerCase();
    String text = fieldOfLaw.text().toLowerCase();

    if (identifier.equals(term)) score += 8;
    if (identifier.startsWith(term)) score += 5;
    if (identifier.contains(term)) score += 2;

    if (text.startsWith(term)) score += 5;
    // split by whitespace and hyphen to get words
    for (String textPart : text.split("[\\s-]+")) {
      if (textPart.equals(term)) score += 4;
      if (textPart.startsWith(term)) score += 3;
      if (textPart.contains(term)) score += 1;
    }
    return score;
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

  public Mono<List<FieldOfLaw>> getFieldsOfLawForDocumentUnit(UUID documentUnitUuid) {
    return repository.findAllForDocumentUnit(documentUnitUuid);
  }

  public Mono<List<FieldOfLaw>> addFieldOfLawToDocumentUnit(
      UUID documentUnitUuid, String identifier) {
    return repository.addFieldOfLawToDocumentUnit(documentUnitUuid, identifier);
  }

  public Mono<List<FieldOfLaw>> removeFieldOfLawToDocumentUnit(
      UUID documentUnitUuid, String identifier) {
    return repository.removeFieldOfLawToDocumentUnit(documentUnitUuid, identifier);
  }
}
