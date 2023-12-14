package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentTypeTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationUnitSearchResultTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationUnitTransformer;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitException;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitSearchInput;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitSearchResult;
import de.bund.digitalservice.ris.caselaw.domain.Procedure;
import de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationType;
import de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Repository
@Slf4j
@Primary
public class PostgresDocumentationUnitRepositoryImpl implements DocumentUnitRepository {
  private final DatabaseDocumentationUnitRepository repository;
  private final DatabaseCourtRepository databaseCourtRepository;
  private final DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  private final DatabaseKeywordRepository keywordRepository;
  private final DatabaseFieldOfLawRepository fieldOfLawRepository;
  private final DatabaseProcedureRepository procedureRepository;
  private final DatabaseRelatedDocumentationRepository relatedDocumentationRepository;

  public PostgresDocumentationUnitRepositoryImpl(
      DatabaseDocumentationUnitRepository repository,
      DatabaseCourtRepository databaseCourtRepository,
      DatabaseDocumentationOfficeRepository documentationOfficeRepository,
      DatabaseRelatedDocumentationRepository relatedDocumentationRepository,
      DatabaseKeywordRepository keywordRepository,
      DatabaseProcedureRepository procedureRepository,
      DatabaseFieldOfLawRepository fieldOfLawRepository) {

    this.repository = repository;
    this.databaseCourtRepository = databaseCourtRepository;
    this.documentationOfficeRepository = documentationOfficeRepository;
    this.keywordRepository = keywordRepository;
    this.relatedDocumentationRepository = relatedDocumentationRepository;
    this.fieldOfLawRepository = fieldOfLawRepository;
    this.procedureRepository = procedureRepository;
  }

  @Override
  @Transactional(transactionManager = "jpaTransactionManager")
  public Mono<DocumentUnit> findByDocumentNumber(String documentNumber) {
    if (log.isDebugEnabled()) {
      log.debug("find by document number: {}", documentNumber);
    }

    return Mono.just(
        DocumentationUnitTransformer.transformToDomain(
            repository.findByDocumentNumber(documentNumber).orElse(null)));
  }

  @Override
  @Transactional(transactionManager = "jpaTransactionManager")
  public DocumentUnit findByUuid(UUID uuid) {
    if (log.isDebugEnabled()) {
      log.debug("find by uuid: {}", uuid);
    }

    return DocumentationUnitTransformer.transformToDomain(repository.findById(uuid).orElse(null));
  }

  @Override
  public Mono<DocumentUnit> createNewDocumentUnit(
      String documentNumber, DocumentationOffice documentationOffice) {

    return Mono.just(
            documentationOfficeRepository.findByAbbreviation(documentationOffice.abbreviation()))
        .flatMap(
            documentationOfficeDTO ->
                Mono.just(
                    repository.save(
                        DocumentationUnitDTO.builder()
                            .id(UUID.randomUUID())
                            .documentNumber(documentNumber)
                            .documentationOffice(documentationOfficeDTO)
                            .legalEffect(LegalEffectDTO.KEINE_ANGABE)
                            .build())))
        .map(DocumentationUnitTransformer::transformToDomain);
  }

  @Transactional(transactionManager = "jpaTransactionManager")
  @Override
  public void save(DocumentUnit documentUnit) {

    DocumentationUnitDTO documentationUnitDTO =
        repository.findById(documentUnit.uuid()).orElse(null);
    if (documentationUnitDTO == null) {
      log.info("Can't save non-existing docUnit with id = " + documentUnit.uuid());
      return;
    }

    // ---
    // Doing database-related (pre) transformation

    //      if (documentUnit.coreData().documentType() != null) {
    //
    // documentationUnitDTO.setDocumentType(getDbDocType(documentUnit.coreData().documentType()));
    //      }

    //      if (documentUnit.coreData().procedure() != null) {
    //        documentationUnitDTO.setProcedures(getDbProcedures(documentUnit,
    // documentationUnitDTO));
    //      }

    if (documentUnit.coreData() != null) {
      documentationUnitDTO.getRegions().clear();
      if (documentUnit.coreData().court() != null && documentUnit.coreData().court().id() != null) {
        Optional<CourtDTO> court =
            databaseCourtRepository.findById(documentUnit.coreData().court().id());
        if (court.isPresent() && court.get().getRegion() != null) {
          documentationUnitDTO.getRegions().add(court.get().getRegion());
        }
      }
    }
    // ---

    // Transform non-database-related properties
    documentationUnitDTO =
        DocumentationUnitTransformer.transformToDTO(documentationUnitDTO, documentUnit);
    repository.save(documentationUnitDTO);
  }

