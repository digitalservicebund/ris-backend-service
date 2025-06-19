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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DocumentationUnitDocxMetadataInitializationService {

  private final DocumentationUnitRepository repository;
  private final CourtRepository courtRepository;
  private final DocumentTypeRepository documentTypeRepository;

  public DocumentationUnitDocxMetadataInitializationService(
      DocumentationUnitRepository repository,
      CourtRepository courtRepository,
      DocumentTypeRepository documentTypeRepository) {
    this.repository = repository;
    this.courtRepository = courtRepository;
    this.documentTypeRepository = documentTypeRepository;
  }

  public void initializeCoreData(Decision decision, Attachment2Html attachment2Html, User user) {
    CoreData.CoreDataBuilder builder = decision.coreData().toBuilder();
    if (attachment2Html instanceof Docx2Html docx2html) {

      initializeFieldsFromProperties(docx2html.properties(), decision, builder);

      if (docx2html.ecliList().size() == 1) {
        handleEcli(decision, builder, docx2html.ecliList().getFirst());
      }
    }

    Decision updatedDecision = decision.toBuilder().coreData(builder.build()).build();
    repository.saveProcedures(updatedDecision, user);
    // save new court first to avoid override of legal effect
    repository.save(
        decision.toBuilder()
            .coreData(decision.coreData().toBuilder().court(builder.build().court()).build())
            .build(),
        user);
    repository.save(updatedDecision, user);
  }

  private void initializeFieldsFromProperties(
      Map<DocxMetadataProperty, String> properties,
      Decision decision,
      CoreData.CoreDataBuilder builder) {

    properties.forEach(
        (key, value) -> {
          switch (key) {
            case FILE_NUMBER -> handleFileNumber(decision, builder, value);
            case DECISION_DATE -> handleDecisionDate(decision, builder, value);
            case COURT_TYPE, COURT_LOCATION, COURT -> handleCourt(properties, decision, builder);
            case APPRAISAL_BODY -> handleAppraisalBody(decision, builder, value);
            case DOCUMENT_TYPE -> handleDocumentType(decision, builder, value);
            case ECLI -> handleEcli(decision, builder, value);
            case PROCEDURE -> handleProcedure(decision, builder, value);
            case LEGAL_EFFECT -> handleLegalEffect(decision, builder, value);
          }
        });
  }

  private void handleFileNumber(Decision decision, CoreData.CoreDataBuilder builder, String value) {
    if (decision.coreData().fileNumbers().isEmpty()) {
      builder.fileNumbers(Collections.singletonList(value));
    }
  }

  private void handleDecisionDate(
      Decision decision, CoreData.CoreDataBuilder builder, String value) {
    if (decision.coreData().decisionDate() == null) {
      builder.decisionDate(LocalDate.parse(value, DateTimeFormatter.ofPattern("dd.MM.yyyy")));
    }
  }

  private void handleCourt(
      Map<DocxMetadataProperty, String> properties,
      Decision decision,
      CoreData.CoreDataBuilder builder) {
    if (decision.coreData().court() == null && builder.build().court() == null) {
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
      Decision decision, CoreData.CoreDataBuilder builder, String value) {
    if (decision.coreData().appraisalBody() == null) {
      builder.appraisalBody(value);
    }
  }

  private void handleDocumentType(
      Decision decision, CoreData.CoreDataBuilder builder, String value) {
    if (decision.coreData().documentType() == null) {
      Optional<DocumentType> documentType =
          documentTypeRepository.findUniqueCaselawBySearchStr(value);
      builder.documentType(documentType.orElse(null));
    }
  }

  private void handleEcli(Decision decision, CoreData.CoreDataBuilder builder, String value) {
    if (decision.coreData().ecli() == null && builder.build().ecli() == null) {
      builder.ecli(value);
    }
  }

  private void handleProcedure(Decision decision, CoreData.CoreDataBuilder builder, String value) {
    if (decision.coreData().procedure() == null) {
      builder.procedure(Procedure.builder().label(value).build());
    }
  }

  private void handleLegalEffect(
      Decision decision, CoreData.CoreDataBuilder builder, String value) {
    if (decision.coreData().legalEffect() == null
        || decision.coreData().legalEffect().equals(LegalEffect.NOT_SPECIFIED.getLabel())) {
      builder.legalEffect(value);
    }
  }
}
