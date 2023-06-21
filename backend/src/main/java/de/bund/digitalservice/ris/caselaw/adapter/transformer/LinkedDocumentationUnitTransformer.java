package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitMetadataDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.domain.ActiveCitation;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DataSource;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.LinkedDocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.ProceedingDecision;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.util.List;

public class LinkedDocumentationUnitTransformer {
  LinkedDocumentationUnitTransformer() {}

  public static LinkedDocumentationUnit transformToDomain(
      DocumentUnitMetadataDTO documentUnitMetadataDTO) {

    if (documentUnitMetadataDTO.getDataSource() == DataSource.ACTIVE_CITATION) {
      return ActiveCitationTransformer.transformToDomain(documentUnitMetadataDTO);
    } else if (documentUnitMetadataDTO.getDataSource() == DataSource.PROCEEDING_DECISION) {
      return ProceedingDecisionTransformer.transformToDomain(documentUnitMetadataDTO);
    }

    return LinkedDocumentationUnit.builder()
        .uuid(documentUnitMetadataDTO.getUuid())
        .documentNumber(documentUnitMetadataDTO.getDocumentnumber())
        .dataSource(documentUnitMetadataDTO.getDataSource())
        .court(getCourt(documentUnitMetadataDTO))
        .fileNumber(getFileNumber(documentUnitMetadataDTO))
        .documentType(getDocumentTypeByDTO(documentUnitMetadataDTO.getDocumentTypeDTO()))
        .decisionDate(documentUnitMetadataDTO.getDecisionDate())
        .dateKnown(documentUnitMetadataDTO.isDateKnown())
        .build();
  }

  static String getFileNumber(DocumentUnitMetadataDTO documentUnitMetadataDTO) {
    if (documentUnitMetadataDTO.getFileNumbers() == null
        || documentUnitMetadataDTO.getFileNumbers().isEmpty()) {
      return null;
    }

    return documentUnitMetadataDTO.getFileNumbers().get(0).getFileNumber();
  }

  static Court getCourt(DocumentUnitMetadataDTO documentUnitMetadataDTO) {
    if (documentUnitMetadataDTO.getCourtType() == null) {
      return null;
    }

    return Court.builder()
        .type(documentUnitMetadataDTO.getCourtType())
        .location(documentUnitMetadataDTO.getCourtLocation())
        .label(
            Court.generateLabel(
                documentUnitMetadataDTO.getCourtType(), documentUnitMetadataDTO.getCourtLocation()))
        .build();
  }

  static DocumentType getDocumentTypeByDTO(DocumentTypeDTO documentTypeDTO) {
    if (documentTypeDTO == null
        || (documentTypeDTO.getLabel() == null && documentTypeDTO.getJurisShortcut() == null)) {
      return null;
    }

    return DocumentType.builder()
        .label(documentTypeDTO.getLabel())
        .jurisShortcut(documentTypeDTO.getJurisShortcut())
        .build();
  }

  public static <T extends LinkedDocumentationUnit>
      DocumentUnit transferToMetadataDocumentationUnit(T linkedDocumentationUnit) {

    CoreData coreData =
        CoreData.builder()
            .documentType(linkedDocumentationUnit.getDocumentType())
            .fileNumbers(List.of(linkedDocumentationUnit.getFileNumber()))
            .decisionDate(linkedDocumentationUnit.getDecisionDate())
            .dateKnown(linkedDocumentationUnit.isDateKnown())
            .court(linkedDocumentationUnit.getCourt())
            .build();

    return DocumentUnit.builder()
        .uuid(linkedDocumentationUnit.getUuid())
        .documentNumber(linkedDocumentationUnit.getDocumentNumber())
        .coreData(coreData)
        .dataSource(getDatasource(linkedDocumentationUnit))
        .build();
  }

  public static <T extends LinkedDocumentationUnit> DataSource getDatasource(
      T linkedDocumentationUnit) {
    if (linkedDocumentationUnit instanceof ActiveCitation) {
      return DataSource.ACTIVE_CITATION;
    } else if (linkedDocumentationUnit instanceof ProceedingDecision) {
      return DataSource.PROCEEDING_DECISION;
    } else {
      throw new RuntimeException(
          "Couldn't find data source for " + linkedDocumentationUnit.getClass());
    }
  }
}
