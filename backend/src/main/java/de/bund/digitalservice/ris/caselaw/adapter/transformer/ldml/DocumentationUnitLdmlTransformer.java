package de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml;

import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.nullSafeGet;
import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.validate;
import static de.bund.digitalservice.ris.caselaw.adapter.MappingUtils.validateNotNull;

import de.bund.digitalservice.ris.caselaw.adapter.DateUtils;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.RelatedDecision;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import jakarta.xml.bind.ValidationException;
import java.util.ArrayList;
import java.util.List;

public interface DocumentationUnitLdmlTransformer<T extends DocumentationUnit> {

  CaseLawLdml transformToLdml(T documentationUnit);

  default String buildCommonHeader(DocumentationUnit documentationUnit) throws ValidationException {
    validateCoreData(documentationUnit);
    var coreData = documentationUnit.coreData();

    StringBuilder builder = new StringBuilder();

    // Aktenzeichen
    if (coreData.fileNumbers() != null && !coreData.fileNumbers().isEmpty()) {
      builder
          .append("<p>Aktenzeichen: <akn:docNumber refersTo=\"#aktenzeichen\">")
          .append(coreData.fileNumbers().getFirst())
          .append("</akn:docNumber></p>");
    }

    // Entscheidungsdatum
    if (coreData.decisionDate() != null) {
      builder
          .append("<p>Entscheidungsdatum: <akn:docDate refersTo=\"#entscheidungsdatum\" date=\"")
          .append(DateUtils.toDateString(coreData.decisionDate()))
          .append("\">")
          .append(DateUtils.toFormattedDateString(coreData.decisionDate()))
          .append("</akn:docDate></p>");
    }

    // Gericht
    if (coreData.court() != null) {
      builder
          .append("<p>Gericht: <akn:courtType refersTo=\"#gericht\">")
          .append(coreData.court().label())
          .append("</akn:courtType></p>");
    }

    // Dokumenttyp
    if (coreData.documentType().label() != null) {
      builder
          .append("<p>")
          .append("Dokumenttyp: ")
          .append("<akn:docType refersTo=\"#dokumenttyp\">")
          .append(coreData.documentType().label())
          .append("</akn:docType>")
          .append("</p>");
    }

    return builder.toString();
  }

  default void validateCoreData(DocumentationUnit documentationUnit) throws ValidationException {
    if (documentationUnit.coreData() != null) {
      validateNotNull(documentationUnit.coreData().court(), "Court missing");
      if (documentationUnit.coreData().court() != null) {
        validateNotNull(documentationUnit.coreData().court().type(), "CourtType missing");
        validateNotNull(documentationUnit.coreData().court().type(), "CourtLabel missing");
      }
      validateNotNull(documentationUnit.coreData().documentType(), "DocumentType missing");
      validate(!documentationUnit.coreData().fileNumbers().isEmpty(), "FileNumber missing");
      validateNotNull(documentationUnit.coreData().decisionDate(), "DecisionDate missing");
    } else {
      throw new ValidationException("Core data is null");
    }
  }

  default List<RelatedDecision> buildRelatedDecisions(
      List<? extends RelatedDocumentationUnit> relatedDecisions) {
    List<RelatedDecision> previousDecision = new ArrayList<>();
    for (RelatedDocumentationUnit current : relatedDecisions) {
      RelatedDecision decision =
          RelatedDecision.builder()
              .date(DateUtils.toDateString(current.getDecisionDate()))
              .documentNumber(current.getDocumentNumber())
              .fileNumber(current.getFileNumber())
              .courtType(nullSafeGet(current.getCourt(), Court::type))
              .build();
      previousDecision.add(decision);
    }
    return previousDecision;
  }
}
