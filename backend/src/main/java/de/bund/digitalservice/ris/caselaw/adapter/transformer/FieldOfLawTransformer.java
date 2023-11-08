package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FieldOfLawDTO;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw.FieldOfLawBuilder;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.Keyword;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.Norm;
import java.util.Collections;
import java.util.List;

public class FieldOfLawTransformer {
  private FieldOfLawTransformer() {}

  public static FieldOfLaw transformToDomain(FieldOfLawDTO fieldOfLawDTO) {
    return transformToDomain(fieldOfLawDTO, true);
  }

  public static FieldOfLaw transformToDomain(FieldOfLawDTO fieldOfLawDTO, boolean withChildren) {
    FieldOfLawBuilder builder =
        FieldOfLaw.builder()
            .id(fieldOfLawDTO.getId())
            .identifier(fieldOfLawDTO.getIdentifier())
            .text(fieldOfLawDTO.getText());

    List<Keyword> keywords = null;
    if (fieldOfLawDTO.getKeywords() != null) {
      keywords =
          fieldOfLawDTO.getKeywords().stream()
              .map(keywordDTO -> Keyword.builder().value(keywordDTO.getValue()).build())
              .toList();
      builder.keywords(keywords);
    }

    List<Norm> norms = null;
    if (fieldOfLawDTO.getNorms() != null) {
      norms =
          fieldOfLawDTO.getNorms().stream()
              .map(
                  normDTO ->
                      Norm.builder()
                          .abbreviation(normDTO.getAbbreviation())
                          .singleNormDescription(normDTO.getSingleNormDescription())
                          .build())
              .toList();
      builder.norms(norms);
    }

    List<String> linkedFields = null;
    if (fieldOfLawDTO.getFieldOfLawTextReferences() != null) {
      linkedFields =
          fieldOfLawDTO.getFieldOfLawTextReferences().stream()
              .map(FieldOfLawDTO::getIdentifier)
              .toList();
      builder.linkedFields(linkedFields);
    }

    if (withChildren && fieldOfLawDTO.getChildren() != null) {
      List<FieldOfLaw> children =
          fieldOfLawDTO.getChildren().stream()
              .map(FieldOfLawTransformer::transformToDomain)
              .toList();
      if (!children.isEmpty()) {
        builder.children(children);
        builder.childrenCount(children.size());
      }
    } else {
      builder.children(Collections.emptyList());
      if (fieldOfLawDTO.getChildren() != null) {
        builder.childrenCount(fieldOfLawDTO.getChildren().size());
      }
    }

    return builder.build();
  }
}
