package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.FieldOfLawRepository;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.Norm;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
      Optional<String> optionalSearchStr, Pageable pageable) {

    if (optionalSearchStr.isEmpty() || optionalSearchStr.get().isBlank()) {
      return repository.findAllByOrderByIdentifierAsc(pageable);
    }

    return searchAndOrderByScore(optionalSearchStr.get().trim(), pageable);
  }

  private String[] splitSearchTerms(String searchStr) {
    return Arrays.stream(searchStr.split("\\s+")).map(String::trim).toArray(String[]::new);
  }

  Slice<FieldOfLaw> searchAndOrderByScore(String searchStr, Pageable pageable) {
    Matcher matcher = NORMS_PATTERN.matcher(searchStr);
    String[] searchTerms;
    String normStr;

    List<FieldOfLaw> unorderedList;
    if (matcher.find()) {
      normStr = matcher.group(1).trim().replaceAll("§(\\d+)", "§ $1");
      String afterNormSearchStr = matcher.group(2).trim();
      if (afterNormSearchStr.isEmpty()) {
        searchTerms = null;
        unorderedList = repository.findByNormStr(normStr);
      } else {
        searchTerms = splitSearchTerms(afterNormSearchStr);
        unorderedList = repository.findByNormStrAndSearchTerms(normStr, searchTerms);
      }
    } else {
      normStr = null;
      searchTerms = splitSearchTerms(searchStr);
      unorderedList = repository.findBySearchTerms(searchTerms);
    }

    if (unorderedList == null || unorderedList.isEmpty()) {
      return new PageImpl<>(List.of(), pageable, 0);
    }

    Map<FieldOfLaw, Integer> scores = calculateScore(searchTerms, normStr, unorderedList);

    List<FieldOfLaw> orderedList =
        unorderedList.stream()
            .sorted(
                (f1, f2) -> {
                  int compare = scores.get(f2).compareTo(scores.get(f1));

                  if (compare == 0) {
                    compare = f1.identifier().compareTo(f2.identifier());
                  }

                  return compare;
                })
            .toList();

    int fromIdx = (int) pageable.getOffset();
    int toIdx = (int) Math.min(pageable.getOffset() + pageable.getPageSize(), orderedList.size());

    List<FieldOfLaw> pageContent = new ArrayList<>();
    if (fromIdx < toIdx) {
      pageContent = orderedList.subList(fromIdx, toIdx);
    }

    int totalElements = orderedList.size();

    return new PageImpl<>(pageContent, pageable, totalElements);
  }

  private Map<FieldOfLaw, Integer> calculateScore(
      String[] searchTerms, String normStr, List<FieldOfLaw> fieldOfLaws) {
    Map<FieldOfLaw, Integer> scores = new HashMap<>();

    if (fieldOfLaws == null || fieldOfLaws.isEmpty()) {
      return scores;
    }

    fieldOfLaws.forEach(
        fieldOfLaw -> {
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
        });

    return scores;
  }

  private int getScoreContributionFromSearchTerm(FieldOfLaw fieldOfLaw, String searchTerm) {
    int score = 0;
    searchTerm = searchTerm.toLowerCase();
    String identifier =
        fieldOfLaw.identifier() == null ? "" : fieldOfLaw.identifier().toLowerCase();
    String text = fieldOfLaw.text() == null ? "" : fieldOfLaw.text().toLowerCase();

    if (identifier.equals(searchTerm)) score += 8;
    if (identifier.startsWith(searchTerm)) score += 5;
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

  public List<FieldOfLaw> getFieldsOfLawByIdentifierSearch(Optional<String> optionalSearchStr) {
    if (optionalSearchStr.isEmpty() || optionalSearchStr.get().isBlank()) {
      return repository.findAllByOrderByIdentifierAsc(PageRequest.of(0, 30)).stream().toList();
    }
    return repository.findByIdentifierSearch(optionalSearchStr.get().trim());
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