  @Override
  public void saveKeywords(DocumentUnit documentUnit) {
    if (documentUnit == null || documentUnit.contentRelatedIndexing() == null) {
      return;
    }

    repository
        .findById(documentUnit.uuid())
        .ifPresent(
            documentationUnitDTO -> {
              ContentRelatedIndexing contentRelatedIndexing = documentUnit.contentRelatedIndexing();

              if (contentRelatedIndexing.keywords() != null) {
                List<DocumentationUnitKeywordDTO> documentationUnitKeywordDTOs = new ArrayList<>();

                List<String> keywords = contentRelatedIndexing.keywords();
                for (int i = 0; i < keywords.size(); i++) {
                  String value = keywords.get(i);

                  KeywordDTO keywordDTO =
                      keywordRepository
                          .findByValue(value)
                          .orElseGet(
                              () ->
                                  keywordRepository.save(
                                      KeywordDTO.builder().value(value).build()));

                  DocumentationUnitKeywordDTO documentationUnitKeywordDTO =
                      DocumentationUnitKeywordDTO.builder()
                          .primaryKey(
                              new DocumentationUnitKeywordId(
                                  documentationUnitDTO.getId(), keywordDTO.getId()))
                          .documentationUnit(documentationUnitDTO)
                          .keyword(keywordDTO)
                          .rank(i + 1)
                          .build();

                  documentationUnitKeywordDTOs.add(documentationUnitKeywordDTO);
                }

                documentationUnitDTO.setDocumentationUnitKeywordDTOs(documentationUnitKeywordDTOs);

                repository.save(documentationUnitDTO);
              }
            });
  }

  @Override
  public void saveFieldsOfLaw(DocumentUnit documentUnit) {
    if (documentUnit == null || documentUnit.contentRelatedIndexing() == null) {
      return;
    }

    repository
        .findById(documentUnit.uuid())
        .ifPresent(
            documentationUnitDTO -> {
              ContentRelatedIndexing contentRelatedIndexing = documentUnit.contentRelatedIndexing();

              if (contentRelatedIndexing.fieldsOfLaw() != null) {
                List<DocumentationUnitFieldOfLawDTO> documentationUnitFieldOfLawDTOs =
                    new ArrayList<>();

                List<FieldOfLaw> fieldsOfLaw = contentRelatedIndexing.fieldsOfLaw();
                for (int i = 0; i < fieldsOfLaw.size(); i++) {
                  FieldOfLaw fieldOfLaw = fieldsOfLaw.get(i);

                  Optional<FieldOfLawDTO> fieldOfLawDTOOptional =
                      fieldOfLawRepository.findById(fieldOfLaw.id());

                  if (fieldOfLawDTOOptional.isPresent()) {
                    DocumentationUnitFieldOfLawDTO documentationUnitFieldOfLawDTO =
                        DocumentationUnitFieldOfLawDTO.builder()
                            .primaryKey(
                                new DocumentationUnitFieldOfLawId(
                                    documentationUnitDTO.getId(),
                                    fieldOfLawDTOOptional.get().getId()))
                            .rank(i + 1)
                            .build();
                    documentationUnitFieldOfLawDTO.setDocumentationUnit(documentationUnitDTO);
                    documentationUnitFieldOfLawDTO.setFieldOfLaw(fieldOfLawDTOOptional.get());

                    documentationUnitFieldOfLawDTOs.add(documentationUnitFieldOfLawDTO);
                  } else {
                    throw new DocumentationUnitException(
                        "field of law with id: '" + fieldOfLaw.id() + "' not found.");
                  }
                }

                documentationUnitDTO.setDocumentationUnitFieldsOfLaw(
                    documentationUnitFieldOfLawDTOs);

                repository.save(documentationUnitDTO);
              }
            });
  }

