package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FieldOfLawDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.Notation;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw.FieldOfLawBuilder;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.Norm;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class FieldOfLawTransformer {
  private FieldOfLawTransformer() {}

  public static FieldOfLaw transformToDomain(
      FieldOfLawDTO fieldOfLawDTO, boolean withChildren, boolean withNorms) {
    if (fieldOfLawDTO == null) {
      return null;
    }

    FieldOfLawBuilder builder =
        FieldOfLaw.builder()
            .id(fieldOfLawDTO.getId())
            .identifier(fieldOfLawDTO.getIdentifier())
            .notation(
                Optional.ofNullable(fieldOfLawDTO.getNotation())
                    .map(Notation::toString)
                    .orElse(null))
            .text(fieldOfLawDTO.getText());

    if (fieldOfLawDTO.getParent() != null) {
      builder.parent(transformToDomain(fieldOfLawDTO.getParent(), false, false));
    }

    if (withNorms && fieldOfLawDTO.getNorms() != null) {
      List<Norm> norms =
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

    if (fieldOfLawDTO.getFieldOfLawTextReferences() != null) {
      List<String> linkedFields =
          fieldOfLawDTO.getFieldOfLawTextReferences().stream()
              .map(FieldOfLawDTO::getIdentifier)
              .toList();
      builder.linkedFields(linkedFields);
    }

    if (fieldOfLawDTO.getChildren() != null) {
      builder.hasChildren(!fieldOfLawDTO.getChildren().isEmpty());
    }

    if (withChildren && fieldOfLawDTO.getChildren() != null) {
      List<FieldOfLaw> children =
          fieldOfLawDTO.getChildren().stream()
              .map(fol -> FieldOfLawTransformer.transformToDomain(fol, false, withNorms))
              .toList();
      if (!children.isEmpty()) {
        builder.children(children);
        builder.hasChildren(true);
      }
    } else {
      builder.children(Collections.emptyList());
    }

    return builder.build();
  }
}
