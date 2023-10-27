package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingCourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingDateDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingEcliDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingFileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitMetadataDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.KeywordDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.OriginalFileDocumentDTO;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing.ContentRelatedIndexingBuilder;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.CoreData.CoreDataBuilder;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitNorm;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.Texts;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DocumentationUnitTransformer {
    private DocumentationUnitTransformer() {
    }

    public static DocumentationUnitDTO transformToDTO(
            DocumentationUnitDTO currentDto, DocumentUnit updatedDomainObject) {

        if (log.isDebugEnabled()) {
            log.debug("enrich database documentation unit '{}'", currentDto.getId());
        }

        // TODO needs null-checking
        // OriginalFileDocumentDTO originalFileDocument =
        // OriginalFileDocumentDTO.builder()
        // .extension(updatedDomainObject.filetype())
        // .filename(updatedDomainObject.filename())
        // .s3ObjectPath(updatedDomainObject.s3path())
        // .uploadTimestamp(updatedDomainObject.fileuploadtimestamp())
        // .build();

        DocumentationUnitDTO.DocumentationUnitDTOBuilder builder = currentDto.toBuilder()
                .id(updatedDomainObject.uuid())
                .documentNumber(updatedDomainObject.documentNumber());
        // .originalFileDocument(originalFileDocument)

        if (updatedDomainObject.coreData() != null) {
            var coreData = updatedDomainObject.coreData();

            builder
                    .ecli(coreData.ecli())
                    .judicialBody(coreData.appraisalBody())
                    .decisionDate(coreData.decisionDate())
                    .inputType(coreData.inputType());

            if (coreData.documentType() != null) {
                DocumentTypeDTO documentType = null;
                if (coreData.documentType().label().equals(currentDto.getDocumentType().getLabel())) {
                    documentType = currentDto.getDocumentType();
                } else {
                    documentType = DocumentTypeDTO.builder()
                            .abbreviation(coreData.documentType().jurisShortcut())
                            .label(coreData.documentType().label())
                            .build();
                }
                builder.documentType(documentType);
            }

            var fileNumbers = coreData.fileNumbers();
            if (fileNumbers != null && !fileNumbers.isEmpty()) {
                builder.fileNumbers(
                        fileNumbers.stream()
                                .map(
                                        fileNumber -> FileNumberDTO.builder()
                                                // TODO do we have to use the fileNumber repo instead?
                                                .value(fileNumber)
                                                .documentationUnit(currentDto)
                                                .build())
                                .collect(Collectors.toSet()));
            }

            if (coreData.deviatingCourts() != null) {
                Set<DeviatingCourtDTO> deviatingCourtDTOs = null;
                List<String> deviatingCourts = coreData.deviatingCourts();
                for (int i = 0; i < deviatingCourts.size(); i++) {
                    deviatingCourtDTOs.add(
                            DeviatingCourtDTO.builder()
                                    .value(deviatingCourts.get(i))
                                    .rank(Long.valueOf(i + 1))
                                    .build());
                }
                builder.deviatingCourts(deviatingCourtDTOs);
            }

            if (coreData.deviatingDecisionDates() != null) {
                Set<DeviatingDateDTO> deviatingDateDTOs = null;
                List<LocalDate> deviatingDecisionDates = coreData.deviatingDecisionDates();
                for (int i = 0; i < deviatingDecisionDates.size(); i++) {
                    deviatingDateDTOs.add(
                            DeviatingDateDTO.builder()
                                    .value(deviatingDecisionDates.get(i))
                                    .rank(Long.valueOf(i + 1))
                                    .build());
                }
                builder.deviatingDates(deviatingDateDTOs);
            }

            if (coreData.deviatingFileNumbers() != null) {
                Set<DeviatingFileNumberDTO> deviatingFileNumberDTOs = null;
                List<String> deviatingFileNumbers = coreData.deviatingFileNumbers();
                for (int i = 0; i < deviatingFileNumbers.size(); i++) {
                    deviatingFileNumberDTOs.add(
                            DeviatingFileNumberDTO.builder()
                                    .value(deviatingFileNumbers.get(i))
                                    .rank(Long.valueOf(i + 1))
                                    .build());
                }
                builder.deviatingFileNumbers(deviatingFileNumberDTOs);
            }

            if (coreData.deviatingEclis() != null) {
                Set<DeviatingEcliDTO> deviatingEcliDTOs = null;
                List<String> deviatingEclis = coreData.deviatingEclis();
                for (int i = 0; i < deviatingEclis.size(); i++) {
                    deviatingEcliDTOs.add(
                            DeviatingEcliDTO.builder()
                                    .value(deviatingEclis.get(i))
                                    .rank(Long.valueOf(i + 1))
                                    .build());
                }
                builder.deviatingEclis(deviatingEcliDTOs);
            }

            // TODO documentationOffice
            // TODO court

            // .normReferences(
            // updatedDomainObject.contentRelatedIndexing().norms().stream()
            // .map(
            // norm ->
            // NormReferenceDTO.builder()
            // // TODO do we have to use the normAbbreviation repo instead?
            // .normAbbreviation(
            // NormAbbreviationDTO.builder()
            // .id(norm.normAbbreviation().id())
            // .build())
            // .singleNorm(norm.singleNorm())
            // .dateOfVersion(norm.dateOfVersion())
            // .dateOfRelevance(norm.dateOfRelevance())
            // .build())
            // .collect(Collectors.toSet()));

            // TODO nullchecks
            // var legalEffect =
            // LegalEffect.deriveLegalEffectFrom(
            // updatedDomainObject, hasCourtChanged(currentDto, updatedDomainObject));

            // LegalEffectDTO legalEffectDTO;
            // switch (legalEffect) {
            // case NO -> legalEffectDTO = LegalEffectDTO.NEIN;
            // case YES -> legalEffectDTO = LegalEffectDTO.JA;
            // case NOT_SPECIFIED -> legalEffectDTO = LegalEffectDTO.KEINE_ANGABE;
            // default -> legalEffectDTO = LegalEffectDTO.FALSCHE_ANGABE;
            // }
            // builder.legalEffect(legalEffectDTO);

        } else {
            builder.procedure(null).ecli(null).judicialBody(null).decisionDate(null).inputType(null)
            // TODO documentationOffice
            // TODO court
            ;
        }

        if (currentDto.getId() == null
                && updatedDomainObject.proceedingDecisions() != null
                && !updatedDomainObject.proceedingDecisions().isEmpty()) {

            throw new DocumentUnitTransformerException(
                    "Transformation of a document unit with previous decisions only allowed by update. "
                            + "Document unit must have a database id!");
        }

        if (updatedDomainObject.texts() != null) {
            Texts texts = updatedDomainObject.texts();

            builder
                    .headline(texts.headline())
                    .guidingPrinciple(texts.guidingPrinciple())
                    .headnote(texts.headnote())
                    .tenor(texts.tenor())
                    .grounds(texts.reasons())
                    .caseFacts(texts.caseFacts())
                    .decisionGrounds(texts.decisionReasons());

            if (texts.decisionName() != null) {
                // Todo multiple decision names?
                //
                // builder.decisionNames(Set.of(DecisionNameDTO.builder().value(texts.decisionName()).build()));
            }
        } else {
            builder
                    // .decisionNames(null)
                    .headline(null)
                    .guidingPrinciple(null)
                    .headnote(null)
                    .tenor(null)
                    .grounds(null)
                    .caseFacts(null)
                    .decisionGrounds(null);
        }

        return builder.build();
    }

    private static boolean hasCourtChanged(
            DocumentationUnitDTO documentUnitDTO, DocumentUnit documentUnit) {
        return documentUnit == null
                || documentUnit.coreData() == null
                || documentUnit.coreData().court() == null
                // TODO court
                // || !Objects.equals(documentUnitDTO.getCourtType(),
                // documentUnit.coreData().court().type())
                // || !Objects.equals(
                // documentUnitDTO.getCourtLocation(),
                // documentUnit.coreData().court().location()
                ;
    }

    public static DocumentUnit transformToMetadataToDomain(
            DocumentationUnitMetadataDTO documentUnitMetadataDTO) {

        if (log.isDebugEnabled()) {
            log.debug(
                    "transfer database metadata documentation unit '{}' to domain object",
                    documentUnitMetadataDTO.getId());
        }

        if (documentUnitMetadataDTO == null) {
            return DocumentUnit.builder().build();
        }

        DocumentType documentType = null;
        DocumentTypeDTO documentTypeDTO = documentUnitMetadataDTO.getDocumentType();
        if (documentTypeDTO != null) {
            documentType = new DocumentType(documentTypeDTO.getAbbreviation(), documentTypeDTO.getLabel());
        }

        List<String> fileNumbers = null;
        if (documentUnitMetadataDTO.getFileNumbers() != null) {
            fileNumbers = documentUnitMetadataDTO.getFileNumbers().stream().map(FileNumberDTO::getValue).toList();
        }

        CoreData coreData = CoreData.builder()
                .fileNumbers(fileNumbers)
                // .court(
                // getCourtObject(
                // documentUnitMetadataDTO.getCourtType(),
                // documentUnitMetadataDTO.getCourtLocation()))
                .documentType(documentType)
                .ecli(documentUnitMetadataDTO.getEcli())
                .appraisalBody(documentUnitMetadataDTO.getJudicialBody())
                .decisionDate(
                        documentUnitMetadataDTO.getDecisionDate() == null
                                ? null
                                : documentUnitMetadataDTO.getDecisionDate())
                // .legalEffect(documentUnitMetadataDTO.getLegalEffect())
                .inputType(documentUnitMetadataDTO.getInputType())
                // .documentationOffice(
                //
                // getDocumentationOffice(documentUnitMetadataDTO.getDocumentationOffice()))
                // TODO multiple regions? .region(documentUnitMetadataDTO.getRegion())
                .build();

        return DocumentUnit.builder()
                .uuid(documentUnitMetadataDTO.getId())
                .coreData(coreData)
                .documentNumber(documentUnitMetadataDTO.getDocumentNumber())
                // .fileuploadtimestamp(documentUnitMetadataDTO.getFileuploadtimestamp())
                // .s3path(documentUnitMetadataDTO.getS3path())
                // .filetype(documentUnitMetadataDTO.getFiletype())
                // .filename(documentUnitMetadataDTO.getFilename())
                // .status(documentUnitMetadataDTO.getStatus())
                .build();
    }

    public static DocumentUnit transformToDomain(DocumentationUnitDTO documentationUnitDTO) {
        if (log.isDebugEnabled()) {
            log.debug(
                    "transfer database documentation unit '{}' to domain object",
                    documentationUnitDTO.getId());
        }

        if (documentationUnitDTO == null) {
            return DocumentUnit.builder().build();
        }

        CoreDataBuilder coreDataBuilder = CoreData.builder()
                .court(CourtTransformer.transformDTO((documentationUnitDTO.getCourt())))
                // documentationUnitDTO.getCourtLocation()))
                // .procedure(getProcedure(documentationUnitDTO.getProcedure()))
                // .previousProcedures(documentationUnitDTO.getPreviousProcedures())
                .documentationOffice(
                        DocumentationOffice.builder()
                                .abbreviation(documentationUnitDTO.getDocumentationOffice().getAbbreviation())
                                .build())
                // TODO multiple regions .region(documentationUnitDTO.getRegions())
                .ecli(documentationUnitDTO.getEcli())
                .decisionDate(documentationUnitDTO.getDecisionDate())
                .appraisalBody(documentationUnitDTO.getJudicialBody())
                // .legalEffect(documentationUnitDTO.getLegalEffect().toString())
                .inputType(documentationUnitDTO.getInputType());

        List<String> fileNumbers = null;
        if (documentationUnitDTO.getFileNumbers() != null) {
            fileNumbers = documentationUnitDTO.getFileNumbers().stream().map(FileNumberDTO::getValue).toList();
            coreDataBuilder.fileNumbers(fileNumbers);
        }

        List<String> deviatingFileNumbers = null;
        if (documentationUnitDTO.getDeviatingFileNumbers() != null) {
            deviatingFileNumbers = documentationUnitDTO.getDeviatingFileNumbers().stream()
                    .map(DeviatingFileNumberDTO::getValue)
                    .toList();
            coreDataBuilder.deviatingFileNumbers(deviatingFileNumbers);
        }

        List<String> deviatingCourts = null;
        if (documentationUnitDTO.getDeviatingCourts() != null) {
            deviatingCourts = documentationUnitDTO.getDeviatingCourts().stream()
                    .map(DeviatingCourtDTO::getValue)
                    .toList();
            coreDataBuilder.deviatingCourts(deviatingCourts);
        }

        DocumentType documentType = null;
        DocumentTypeDTO documentTypeDTO = documentationUnitDTO.getDocumentType();
        if (documentTypeDTO != null) {
            documentType = new DocumentType(documentTypeDTO.getAbbreviation(), documentTypeDTO.getLabel());
            coreDataBuilder.documentType(documentType);
        }

        List<String> deviatingEclis = null;
        if (documentationUnitDTO.getDeviatingEclis() != null) {
            deviatingEclis = documentationUnitDTO.getDeviatingEclis().stream()
                    .map(DeviatingEcliDTO::getValue)
                    .toList();
            coreDataBuilder.deviatingEclis(deviatingEclis);
        }

        List<LocalDate> deviatingDecisionDates = null;
        if (documentationUnitDTO.getDeviatingDates() != null) {
            deviatingDecisionDates = documentationUnitDTO.getDeviatingDates().stream()
                    .map(DeviatingDateDTO::getValue)
                    .toList();
            coreDataBuilder.deviatingDecisionDates(deviatingDecisionDates);
        }

        // List<ProceedingDecision> proceedingDecisions = null;
        // if (documentationUnitDTO.getProceedingDecisions() != null) {
        // proceedingDecisions =
        // documentationUnitDTO.getProceedingDecisions().stream()
        // .map(ProceedingDecisionTransformer::transformToDomain)
        // .toList();
        // }

        CoreData coreData = coreDataBuilder.build();

        ContentRelatedIndexingBuilder contentRelatedIndexingBuilder = ContentRelatedIndexing.builder();

        if (documentationUnitDTO.getKeywords() != null) {
            List<String> keywords = documentationUnitDTO.getKeywords().stream().map(KeywordDTO::getValue).toList();
            contentRelatedIndexingBuilder.keywords(keywords);
        }

        // List<FieldOfLaw> fieldsOfLaw = null;
        // if (documentationUnitDTO.getFieldsOfLaw() != null) {
        // fieldsOfLaw =
        // documentationUnitDTO.getFieldsOfLaw().stream()
        // .map(FieldOfLawTransformer::transformToDomain)
        // .toList();
        // }

        List<DocumentUnitNorm> norms = null;
        if (documentationUnitDTO.getNormReferences() != null) {
            norms = documentationUnitDTO.getNormReferences().stream()
                    .map(DocumentUnitNormTransformer::transformToDomain)
                    .toList();

            contentRelatedIndexingBuilder.norms(norms);
        }

        // List<ActiveCitation> activeCitations = null;
        // if (documentationUnitDTO.getActiveCitations() != null) {
        // activeCitations = documentationUnitDTO.getActiveCitations();
        // }

        ContentRelatedIndexing contentRelatedIndexing = contentRelatedIndexingBuilder.build();

        Texts texts = Texts.builder()
                // TODO multiple decisionNames
                // .decisionName(
                // documentationUnitDTO.getDecisionNames().isEmpty()
                // ? null
                // :
                // documentationUnitDTO.getDecisionNames().stream().findFirst().get().getValue())
                .headline(documentationUnitDTO.getHeadline())
                .guidingPrinciple(documentationUnitDTO.getGuidingPrinciple())
                .headnote(documentationUnitDTO.getHeadnote())
                .tenor(documentationUnitDTO.getTenor())
                .reasons(documentationUnitDTO.getGrounds())
                .caseFacts(documentationUnitDTO.getCaseFacts())
                .decisionReasons(documentationUnitDTO.getDecisionGrounds())
                .build();

        OriginalFileDocumentDTO originalFileDocumentDTO = documentationUnitDTO.getOriginalFileDocument();

        return DocumentUnit.builder()
                .uuid(documentationUnitDTO.getId())
                .documentNumber(documentationUnitDTO.getDocumentNumber())
                // .fileuploadtimestamp(originalFileDocumentDTO.getUploadTimestamp())
                // .s3path(originalFileDocumentDTO.getS3ObjectPath())
                // .filetype(originalFileDocumentDTO.getExtension())
                // .filename(originalFileDocumentDTO.getFilename())
                .coreData(coreData)
                // .proceedingDecisions(proceedingDecisions)
                .texts(texts)
                // .status(documentUnitDTO.getStatus())
                .contentRelatedIndexing(contentRelatedIndexing)
                .build();
    }
}