  @Override
  @Transactional(transactionManager = "jpaTransactionManager")
  public void saveProcedures(DocumentUnit documentUnit) {
    if (documentUnit == null
        || documentUnit.coreData() == null
        || documentUnit.coreData().procedure() == null) {
      return;
    }

    repository
        .findById(documentUnit.uuid())
        .ifPresent(
            documentationUnitDTO -> {
              Procedure procedure = documentUnit.coreData().procedure();

              List<DocumentationUnitProcedureDTO> documentationUnitProcedureDTOs =
                  new ArrayList<>();

              ProcedureDTO procedureDTO = null;
              if (procedure.id() == null) {
                procedureDTO =
                    ProcedureDTO.builder()
                        .label(procedure.label())
                        .createdAt(Instant.now())
                        .documentationOffice(documentationUnitDTO.getDocumentationOffice())
                        .build();

                procedureDTO = procedureRepository.save(procedureDTO);

              } else {
                Optional<ProcedureDTO> optionalProcedureDTO =
                    procedureRepository.findById(procedure.id());
                if (optionalProcedureDTO.isPresent()) {
                  procedureDTO = optionalProcedureDTO.get();
                }
              }

              boolean sameAsLast =
                  !documentationUnitDTO.getProcedures().isEmpty()
                      && documentationUnitDTO
                          .getProcedures()
                          .get(0)
                          .getProcedure()
                          .equals(procedureDTO);

              if (procedureDTO != null && !sameAsLast) {
                DocumentationUnitProcedureDTO documentationUnitProcedureDTO =
                    DocumentationUnitProcedureDTO.builder()
                        .primaryKey(
                            new DocumentationUnitProcedureId(
                                documentationUnitDTO.getId(), procedureDTO.getId()))
                        .documentationUnit(documentationUnitDTO)
                        .procedure(procedureDTO)
                        .build();
                documentationUnitProcedureDTOs.add(documentationUnitProcedureDTO);
              }

              documentationUnitDTO
                  .getProcedures()
                  .forEach(
                      documentationUnitProcedureDTO -> {
                        DocumentationUnitProcedureDTO newLink =
                            DocumentationUnitProcedureDTO.builder()
                                .primaryKey(
                                    new DocumentationUnitProcedureId(
                                        documentationUnitDTO.getId(),
                                        documentationUnitProcedureDTO.getProcedure().getId()))
                                .documentationUnit(documentationUnitDTO)
                                .procedure(documentationUnitProcedureDTO.getProcedure())
                                .build();
                        documentationUnitProcedureDTOs.add(newLink);
                      });

              updateProcedureRank(documentationUnitProcedureDTOs);

              documentationUnitDTO.getProcedures().clear();
              documentationUnitDTO.getProcedures().addAll(documentationUnitProcedureDTOs);

              repository.save(documentationUnitDTO);
            });
  }

  private void updateProcedureRank(
      List<DocumentationUnitProcedureDTO> documentationUnitProcedureDTOs) {
    for (int i = 0; i < documentationUnitProcedureDTOs.size(); i++) {
      DocumentationUnitProcedureDTO documentationUnitProcedureDTO =
          documentationUnitProcedureDTOs.get(i);
      documentationUnitProcedureDTO.setRank(i + 1);
    }
  }

  //  private DocumentTypeDTO getDbDocType(DocumentType documentType) {
  //    if (documentType == null) {
  //      return null;
  //    }
  //    // TODO cache category at application start
  //    DocumentTypeDTO docTypeDTO =
  //        databaseDocumentTypeRepository.findFirstByAbbreviationAndCategory(
  //            documentType.jurisShortcut(),
  // databaseDocumentCategoryRepository.findFirstByLabel("R"));
  //
  //    if (docTypeDTO == null) {
  //      throw new DocumentationUnitException(
  //          "no document type for the shortcut '" + documentType.jurisShortcut() + "' found.");
  //    }
  //    return docTypeDTO;
  //  }
  //
  //  private List<ProcedureDTO> getDbProcedures(
  //      DocumentUnit documentUnit, DocumentationUnitDTO documentationUnitDTO) {
  //    Stream<String> procedureLabels = Stream.of(documentUnit.coreData().procedure().label());
  //    if (documentUnit.coreData().previousProcedures() != null)
  //      procedureLabels =
  //          Stream.concat(procedureLabels, documentUnit.coreData().previousProcedures().stream());
  //
  //    return procedureLabels
  //        .map(procedureLabel -> findOrCreateProcedureDTO(procedureLabel, documentationUnitDTO))
  //        .toList();
  //  }
  //
  //  private ProcedureDTO findOrCreateProcedureDTO(
  //      String procedureLabel, DocumentationUnitDTO documentationUnitDTO) {
  //    return Optional.ofNullable(
  //            databaseProcedureRepository.findByLabelAndDocumentationOffice(
  //                procedureLabel, documentationUnitDTO.getDocumentationOffice()))
  //        .orElse(
  //            ProcedureDTO.builder()
  //                .label(procedureLabel)
  //                .documentationOffice(documentationUnitDTO.getDocumentationOffice())
  //                .documentationUnits(List.of(documentationUnitDTO))
  //                .build());
  //  }

