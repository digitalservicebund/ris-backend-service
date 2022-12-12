package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.Texts;

public class DocumentUnitTransformer {
  private DocumentUnitTransformer() {}

  public static DocumentUnitDTO generateDTO(DocumentUnit documentUnit) {
    return fillData(null, DocumentUnitDTO.builder(), documentUnit);
  }

  public static DocumentUnitDTO enrichDTO(
      DocumentUnitDTO documentUnitDTO, DocumentUnit documentUnit) {

    return fillData(documentUnitDTO.getId(), documentUnitDTO.toBuilder(), documentUnit);
  }

  private static DocumentUnitDTO fillData(
      Long documentUnitId,
      DocumentUnitDTO.DocumentUnitDTOBuilder documentUnitDTOBuilder,
      DocumentUnit documentUnit) {

    documentUnitDTOBuilder
        .uuid(documentUnit.uuid())
        .documentnumber(documentUnit.documentNumber())
        .creationtimestamp(documentUnit.creationtimestamp())
        .fileuploadtimestamp(documentUnit.fileuploadtimestamp())
        .s3path(documentUnit.s3path())
        .filetype(documentUnit.filetype())
        .filename(documentUnit.filename());

    if (documentUnit.coreData() != null) {
      CoreData coreData = documentUnit.coreData();

      documentUnitDTOBuilder
          .fileNumber(coreData.fileNumber())
          .category(coreData.category())
          .procedure(coreData.procedure())
          .ecli(coreData.ecli())
          .appraisalBody(coreData.appraisalBody())
          .decisionDate(coreData.decisionDate() != null ? coreData.decisionDate().toString() : null)
          .legalEffect(documentUnit.coreData().legalEffect())
          .inputType(documentUnit.coreData().inputType())
          .center(documentUnit.coreData().center())
          .region(documentUnit.coreData().region());

      if (coreData.court() != null) {
        documentUnitDTOBuilder
            .courtType(documentUnit.coreData().court().type())
            .courtLocation(coreData.court().location());
      }
    }

    if (documentUnitId == null
        && documentUnit.previousDecisions() != null
        && !documentUnit.previousDecisions().isEmpty()) {

      throw new DocumentUnitTransformerException(
          "Transformation of a document unit with previous decisions only allowed by update. "
              + "Document unit must have a database id!");
    }

    if (documentUnit.previousDecisions() != null) {
      documentUnitDTOBuilder.previousDecisions(
          documentUnit.previousDecisions().stream()
              .map(
                  previousDecision ->
                      PreviousDecisionDTO.builder()
                          .id(previousDecision.id())
                          .documentUnitId(documentUnitId)
                          .courtLocation(previousDecision.courtPlace())
                          .courtType(previousDecision.courtType())
                          .fileNumber(previousDecision.fileNumber())
                          .decisionDate(previousDecision.date())
                          .build())
              .toList());
    }

    if (documentUnit.texts() != null) {
      Texts texts = documentUnit.texts();

      documentUnitDTOBuilder
          .decisionName(texts.decisionName())
          .headline(texts.headline())
          .guidingPrinciple(texts.guidingPrinciple())
          .headnote(texts.headnote())
          .tenor(texts.tenor())
          .reasons(texts.reasons())
          .caseFacts(texts.caseFacts())
          .decisionReasons(texts.decisionReasons());
    }

    return documentUnitDTOBuilder.build();
  }
}
