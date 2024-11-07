package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.FieldOfLawRepository;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.Norm;
import java.util.Arrays;
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
      Optional<String> optionalSearchStr,
      Optional<String> identifier,
      Optional<String> norm,
      Pageable pageable) {

    if (identifier.isPresent() && identifier.get().isBlank()) {
      identifier = Optional.empty();
    }
    if (optionalSearchStr.isPresent() && optionalSearchStr.get().isBlank()) {
      optionalSearchStr = Optional.empty();
    }
    if (norm.isPresent() && norm.get().isBlank()) {
      norm = Optional.empty();
    }

    return searchAndOrderByScore(optionalSearchStr, identifier, norm, pageable);
  }

  private String[] splitSearchTerms(String searchStr) {
    return Arrays.stream(searchStr.split("\\s+")).map(String::trim).toArray(String[]::new);
  }

  Slice<FieldOfLaw> searchAndOrderByScore(
      Optional<String> searchStr,
      Optional<String> identifier,
      Optional<String> norm,
      Pageable pageable) {
    // Parse search terms if present
    String[] searchTerms = searchStr.map(this::splitSearchTerms).orElse(null);

    // Parse norm string if present, and format it
    String normStr = norm.map(n -> n.trim().replaceAll("§(\\d+)", "§ $1")).orElse("");

    // Initialize an unordered list to hold search results
    List<FieldOfLaw> unorderedList = List.of();

    // Query based on combinations of identifier, searchStr, and norm
    if (identifier.isPresent()) {
      String identifierStr = identifier.get().trim();

      // Handle all combinations when identifier is present
      if (searchTerms != null) {
        unorderedList =
            norm.isPresent()
                ? repository.findByIdentifierAndSearchTermsAndNormStr(
                    identifierStr, searchTerms, normStr)
                : repository.findByIdentifierAndSearchTerms(identifierStr, searchTerms);
      } else {
        unorderedList =
            norm.isPresent()
                ? repository.findByIdentifierAndNormStr(identifierStr, normStr)
                : repository.findByIdentifier(identifierStr, pageable);
      }
    } else {
      // Handle all combinations when identifier is absent
      if (searchTerms != null) {
        unorderedList =
            norm.isPresent()
                ? repository.findByNormStrAndSearchTerms(normStr, searchTerms)
                : repository.findBySearchTerms(searchTerms);
      } else if (norm.isPresent()) {
        unorderedList = repository.findByNormStr(normStr);
      }
    }

    // If no results found, return an empty page
    if (unorderedList.isEmpty()) {
      return new PageImpl<>(List.of(), pageable, 0);
    }

    // Calculate scores and sort the list based on the score and identifier
    Map<FieldOfLaw, Integer> scores =
        calculateScore(identifier, searchTerms, normStr, unorderedList);
    List<FieldOfLaw> orderedList =
        unorderedList.stream()
            .sorted(
                (f1, f2) -> {
                  int compare = scores.get(f2).compareTo(scores.get(f1));
                  return compare != 0 ? compare : f1.identifier().compareTo(f2.identifier());
                })
            .toList();

    // Extract the correct sublist for pagination
    int fromIdx = (int) pageable.getOffset();
    int toIdx = (int) Math.min(pageable.getOffset() + pageable.getPageSize(), orderedList.size());
    List<FieldOfLaw> pageContent = orderedList.subList(Math.max(0, fromIdx), toIdx);

    // Return the paginated results
    return new PageImpl<>(pageContent, pageable, orderedList.size());
  }

  private Map<FieldOfLaw, Integer> calculateScore(
      Optional<String> identifier,
      String[] searchTerms,
      String normStr,
      List<FieldOfLaw> fieldOfLaws) {
    Map<FieldOfLaw, Integer> scores = new HashMap<>();

    if (fieldOfLaws == null || fieldOfLaws.isEmpty()) {
      return scores;
    }

    fieldOfLaws.forEach(
        fieldOfLaw -> {
          int score = 0;

          if (identifier.isPresent()) {
            score += getScoreContributionFromIdentifier(fieldOfLaw, identifier);
          }

          if (searchTerms != null) {
            for (String searchTerm : searchTerms) {
              score += getScoreContributionFromSearchTerm(fieldOfLaw, searchTerm);
            }
          }

          if (normStr != null) {
            score += getScoreContributionFromNormStr(fieldOfLaw, normStr);
          }

          scores.put(fieldOfLaw, score);
        });

    return scores;
  }

  private int getScoreContributionFromIdentifier(
      FieldOfLaw fieldOfLaw, Optional<String> identifier) {
    int score = 0;
    if (identifier.isPresent()) {
      String searchStr = identifier.get().trim().toLowerCase();

      String identifierStr =
          fieldOfLaw.identifier() == null ? "" : fieldOfLaw.identifier().toLowerCase();

      if (identifierStr.equals(searchStr)) score += 8;
      if (identifierStr.startsWith(searchStr)) score += 5;
      if (identifierStr.contains(searchStr)) score += 2;
    }
    return score;
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
