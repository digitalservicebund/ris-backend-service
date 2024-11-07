package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.FieldOfLawRepository;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.Norm;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FieldOfLawService {
  private static final String ROOT_ID = "root";
  private static final Pattern NORMS_PATTERN = Pattern.compile("norm\\s?:\\s?\"([^\"]*)\"(.*)");

  private final FieldOfLawRepository repository;

  public FieldOfLawService(FieldOfLawRepository repository) {
    this.repository = repository;
  }

  public Slice<FieldOfLaw> getFieldsOfLawBySearchQuery(
      Optional<String> description,
      Optional<String> identifier,
      Optional<String> norm,
      Pageable pageable) {

    if (identifier.isPresent() && identifier.get().isBlank()) {
      identifier = Optional.empty();
    }
    if (description.isPresent() && description.get().isBlank()) {
      description = Optional.empty();
    }
    if (norm.isPresent() && norm.get().isBlank()) {
      norm = Optional.empty();
    }

    return searchAndOrderByScore(description, identifier, norm, pageable);
  }

  private String[] splitSearchTerms(String searchStr) {
    return Arrays.stream(searchStr.split("\\s+")).map(String::trim).toArray(String[]::new);
  }

  Slice<FieldOfLaw> searchAndOrderByScore(
      Optional<String> description,
      Optional<String> identifier,
      Optional<String> norm,
      Pageable pageable) {
    Optional<String[]> searchTerms = description.map(this::splitSearchTerms);
    Optional<String> normStr = norm.map(n -> n.trim().replaceAll("§(\\d+)", "§ $1"));

    List<FieldOfLaw> unorderedList;

    if (identifier.isPresent()) {
      String identifierStr = identifier.get().trim();

      if (searchTerms.isEmpty() && normStr.isEmpty()) {
        // Returned list is already ordered by identifier, so scoring is not necessary here
        unorderedList = repository.findByIdentifier(identifierStr, pageable);
        return sliceResults(unorderedList, pageable);
      } else {
        unorderedList = getResultsWithIdentifier(identifierStr, searchTerms, normStr);
      }
    } else {
      unorderedList = getResultsWithoutIdentifier(searchTerms, normStr);
    }

    // If no results found, return an empty page
    if (unorderedList.isEmpty()) {
      return new PageImpl<>(List.of(), pageable, 0);
    }

    List<FieldOfLaw> orderedList = orderResults(searchTerms, normStr, unorderedList);

    return sliceResults(orderedList, pageable);
  }

  private List<FieldOfLaw> getResultsWithIdentifier(
      String identifierStr, Optional<String[]> searchTerms, Optional<String> norm) {
    if (searchTerms.isPresent() && norm.isPresent()) {
      return repository.findByIdentifierAndSearchTermsAndNorm(
          identifierStr, searchTerms.get(), norm.get());
    }
    if (searchTerms.isPresent()) {
      return repository.findByIdentifierAndSearchTerms(identifierStr, searchTerms.get());
    }
    if (norm.isPresent()) {
      return repository.findByIdentifierAndNorm(identifierStr, norm.get());
    }
    return Collections.emptyList();
  }

  private List<FieldOfLaw> getResultsWithoutIdentifier(
      Optional<String[]> searchTerms, Optional<String> norm) {
    if (searchTerms.isPresent() && norm.isPresent()) {
      return repository.findByNormAndSearchTerms(norm.get(), searchTerms.get());
    }
    if (searchTerms.isPresent()) {
      return repository.findBySearchTerms(searchTerms.get());
    }
    if (norm.isPresent()) {
      return repository.findByNorm(norm.get());
    }
    return Collections.emptyList();
  }

  private List<FieldOfLaw> orderResults(
      Optional<String[]> searchTerms, Optional<String> normStr, List<FieldOfLaw> unorderedList) {
    // Calculate scores and sort the list based on the score and identifier
    Map<FieldOfLaw, Integer> scores = calculateScore(searchTerms, normStr, unorderedList);
    return unorderedList.stream()
        .sorted(
            (f1, f2) -> {
              int compare = scores.get(f2).compareTo(scores.get(f1));
              return compare != 0 ? compare : f1.identifier().compareTo(f2.identifier());
            })
        .toList();
  }

  private Slice<FieldOfLaw> sliceResults(List<FieldOfLaw> orderedList, Pageable pageable) {
    // Extract the correct sublist for pagination
    int fromIdx = (int) pageable.getOffset();
    int toIdx = (int) Math.min(pageable.getOffset() + pageable.getPageSize(), orderedList.size());
    List<FieldOfLaw> pageContent = orderedList.subList(Math.max(0, fromIdx), toIdx);
    return new PageImpl<>(pageContent, pageable, orderedList.size());
  }

  private Map<FieldOfLaw, Integer> calculateScore(
      Optional<String[]> searchTerms, Optional<String> normStr, List<FieldOfLaw> fieldOfLaws) {
    Map<FieldOfLaw, Integer> scores = new HashMap<>();

    if (fieldOfLaws == null || fieldOfLaws.isEmpty()) {
      return scores;
    }

    fieldOfLaws.forEach(
        fieldOfLaw -> {
          int score = 0;

          if (searchTerms.isPresent()) {
            for (String searchTerm : searchTerms.get()) {
              score += getScoreContributionFromSearchTerm(fieldOfLaw, searchTerm);
            }
          }

          if (normStr.isPresent()) {
            score += getScoreContributionFromNormStr(fieldOfLaw, normStr.get());
          }

          scores.put(fieldOfLaw, score);
        });

    return scores;
  }

  private int getScoreContributionFromSearchTerm(FieldOfLaw fieldOfLaw, String searchTerm) {
    int score = 0;
    searchTerm = searchTerm.toLowerCase();
    String text = fieldOfLaw.text() == null ? "" : fieldOfLaw.text().toLowerCase();

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
    if (normStr.isBlank()) {
      return score;
    }
    for (Norm norm : fieldOfLaw.norms()) {
      String abbreviation = norm.abbreviation() == null ? "" : norm.abbreviation().toLowerCase();
      String description =
          norm.singleNormDescription() == null ? "" : norm.singleNormDescription().toLowerCase();
      String normText = description + " " + abbreviation;
      if (description.equals(normStr)) score += 8;
      if (description.startsWith(normStr)) score += 5;
      if (normText.contains(normStr)) score += 5;
    }
    return score;
  }

  public List<FieldOfLaw> getChildrenOfFieldOfLaw(String identifier) {
    if (identifier.equalsIgnoreCase(ROOT_ID)) {
      return repository.getTopLevelNodes();
    }

    return repository.findAllByParentIdentifierOrderByIdentifierAsc(identifier);
  }

  public FieldOfLaw getTreeForFieldOfLaw(String identifier) {
    return repository.findTreeByIdentifier(identifier);
  }
}
