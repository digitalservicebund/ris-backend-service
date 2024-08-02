package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.court.CourtRepository;
import de.bund.digitalservice.ris.caselaw.domain.docx.Docx2Html;
import de.bund.digitalservice.ris.caselaw.domain.docx.DocxMetadataProperty;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DocumentUnitAttachmentService {

  private final DocumentUnitRepository repository;
  private final CourtRepository courtRepository;
  private final DocumentTypeRepository documentTypeRepository;

  public DocumentUnitAttachmentService(
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

    initializeEcli(docx2html.ecliList(), documentUnit, builder);
    initializeFieldsFromProperties(docx2html.properties(), documentUnit, builder);

    DocumentUnit updatedDocumentationUnit =
        documentUnit.toBuilder().coreData(builder.build()).build();
    repository.saveProcedures(updatedDocumentationUnit);
    repository.save(updatedDocumentationUnit);
  }

  // TODO probably split in methods
  private void initializeFieldsFromProperties(
      Map<DocxMetadataProperty, String> properties,
      DocumentUnit documentUnit,
      CoreData.CoreDataBuilder builder) {
    for (Map.Entry<DocxMetadataProperty, String> entry : properties.entrySet()) {
      switch (entry.getKey()) {
        case FILE_NUMBER -> {
          if (documentUnit.coreData().fileNumbers().isEmpty()) {
            builder.fileNumbers(Collections.singletonList(entry.getValue()));
          }
        }
        case DECISION_DATE -> {
          if (documentUnit.coreData().decisionDate() == null) {
            builder.decisionDate(LocalDate.parse(entry.getValue()));
          }
        }
        case COURT_TYPE, COURT_LOCATION, COURT -> {
          if (documentUnit.coreData().court() == null && builder.build().court() == null) {
            String courtTypeProperty = properties.get(DocxMetadataProperty.COURT_TYPE);
            String courtLocationProperty = properties.get(DocxMetadataProperty.COURT_LOCATION);
            String courtProperty = properties.get(DocxMetadataProperty.COURT);

            // We prioritize the given three values type, location and court like this:
            // 1. Use type and possibly location if type is present. Otherwise,
            // 2. use court if present.
            Optional<Court> court = Optional.empty();
            if (courtTypeProperty != null) {
              court =
                  courtRepository.findByTypeAndLocation(courtTypeProperty, courtLocationProperty);
            }
            if (court.isEmpty() && courtProperty != null) {
              court = courtRepository.findUniqueBySearchString(courtProperty);
            }

            builder.court(court.orElse(null));
          }
        }
        case APPRAISAL_BODY -> {
          if (documentUnit.coreData().appraisalBody() == null) {
            builder.appraisalBody(entry.getValue());
          }
        }
        case DOCUMENT_TYPE -> {
          if (documentUnit.coreData().documentType() == null) {
            List<DocumentType> documentTypes =
                documentTypeRepository.findCaselawBySearchStr(entry.getValue());
            if (documentTypes.size() == 1) {
              builder.documentType(documentTypes.get(0));
            }
          }
        }
        case ECLI -> {
          if (documentUnit.coreData().ecli() == null && builder.build().ecli() == null) {
            builder.ecli(entry.getValue());
          }
        }
        case PROCEDURE -> {
          if (documentUnit.coreData().procedure() == null) {
            builder.procedure(Procedure.builder().label(entry.getValue()).build());
          }
        }
        case LEGAL_EFFECT -> {
          if (documentUnit.coreData().legalEffect() == null
              || documentUnit
                  .coreData()
                  .legalEffect()
                  .equals(LegalEffect.NOT_SPECIFIED.getLabel())) {
            builder.legalEffect(entry.getValue());
          }
        }
        default -> {}
      }
    }
  }

  private void initializeEcli(
      List<String> ecliList, DocumentUnit documentUnit, CoreData.CoreDataBuilder builder) {
    if (ecliList.size() == 1 && documentUnit.coreData().ecli() == null) {
      builder.ecli(ecliList.get(0));
    }
  }
}
