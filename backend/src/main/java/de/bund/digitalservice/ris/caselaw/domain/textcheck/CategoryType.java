package de.bund.digitalservice.ris.caselaw.domain.textcheck;

import com.fasterxml.jackson.annotation.JsonValue;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceeding;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;

@Getter
public enum CategoryType {
  REASONS("reasons", List.of(Decision.class)),
  CASE_FACTS("caseFacts", List.of(Decision.class)),
  DECISION_REASONS("decisionReasons", List.of(Decision.class)),
  HEADNOTE("headnote", List.of(Decision.class)),
  OTHER_HEADNOTE("otherHeadnote", List.of(Decision.class)),
  GUIDING_PRINCIPLE("guidingPrinciple", List.of(Decision.class)),
  TENOR("tenor", List.of(Decision.class)),
  HEADLINE("headline", List.of(Decision.class, PendingProceeding.class)),
  OTHER_LONG_TEXT("otherLongText", List.of(Decision.class)),
  DISSENTING_OPINION("dissentingOpinion", List.of(Decision.class)),
  OUTLINE("outline", List.of(Decision.class)),
  LEGAL_ISSUE("legalIssue", List.of(PendingProceeding.class)),
  RESOLUTION_NOTE("resolutionNote", List.of(PendingProceeding.class));

  private final List<Class<? extends DocumentationUnit>> allowedClasses;
  @JsonValue private final String name;

  CategoryType(String name, List<Class<? extends DocumentationUnit>> allowedClasses) {
    this.allowedClasses = allowedClasses;
    this.name = name;
  }

  public static CategoryType[] forDocumentationUnitType(Class<? extends DocumentationUnit> clazz) {
    return Arrays.stream(values())
        .filter(categoryType -> categoryType.allowedClasses.contains(clazz))
        .toArray(CategoryType[]::new);
  }

  public static CategoryType forName(String category) {
    for (CategoryType type : CategoryType.values()) {
      if (type.name.equals(category)) {
        return type;
      }
    }

    return null;
  }
}
