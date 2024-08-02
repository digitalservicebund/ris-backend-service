package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.court.CourtRepository;
import de.bund.digitalservice.ris.caselaw.domain.docx.Docx2Html;
import de.bund.digitalservice.ris.caselaw.domain.docx.DocxMetadataProperty;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DocumentationUnitDocxMetadataInitializationService {

  private final DocumentUnitRepository repository;
  private final CourtRepository courtRepository;
  private final DocumentTypeRepository documentTypeRepository;

  public DocumentationUnitDocxMetadataInitializationService(
      DocumentUnitRepository repository,
      CourtRepository courtRepository,
      DocumentTypeRepository documentTypeRepository) {
    this.repository = repository;
    this.courtRepository = courtRepository;
    this.documentTypeRepository = documentTypeRepository;
  }

  public void initializeCoreData(UUID uuid, Docx2Html docx2html) {
    Optional<DocumentUnit> documentationUnitOptional = repository.findByUuid(uuid);
    if (documentationUnitOptional.isEmpty()) {
      return;
    }
    DocumentUnit documentUnit = documentationUnitOptional.get();
    CoreData.CoreDataBuilder builder = documentationUnitOptional.get().coreData().toBuilder();

    if (docx2html.ecliList().size() == 1) {
      handleEcli(documentUnit, builder, docx2html.ecliList().get(0));
    }
    initializeFieldsFromProperties(docx2html.properties(), documentUnit, builder);

    DocumentUnit updatedDocumentationUnit =
        documentUnit.toBuilder().coreData(builder.build()).build();
    repository.saveProcedures(updatedDocumentationUnit);
    // save new court first to avoid override of legal effect
    repository.save(
        documentUnit.toBuilder()
            .coreData(documentUnit.coreData().toBuilder().court(builder.build().court()).build())
            .build());
    repository.save(updatedDocumentationUnit);
  }

  private void initializeFieldsFromProperties(
      Map<DocxMetadataProperty, String> properties,
      DocumentUnit documentUnit,
      CoreData.CoreDataBuilder builder) {

    properties.forEach(
        (key, value) -> {
          switch (key) {
            case FILE_NUMBER -> handleFileNumber(documentUnit, builder, value);
            case DECISION_DATE -> handleDecisionDate(documentUnit, builder, value);
            case COURT_TYPE, COURT_LOCATION, COURT ->
                handleCourt(properties, documentUnit, builder);
            case APPRAISAL_BODY -> handleAppraisalBody(documentUnit, builder, value);
            case DOCUMENT_TYPE -> handleDocumentType(documentUnit, builder, value);
            case ECLI -> handleEcli(documentUnit, builder, value);
            case PROCEDURE -> handleProcedure(documentUnit, builder, value);
            case LEGAL_EFFECT -> handleLegalEffect(documentUnit, builder, value);
            default -> {}
          }
        });
  }

  private void handleFileNumber(
      DocumentUnit documentUnit, CoreData.CoreDataBuilder builder, String value) {
    if (documentUnit.coreData().fileNumbers().isEmpty()) {
      builder.fileNumbers(Collections.singletonList(value));
    }
  }

  private void handleDecisionDate(
      DocumentUnit documentUnit, CoreData.CoreDataBuilder builder, String value) {
    if (documentUnit.coreData().decisionDate() == null) {
      builder.decisionDate(LocalDate.parse(value, DateTimeFormatter.ofPattern("dd.MM.yyyy")));
    }
  }

  private void handleCourt(
      Map<DocxMetadataProperty, String> properties,
      DocumentUnit documentUnit,
      CoreData.CoreDataBuilder builder) {
    if (documentUnit.coreData().court() == null && builder.build().court() == null) {
      String courtTypeProperty = properties.get(DocxMetadataProperty.COURT_TYPE);
      String courtLocationProperty = properties.get(DocxMetadataProperty.COURT_LOCATION);
      String courtProperty = properties.get(DocxMetadataProperty.COURT);

      Optional<Court> court = Optional.empty();
      if (courtTypeProperty != null) {
        court = courtRepository.findByTypeAndLocation(courtTypeProperty, courtLocationProperty);
      }
      if (court.isEmpty() && courtProperty != null) {
        court = courtRepository.findUniqueBySearchString(courtProperty);
      }

      builder.court(court.orElse(null));
    }
  }

  private void handleAppraisalBody(
      DocumentUnit documentUnit, CoreData.CoreDataBuilder builder, String value) {
    if (documentUnit.coreData().appraisalBody() == null) {
      builder.appraisalBody(value);
    }
  }

  private void handleDocumentType(
      DocumentUnit documentUnit, CoreData.CoreDataBuilder builder, String value) {
    if (documentUnit.coreData().documentType() == null) {
      Optional<DocumentType> documentType =
          documentTypeRepository.findUniqueCaselawBySearchStr(value);
      builder.documentType(documentType.orElse(null));
    }
  }

  private void handleEcli(
      DocumentUnit documentUnit, CoreData.CoreDataBuilder builder, String value) {
    if (documentUnit.coreData().ecli() == null && builder.build().ecli() == null) {
      builder.ecli(value);
    }
  }

  private void handleProcedure(
      DocumentUnit documentUnit, CoreData.CoreDataBuilder builder, String value) {
    if (documentUnit.coreData().procedure() == null) {
      builder.procedure(Procedure.builder().label(value).build());
    }
  }

  private void handleLegalEffect(
      DocumentUnit documentUnit, CoreData.CoreDataBuilder builder, String value) {
    if (documentUnit.coreData().legalEffect() == null
        || documentUnit.coreData().legalEffect().equals(LegalEffect.NOT_SPECIFIED.getLabel())) {
      builder.legalEffect(value);
    }
  }
}
