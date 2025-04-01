package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.IgnoredTextCheckWordDTO;
import de.bund.digitalservice.ris.caselaw.domain.exception.TextCheckUnsupportedTypeException;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words.IgnoredTextCheckType;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words.IgnoredTextCheckWord;
import java.util.UUID;
import lombok.experimental.UtilityClass;

@UtilityClass
public class IgnoredTextCheckWordTransformer {

  public static IgnoredTextCheckWordDTO transformToDTO(
      IgnoredTextCheckWord ignoredTextCheckWord,
      UUID documentationOfficeId,
      UUID documentationUnitId) {

    switch (ignoredTextCheckWord.getType()) {
      case DOCUMENTATION_OFFICE -> {
        return IgnoredTextCheckWordDTO.builder()
            .id(ignoredTextCheckWord.getId())
            .word(ignoredTextCheckWord.getWord())
            .documentationOffice(DocumentationOfficeDTO.builder().id(documentationOfficeId).build())
            .build();
      }
      case DOCUMENTATION_UNIT -> {
        if (documentationUnitId == null) {
          throw new TextCheckUnsupportedTypeException(
              "Following type must have documentation unit id");
        }

        return IgnoredTextCheckWordDTO.builder()
            .id(ignoredTextCheckWord.getId())
            .word(ignoredTextCheckWord.getWord())
            .documentationUnit(
                ignoredTextCheckWord.getType() == IgnoredTextCheckType.DOCUMENTATION_UNIT
                    ? DecisionDTO.builder().id(documentationUnitId).build()
                    : null)
            .documentationOffice(DocumentationOfficeDTO.builder().id(documentationOfficeId).build())
            .build();
      }
      default -> throw new TextCheckUnsupportedTypeException();
    }
  }

  public static IgnoredTextCheckWord transformToDomain(
      IgnoredTextCheckWordDTO ignoredTextCheckWordDTO) {
    return IgnoredTextCheckWord.builder()
        .id(ignoredTextCheckWordDTO.getId())
        .word(ignoredTextCheckWordDTO.getWord())
        .type(transformType(ignoredTextCheckWordDTO))
        .isEditable(ignoredTextCheckWordDTO.getJurisId() == null)
        .build();
  }

  public static IgnoredTextCheckType transformType(
      IgnoredTextCheckWordDTO ignoredTextCheckWordDTO) {
    if (ignoredTextCheckWordDTO.getDocumentationUnit() != null) {
      return IgnoredTextCheckType.DOCUMENTATION_UNIT;
    } else {
      return IgnoredTextCheckType.DOCUMENTATION_OFFICE;
    }
  }
}