  @Override
  @Transactional(transactionManager = "jpaTransactionManager")
  public Mono<DocumentUnit> attachFile(
      UUID documentUnitUuid, String fileUuid, String type, String fileName) {
    var docUnitDto = repository.findById(documentUnitUuid).orElseThrow();
    docUnitDto.setOriginalFileDocument(
        OriginalFileDocumentDTO.builder()
            .id(UUID.randomUUID())
            .s3ObjectPath(fileUuid)
            .filename(fileName)
            .extension(type)
            .uploadTimestamp(Instant.now())
            .build());
    docUnitDto = repository.save(docUnitDto);
    return Mono.just(DocumentationUnitTransformer.transformToDomain(docUnitDto));
  }

  @Override
  @Transactional(transactionManager = "jpaTransactionManager")
  public DocumentUnit removeFile(UUID documentUnitId) {
    var docUnitDto = repository.findById(documentUnitId).orElseThrow();

    docUnitDto.setOriginalFileDocument(null);

    docUnitDto = repository.save(docUnitDto);

    return DocumentationUnitTransformer.transformToDomain(docUnitDto);
  }

  @Override
  public void delete(DocumentUnit documentUnit) {
    repository.deleteById(documentUnit.uuid());
  }

  @Override
  @Transactional(transactionManager = "jpaTransactionManager")
  public Slice<RelatedDocumentationUnit> searchByRelatedDocumentationUnit(
      RelatedDocumentationUnit relatedDocumentationUnit,
      DocumentationOffice documentationOffice,
      Pageable pageable) {
    String courtType =
        Optional.ofNullable(relatedDocumentationUnit.getCourt()).map(Court::type).orElse(null);
    String courtLocation =
        Optional.ofNullable(relatedDocumentationUnit.getCourt()).map(Court::location).orElse(null);

    DocumentationOfficeDTO documentationOfficeDTO =
        documentationOfficeRepository.findByAbbreviation(documentationOffice.abbreviation());

    Slice<DocumentationUnitSearchResultDTO> documentationUnitSearchResultDTOPage =
        repository.searchByDocumentUnitSearchInput(
            documentationOfficeDTO.getId(),
            relatedDocumentationUnit.getFileNumber(),
            courtType,
            courtLocation,
            relatedDocumentationUnit.getDecisionDate(),
            null,
            null,
            false,
            false,
            DocumentTypeTransformer.transformToDTO(relatedDocumentationUnit.getDocumentType()),
            pageable);

    return documentationUnitSearchResultDTOPage.map(
        DocumentationUnitSearchResultTransformer::transformToRelatedDocumentation);
  }

  @Transactional(transactionManager = "jpaTransactionManager")
  public Slice<DocumentationUnitSearchResult> searchByDocumentationUnitSearchInput(
      Pageable pageable,
      DocumentationOffice documentationOffice,
      DocumentationUnitSearchInput searchInput) {
    if (log.isDebugEnabled()) {
      log.debug("Find by overview search: {}, {}", documentationOffice, searchInput);
    }

    DocumentationOfficeDTO documentationOfficeDTO =
        documentationOfficeRepository.findByAbbreviation(documentationOffice.abbreviation());

    Boolean withError =
        Optional.ofNullable(searchInput.status()).map(Status::withError).orElse(false);

    Slice<DocumentationUnitSearchResultDTO> documentationUnitSearchResultDTOPage =
        repository.searchByDocumentUnitSearchInput(
            documentationOfficeDTO.getId(),
            searchInput.documentNumberOrFileNumber(),
            searchInput.courtType(),
            searchInput.courtLocation(),
            searchInput.decisionDate(),
            searchInput.decisionDateEnd(),
            searchInput.status() != null ? searchInput.status().publicationStatus() : null,
            withError,
            searchInput.myDocOfficeOnly(),
            null,
            pageable);

    return documentationUnitSearchResultDTOPage.map(
        DocumentationUnitSearchResultTransformer::transformToDomain);
  }

  @Override
  public Map<RelatedDocumentationType, Long> getAllDocumentationUnitWhichLink(
      UUID documentationUnitId) {
    return relatedDocumentationRepository
        .findAllByReferencedDocumentationUnitId(documentationUnitId)
        .stream()
        .collect(Collectors.groupingBy(RelatedDocumentationDTO::getType, Collectors.counting()));
  }
}
