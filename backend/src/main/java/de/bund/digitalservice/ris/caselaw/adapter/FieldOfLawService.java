package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.FieldOfLawRepository;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.Norm;
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
  private static final int MAX_TREE_DEPTH = 7;

  private final FieldOfLawRepository repository;

  public FieldOfLawService(FieldOfLawRepository repository) {
    this.repository = repository;
  }

  public Mono<Page<FieldOfLaw>> getFieldsOfLawBySearchQuery(
      Optional<String> optionalSearchStr, Pageable pageable) {
    if (optionalSearchStr.isEmpty() || optionalSearchStr.get().isBlank()) {
      return repository
          .findAllByOrderByIdentifierAsc(pageable)
          .collectList()
          .zipWith(repository.count())
          .map(t -> new PageImpl<>(t.getT1(), pageable, t.getT2()));
    }
    return searchAndOrderByScore(optionalSearchStr.get().trim(), pageable);
  }

  private String[] splitSearchTerms(String searchStr) {
    return Arrays.stream(searchStr.split("\\s+")).map(String::trim).toArray(String[]::new);
  }

  public Mono<Page<FieldOfLaw>> searchAndOrderByScore(String searchStr, Pageable pageable) {
    Matcher matcher = NORMS_PATTERN.matcher(searchStr);
    AtomicInteger totalElements = new AtomicInteger();
    String[] searchTerms;
    String normStr;

    Mono<List<FieldOfLaw>> unorderedList;
    if (matcher.find()) {
      normStr = matcher.group(1).trim();
      String afterNormSearchStr = matcher.group(2).trim();
      if (afterNormSearchStr.isEmpty()) {
        searchTerms = null;
        unorderedList = repository.findByNormStr(normStr).collectList();
      } else {
        searchTerms = splitSearchTerms(afterNormSearchStr);
        unorderedList = repository.findByNormStrAndSearchTerms(normStr, searchTerms).collectList();
      }
    } else {
      normStr = null;
      searchTerms = splitSearchTerms(searchStr);
      unorderedList = repository.findBySearchTerms(searchTerms).collectList();
    }

    return unorderedList
        .flatMap(
            list -> {
              totalElements.set(list.size());
              Map<FieldOfLaw, Integer> scores = new HashMap<>();
              for (FieldOfLaw fieldOfLaw : list) {
                int score = 0;
                if (searchTerms != null) {
                  for (String searchTerm : searchTerms) {
                    score += getScoreContributionFromSearchTerm(fieldOfLaw, searchTerm);
                  }
                }
                if (normStr != null) {
                  score += getScoreContributionFromNormStr(fieldOfLaw, normStr);
                }
                scores.put(fieldOfLaw, score);
              }
              list.sort((f1, f2) -> scores.get(f2).compareTo(scores.get(f1)));
              int fromIdx = (int) pageable.getOffset();
              int toIdx =
                  (int) Math.min(pageable.getOffset() + pageable.getPageSize(), list.size());
              if (fromIdx > toIdx) {
                list.clear();
                return Mono.just(list);
              }
              return Mono.just(list.subList(fromIdx, toIdx));
            })
        .map(list -> new PageImpl<>(list, pageable, totalElements.get()));
  }

  private int getScoreContributionFromSearchTerm(FieldOfLaw fieldOfLaw, String searchTerm) {
    int score = 0;
    searchTerm = searchTerm.toLowerCase();
    String identifier = fieldOfLaw.identifier().toLowerCase();
    String text = fieldOfLaw.text() == null ? "" : fieldOfLaw.text().toLowerCase();

    if (identifier.equals(searchTerm)) score += 8;
    if (identifier.startsWith(searchTerm)) {
      score += 5;
      int treeDepthOfSearchTerm = searchTerm.split("-").length;
      int treeDepthOfIdentifier = identifier.split("-").length;
      int depthDiff = treeDepthOfIdentifier - treeDepthOfSearchTerm;
      score += MAX_TREE_DEPTH - depthDiff;
    }

    if (identifier.contains(searchTerm)) score += 2;

    if (text.startsWith(searchTerm)) score += 5;
    // split by whitespace and hyphen to get words
    for (String textPart : text.split("[\\s-]+")) {
      if (textPart.equals(searchTerm)) score += 4;
      if (textPart.startsWith(searchTerm)) score += 3;
      if (textPart.contains(searchTerm)) score += 1;
    }
    return score;
  }

  private int getScoreContributionFromNormStr(FieldOfLaw fieldOfLaw, String normStr) {
    int score = 0;
    normStr = normStr.toLowerCase();
    for (Norm norm : fieldOfLaw.norms()) {
      String abbreviation = norm.abbreviation().toLowerCase();
      String normText = abbreviation;
      if (norm.singleNormDescription() != null) {
        normText += " " + norm.singleNormDescription().toLowerCase();
      }
      if (abbreviation.equals(normStr)) score += 8;
      if (abbreviation.startsWith(normStr)) score += 5;
      if (normText.contains(normStr)) score += 5;
    }
    return score;
  }

  public Flux<FieldOfLaw> getChildrenOfFieldOfLaw(String identifier) {
    if (identifier.equalsIgnoreCase(ROOT_ID)) {
      return repository.getTopLevelNodes();
    }

    return repository.findAllByParentIdentifierOrderByIdentifierAsc(identifier);
  }

  public Mono<FieldOfLaw> getTreeForFieldOfLaw(String identifier) {
    return repository.findByIdentifier(identifier).flatMap(this::findParent);
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
