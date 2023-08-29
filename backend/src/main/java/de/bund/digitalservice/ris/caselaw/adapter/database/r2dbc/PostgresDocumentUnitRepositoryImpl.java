package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitSearchEntryDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPADocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPADocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPAProcedureDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPAProcedureLinkDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPAProcedureLinkRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPAProcedureRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitDTO.DocumentUnitDTOBuilder;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseCitationStyleRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseCourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseDocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseFieldOfLawRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseNormAbbreviationRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.NormAbbreviationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.StateDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.StateRepository;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ActiveCitationTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DeviatingDecisionDateTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentUnitTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationUnitLinkTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationUnitSearchEntryTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.IncorrectCourtTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.LinkedDocumentationUnitTransformer;
import de.bund.digitalservice.ris.caselaw.domain.ActiveCitation;
import de.bund.digitalservice.ris.caselaw.domain.DataSource;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitNorm;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitSearchInput;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatus;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitException;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitLink;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitLinkType;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitSearchEntry;
import de.bund.digitalservice.ris.caselaw.domain.LegalEffect;
import de.bund.digitalservice.ris.caselaw.domain.LinkedDocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.Procedure;
import de.bund.digitalservice.ris.caselaw.domain.ProceedingDecision;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@Slf4j
public class PostgresDocumentUnitRepositoryImpl implements DocumentUnitRepository {
  private final DatabaseDocumentUnitRepository repository;
  private final DatabaseDocumentUnitMetadataRepository metadataRepository;
  private final FileNumberRepository fileNumberRepository;
  private final DeviatingEcliRepository deviatingEcliRepository;
  private final DatabaseDeviatingDecisionDateRepository deviatingDecisionDateRepository;
  private final DatabaseIncorrectCourtRepository incorrectCourtRepository;
  private final DatabaseCourtRepository databaseCourtRepository;
  private final StateRepository stateRepository;
  private final DatabaseDocumentTypeRepository databaseDocumentTypeRepository;
  private final DatabaseFieldOfLawRepository fieldOfLawRepository;
  private final DatabaseDocumentUnitFieldsOfLawRepository documentUnitFieldsOfLawRepository;
  private final DatabaseKeywordRepository keywordRepository;
  private final DatabaseDocumentUnitNormRepository documentUnitNormRepository;
  private final JPADocumentationOfficeRepository documentationOfficeRepository;
  private final DatabaseDocumentUnitStatusRepository databaseDocumentUnitStatusRepository;
  private final DatabaseNormAbbreviationRepository normAbbreviationRepository;
  private final DatabaseDocumentationUnitLinkRepository documentationUnitLinkRepository;
  private final DatabaseCitationStyleRepository citationStyleRepository;
  private final JPAProcedureRepository procedureRepository;
  private final JPAProcedureLinkRepository procedureLinkRepository;
  private final EntityManager entityManager;

  public PostgresDocumentUnitRepositoryImpl(
      DatabaseDocumentUnitRepository repository,
      DatabaseDocumentUnitMetadataRepository metadataRepository,
      FileNumberRepository fileNumberRepository,
      DeviatingEcliRepository deviatingEcliRepository,
      DatabaseDeviatingDecisionDateRepository deviatingDecisionDateRepository,
      DatabaseIncorrectCourtRepository incorrectCourtRepository,
      DatabaseCourtRepository databaseCourtRepository,
      StateRepository stateRepository,
      DatabaseDocumentTypeRepository databaseDocumentTypeRepository,
      DatabaseFieldOfLawRepository fieldOfLawRepository,
      DatabaseDocumentUnitFieldsOfLawRepository documentUnitFieldsOfLawRepository,
      DatabaseKeywordRepository keywordRepository,
      DatabaseDocumentUnitNormRepository documentUnitNormRepository,
      DatabaseDocumentUnitStatusRepository databaseDocumentUnitStatusRepository,
      DatabaseNormAbbreviationRepository normAbbreviationRepository,
      DatabaseDocumentationUnitLinkRepository documentationUnitLinkRepository,
      DatabaseCitationStyleRepository citationStyleRepository,
      JPADocumentationOfficeRepository documentationOfficeRepository,
      JPAProcedureRepository procedureRepository,
      JPAProcedureLinkRepository procedureLinkRepository,
      EntityManager entityManager) {

    this.repository = repository;
    this.metadataRepository = metadataRepository;
    this.fileNumberRepository = fileNumberRepository;
    this.deviatingEcliRepository = deviatingEcliRepository;
    this.deviatingDecisionDateRepository = deviatingDecisionDateRepository;
    this.incorrectCourtRepository = incorrectCourtRepository;
    this.databaseCourtRepository = databaseCourtRepository;
    this.stateRepository = stateRepository;
    this.databaseDocumentTypeRepository = databaseDocumentTypeRepository;
    this.fieldOfLawRepository = fieldOfLawRepository;
    this.documentUnitFieldsOfLawRepository = documentUnitFieldsOfLawRepository;
    this.keywordRepository = keywordRepository;
    this.documentUnitNormRepository = documentUnitNormRepository;
    this.documentationOfficeRepository = documentationOfficeRepository;
    this.databaseDocumentUnitStatusRepository = databaseDocumentUnitStatusRepository;
    this.normAbbreviationRepository = normAbbreviationRepository;
    this.documentationUnitLinkRepository = documentationUnitLinkRepository;
    this.citationStyleRepository = citationStyleRepository;
    this.procedureRepository = procedureRepository;
    this.procedureLinkRepository = procedureLinkRepository;
    this.entityManager = entityManager;
  }

  @Override
  public Mono<DocumentUnit> findByDocumentNumber(String documentNumber) {
    if (log.isDebugEnabled()) {
      log.debug("find by document number: {}", documentNumber);
    }

    return repository
        .findByDocumentnumber(documentNumber)
        .flatMap(this::injectAdditionalInformation)
        .map(DocumentUnitTransformer::transformDTO);
  }

  @Override
  public Mono<DocumentUnit> findByUuid(UUID uuid) {
    if (log.isDebugEnabled()) {
      log.debug("find by uuid: {}", uuid);
    }

    return repository
        .findByUuid(uuid)
        .flatMap(this::injectAdditionalInformation)
        .map(DocumentUnitTransformer::transformDTO);
  }

  @Override
  public Mono<DocumentUnit> createNewDocumentUnit(
      String documentNumber, DocumentationOffice documentationOffice) {

    return Mono.just(documentationOfficeRepository.findByLabel(documentationOffice.label()))
        .flatMap(
            documentationOfficeDTO ->
                metadataRepository.save(
                    DocumentUnitMetadataDTO.builder()
                        .uuid(UUID.randomUUID())
                        .creationtimestamp(Instant.now())
                        .documentnumber(documentNumber)
                        .dataSource(DataSource.NEURIS)
                        .documentationOfficeId(documentationOfficeDTO.getId())
                        .documentationOffice(documentationOfficeDTO)
                        .dateKnown(true)
                        .legalEffect(LegalEffect.NOT_SPECIFIED.getLabel())
                        .build()))
        .map(DocumentUnitTransformer::transformMetadataToDomain);
  }

  @Override
  @Transactional(transactionManager = "connectionFactoryTransactionManager")
  public Mono<DocumentUnit> save(DocumentUnit documentUnit) {
    return repository
        .findByUuid(documentUnit.uuid())
        .flatMap(
            documentUnitDTO -> {
              DocumentType documentType = null;
              if (documentUnit.coreData() != null) {
                documentType = documentUnit.coreData().documentType();
              }
              return enrichDocumentType(documentUnitDTO, documentType);
            })
        .flatMap(documentUnitDTO -> enrichLegalEffect(documentUnitDTO, documentUnit))
        .flatMap(documentUnitDTO -> enrichRegion(documentUnitDTO, documentUnit))
        .map(documentUnitDTO -> DocumentUnitTransformer.enrichDTO(documentUnitDTO, documentUnit))
        .flatMap(documentUnitDTO -> saveProcedure(documentUnitDTO, documentUnit))
        .flatMap(repository::save)
        .flatMap(
            documentUnitDTO -> {
              List<String> fileNumbers = Collections.emptyList();
              if (documentUnit.coreData() != null
                  && documentUnit.coreData().fileNumbers() != null) {
                fileNumbers = documentUnit.coreData().fileNumbers();
              }
              return saveFileNumbers(documentUnitDTO, fileNumbers);
            })
        .flatMap(documentUnitDTO -> saveDeviatingFileNumbers(documentUnitDTO, documentUnit))
        .flatMap(documentUnitDTO -> saveDeviatingEcli(documentUnitDTO, documentUnit))
        .flatMap(documentUnitDTO -> saveDeviatingDecisionDate(documentUnitDTO, documentUnit))
        .flatMap(documentUnitDTO -> saveIncorrectCourt(documentUnitDTO, documentUnit))
        .flatMap(documentUnitDTO -> saveNorms(documentUnitDTO, documentUnit))
        .flatMap(this::injectStatus)
        .flatMap(this::injectKeywords)
        .flatMap(this::injectFieldsOfLaw)
        .flatMap(this::injectPreviousProcedures)
        .flatMap(documentUnitDTO -> saveActiveCitations(documentUnitDTO, documentUnit))
        .flatMap(documentUnitDTO -> saveProceedingDecisions(documentUnitDTO, documentUnit))
        .map(DocumentUnitTransformer::transformDTO);
  }

  private Mono<DocumentUnitDTO> enrichDocumentType(
      DocumentUnitDTO documentUnitDTO, DocumentType documentType) {
    if (documentType == null) {
      documentUnitDTO.setDocumentTypeId(null);
      return Mono.just(documentUnitDTO);
    }

    return databaseDocumentTypeRepository
        .findByJurisShortcut(documentType.jurisShortcut())
        .map(
            documentTypeDTO -> {
              if (!documentTypeDTO.getLabel().equals(documentType.label())) {
                throw new DocumentUnitException(
                    "DocumentType label does not match the database entry, this should not happen");
              }
              documentUnitDTO.setDocumentTypeDTO(documentTypeDTO);
              documentUnitDTO.setDocumentTypeId(documentTypeDTO.getId());
              return documentUnitDTO;
            })
        .switchIfEmpty(
            Mono.error(
                new DocumentationUnitException(
                    "no document type for the shortcut '"
                        + documentType.jurisShortcut()
                        + "' found.")));
  }

  private boolean hasCourtChanged(DocumentUnitDTO documentUnitDTO, DocumentUnit documentUnit) {
    return documentUnit == null
        || documentUnit.coreData() == null
        || documentUnit.coreData().court() == null
        || !Objects.equals(documentUnitDTO.getCourtType(), documentUnit.coreData().court().type())
        || !Objects.equals(
            documentUnitDTO.getCourtLocation(), documentUnit.coreData().court().location());
  }

  private Mono<DocumentUnitDTO> enrichLegalEffect(
      DocumentUnitDTO documentUnitDTO, DocumentUnit documentUnit) {
    documentUnitDTO.setLegalEffect(
        LegalEffect.deriveFrom(documentUnit, hasCourtChanged(documentUnitDTO, documentUnit)));
    return Mono.just(documentUnitDTO);
  }

  private Mono<DocumentUnitDTO> enrichRegion(
      DocumentUnitDTO documentUnitDTO, DocumentUnit documentUnit) {
    if (!hasCourtChanged(documentUnitDTO, documentUnit)) {
      return Mono.just(documentUnitDTO);
    }

    return getCourt(documentUnit)
        .flatMap(
            courtDTO -> {
              if (courtDTO.getFederalstate() == null) {
                return Mono.just(StateDTO.builder().label(courtDTO.getRegion()).build());
              }
              return stateRepository
                  .findByJurisshortcut(courtDTO.getFederalstate())
                  .defaultIfEmpty(StateDTO.builder().build());
            })
        .map(
            stateDTO -> {
              documentUnitDTO.setRegion(stateDTO.getLabel());
              return documentUnitDTO;
            });
  }

  private Mono<CourtDTO> getCourt(DocumentUnit documentUnit) {
    if (documentUnit == null
        || documentUnit.coreData() == null
        || documentUnit.coreData().court() == null) {
      return Mono.just(CourtDTO.builder().build());
    }

    return databaseCourtRepository
        .findByCourttypeAndCourtlocation(
            documentUnit.coreData().court().type(), documentUnit.coreData().court().location())
        .defaultIfEmpty(CourtDTO.builder().build());
  }

  public Mono<DocumentUnitDTO> saveFileNumbers(
      DocumentUnitDTO documentUnitDTO, List<String> fileNumbers) {
    return fileNumberRepository
        .findAllByDocumentUnitIdAndIsDeviating(documentUnitDTO.getId(), false)
        .collectList()
        .flatMap(
            fileNumberDTOs -> {
              AtomicInteger fileNumberIndex = new AtomicInteger(0);
              List<FileNumberDTO> toSave = new ArrayList<>();
              List<FileNumberDTO> toDelete = new ArrayList<>();

              fileNumberDTOs.forEach(
                  fileNumberDTO -> {
                    if (fileNumberIndex.get() < fileNumbers.size()) {
                      fileNumberDTO.fileNumber = fileNumbers.get(fileNumberIndex.getAndIncrement());
                      fileNumberDTO.isDeviating = false;
                      toSave.add(fileNumberDTO);
                    } else {
                      toDelete.add(fileNumberDTO);
                    }
                  });

              while (fileNumberIndex.get() < fileNumbers.size()) {
                FileNumberDTO fileNumberDTO =
                    FileNumberDTO.builder()
                        .fileNumber(fileNumbers.get(fileNumberIndex.getAndIncrement()))
                        .documentUnitId(documentUnitDTO.getId())
                        .isDeviating(false)
                        .build();
                toSave.add(fileNumberDTO);
              }

              return fileNumberRepository
                  .deleteAll(toDelete)
                  .then(fileNumberRepository.saveAll(toSave).collectList())
                  .map(
                      savedFileNumberList -> {
                        documentUnitDTO.setFileNumbers(savedFileNumberList);
                        return documentUnitDTO;
                      });
            });
  }

  public Mono<DocumentUnitDTO> saveNorms(
      DocumentUnitDTO documentUnitDTO, DocumentUnit documentUnit) {

    return documentUnitNormRepository
        .findAllByDocumentUnitIdOrderById(documentUnitDTO.getId())
        .collectList()
        .flatMap(
            documentUnitNormDTOs -> {
              List<DocumentUnitNorm> documentUnitNorms = new ArrayList<>();
              if (documentUnit.contentRelatedIndexing() == null
                  || documentUnit.contentRelatedIndexing().norms() == null)
                return Mono.just(documentUnitDTO);

              documentUnitNorms.addAll(documentUnit.contentRelatedIndexing().norms());

              AtomicInteger normIndex = new AtomicInteger(0);
              List<DocumentUnitNormDTO> toSave = new ArrayList<>();
              List<DocumentUnitNormDTO> toDelete = new ArrayList<>();

              documentUnitNormDTOs.forEach(
                  documentUnitNormDTO -> {
                    int index = normIndex.getAndIncrement();
                    UUID normAbbreviationId = null;
                    if (index < documentUnitNorms.size()) {
                      DocumentUnitNorm currentNorm = documentUnitNorms.get(index);
                      if (currentNorm.normAbbreviation() != null) {
                        normAbbreviationId = currentNorm.normAbbreviation().id();
                      }
                      documentUnitNormDTO.normAbbreviationUuid = normAbbreviationId;
                      documentUnitNormDTO.singleNorm = currentNorm.singleNorm();
                      documentUnitNormDTO.dateOfVersion = currentNorm.dateOfVersion();
                      documentUnitNormDTO.dateOfRelevance = currentNorm.dateOfRelevance();
                      toSave.add(documentUnitNormDTO);
                    } else {
                      toDelete.add(documentUnitNormDTO);
                    }
                  });

              while (normIndex.get() < documentUnitNorms.size()) {
                int index = normIndex.getAndIncrement();
                DocumentUnitNorm currentNorm = documentUnitNorms.get(index);
                if (isEmptyNorm(currentNorm)) {
                  continue;
                }
                UUID normAbbreviationId = null;
                if (currentNorm.normAbbreviation() != null) {
                  normAbbreviationId = currentNorm.normAbbreviation().id();
                }
                DocumentUnitNormDTO documentUnitNormDTO =
                    DocumentUnitNormDTO.builder()
                        .normAbbreviationUuid(normAbbreviationId)
                        .singleNorm(currentNorm.singleNorm())
                        .dateOfVersion(currentNorm.dateOfVersion())
                        .dateOfRelevance(currentNorm.dateOfRelevance())
                        .documentUnitId(documentUnitDTO.getId())
                        .build();
                toSave.add(documentUnitNormDTO);
              }

              return documentUnitNormRepository
                  .deleteAll(toDelete)
                  .then(
                      documentUnitNormRepository
                          .saveAll(toSave)
                          .flatMapSequential(this::injectNormAbbreviation)
                          .collectList())
                  .map(
                      savedNormList -> {
                        documentUnitDTO.setNorms(savedNormList);
                        return documentUnitDTO;
                      });
            });
  }

  private boolean isEmptyNorm(DocumentUnitNorm currentNorm) {
    return currentNorm.singleNorm() == null
        && currentNorm.normAbbreviation() == null
        && currentNorm.dateOfRelevance() == null
        && currentNorm.dateOfVersion() == null;
  }

  private Mono<DocumentUnitDTO> saveDeviatingFileNumbers(
      DocumentUnitDTO documentUnitDTO, DocumentUnit documentUnit) {
    return fileNumberRepository
        .findAllByDocumentUnitIdAndIsDeviating(documentUnitDTO.getId(), true)
        .collectList()
        .flatMap(
            deviatingFileNumberDTOs -> {
              List<String> deviatingFileNumbers = new ArrayList<>();
              if (documentUnit.coreData() != null
                  && documentUnit.coreData().deviatingFileNumbers() != null) {
                deviatingFileNumbers.addAll(documentUnit.coreData().deviatingFileNumbers());
              }

              AtomicInteger deviatingFileNumberIndex = new AtomicInteger(0);
              List<FileNumberDTO> toSave = new ArrayList<>();
              List<FileNumberDTO> toDelete = new ArrayList<>();

              deviatingFileNumberDTOs.forEach(
                  fileNumberDTO -> {
                    if (deviatingFileNumberIndex.get() < deviatingFileNumbers.size()) {
                      fileNumberDTO.fileNumber =
                          deviatingFileNumbers.get(deviatingFileNumberIndex.getAndIncrement());
                      fileNumberDTO.isDeviating = true;
                      toSave.add(fileNumberDTO);
                    } else {
                      toDelete.add(fileNumberDTO);
                    }
                  });

              while (deviatingFileNumberIndex.get() < deviatingFileNumbers.size()) {
                FileNumberDTO fileNumberDTO =
                    FileNumberDTO.builder()
                        .fileNumber(
                            deviatingFileNumbers.get(deviatingFileNumberIndex.getAndIncrement()))
                        .documentUnitId(documentUnitDTO.getId())
                        .isDeviating(true)
                        .build();
                toSave.add(fileNumberDTO);
              }

              return fileNumberRepository
                  .deleteAll(toDelete)
                  .then(fileNumberRepository.saveAll(toSave).collectList())
                  .map(
                      savedDeviatingFileNumberList -> {
                        documentUnitDTO.setDeviatingFileNumbers(savedDeviatingFileNumberList);
                        return documentUnitDTO;
                      });
            });
  }

  private Mono<DocumentUnitDTO> saveDeviatingEcli(
      DocumentUnitDTO documentUnitDTO, DocumentUnit documentUnit) {
    return deviatingEcliRepository
        .findAllByDocumentUnitId(documentUnitDTO.getId())
        .collectList()
        .flatMap(
            deviatingEcliDTOs -> {
              List<String> deviatingEclis = new ArrayList<>();
              if (documentUnit.coreData() != null
                  && documentUnit.coreData().deviatingEclis() != null) {
                deviatingEclis.addAll(documentUnit.coreData().deviatingEclis());
              }

              AtomicInteger deviatingEcliIndex = new AtomicInteger(0);
              List<DeviatingEcliDTO> toSave = new ArrayList<>();
              List<DeviatingEcliDTO> toDelete = new ArrayList<>();

              deviatingEcliDTOs.forEach(
                  deviatingEcliDTO -> {
                    if (deviatingEcliIndex.get() < deviatingEclis.size()) {
                      deviatingEcliDTO.ecli =
                          deviatingEclis.get(deviatingEcliIndex.getAndIncrement());
                      toSave.add(deviatingEcliDTO);
                    } else {
                      toDelete.add(deviatingEcliDTO);
                    }
                  });

              while (deviatingEcliIndex.get() < deviatingEclis.size()) {
                DeviatingEcliDTO deviatingEcliDTO =
                    DeviatingEcliDTO.builder()
                        .ecli(deviatingEclis.get(deviatingEcliIndex.getAndIncrement()))
                        .documentUnitId(documentUnitDTO.getId())
                        .build();
                toSave.add(deviatingEcliDTO);
              }

              return deviatingEcliRepository
                  .deleteAll(toDelete)
                  .then(deviatingEcliRepository.saveAll(toSave).collectList())
                  .map(
                      savedDeviatingEcliList -> {
                        documentUnitDTO.setDeviatingEclis(savedDeviatingEcliList);
                        return documentUnitDTO;
                      });
            });
  }

  private Mono<DocumentUnitDTO> saveDeviatingDecisionDate(
      DocumentUnitDTO documentUnitDTO, DocumentUnit documentUnit) {
    return deviatingDecisionDateRepository
        .findAllByDocumentUnitId(documentUnitDTO.getId())
        .collectList()
        .flatMap(
            deviatingDecisionDateDTOs -> {
              List<Instant> deviatingDecisionDates = new ArrayList<>();
              if (documentUnit.coreData() != null
                  && documentUnit.coreData().deviatingDecisionDates() != null) {
                deviatingDecisionDates.addAll(documentUnit.coreData().deviatingDecisionDates());
              }

              AtomicInteger deviatingDecisionDateIndex = new AtomicInteger(0);
              List<DeviatingDecisionDateDTO> toSave = new ArrayList<>();
              List<DeviatingDecisionDateDTO> toDelete = new ArrayList<>();

              deviatingDecisionDateDTOs.forEach(
                  deviatingDecisionDateDTO -> {
                    if (deviatingDecisionDateIndex.get() < deviatingDecisionDates.size()) {
                      deviatingDecisionDateDTO =
                          DeviatingDecisionDateTransformer.enrichDTO(
                              deviatingDecisionDateDTO,
                              deviatingDecisionDates.get(
                                  deviatingDecisionDateIndex.getAndIncrement()));
                      toSave.add(deviatingDecisionDateDTO);
                    } else {
                      toDelete.add(deviatingDecisionDateDTO);
                    }
                  });

              while (deviatingDecisionDateIndex.get() < deviatingDecisionDates.size()) {
                DeviatingDecisionDateDTO deviatingDecisionDateDTO =
                    DeviatingDecisionDateDTO.builder()
                        .decisionDate(
                            deviatingDecisionDates.get(
                                deviatingDecisionDateIndex.getAndIncrement()))
                        .documentUnitId(documentUnitDTO.getId())
                        .build();
                toSave.add(deviatingDecisionDateDTO);
              }

              return deviatingDecisionDateRepository
                  .deleteAll(toDelete)
                  .then(deviatingDecisionDateRepository.saveAll(toSave).collectList())
                  .map(
                      savedDeviatingDecisionDateList -> {
                        documentUnitDTO.setDeviatingDecisionDates(savedDeviatingDecisionDateList);
                        return documentUnitDTO;
                      });
            });
  }

  private Mono<DocumentUnitDTO> saveIncorrectCourt(
      DocumentUnitDTO documentUnitDTO, DocumentUnit documentUnit) {
    return incorrectCourtRepository
        .findAllByDocumentUnitId(documentUnitDTO.getId())
        .collectList()
        .flatMap(
            incorrectCourtDTOs -> {
              List<String> incorrectCourts = new ArrayList<>();
              if (documentUnit.coreData() != null
                  && documentUnit.coreData().incorrectCourts() != null) {
                incorrectCourts.addAll(documentUnit.coreData().incorrectCourts());
              }

              AtomicInteger incorrectCourtIndex = new AtomicInteger(0);
              List<IncorrectCourtDTO> toSave = new ArrayList<>();
              List<IncorrectCourtDTO> toDelete = new ArrayList<>();

              incorrectCourtDTOs.forEach(
                  incorrectCourtDTO -> {
                    if (incorrectCourtIndex.get() < incorrectCourts.size()) {
                      incorrectCourtDTO =
                          IncorrectCourtTransformer.enrichDTO(
                              incorrectCourtDTO,
                              incorrectCourts.get(incorrectCourtIndex.getAndIncrement()));
                      toSave.add(incorrectCourtDTO);
                    } else {
                      toDelete.add(incorrectCourtDTO);
                    }
                  });

              while (incorrectCourtIndex.get() < incorrectCourts.size()) {
                IncorrectCourtDTO incorrectCourtDTO =
                    IncorrectCourtDTO.builder()
                        .court(incorrectCourts.get(incorrectCourtIndex.getAndIncrement()))
                        .documentUnitId(documentUnitDTO.getId())
                        .build();
                toSave.add(incorrectCourtDTO);
              }

              return incorrectCourtRepository
                  .deleteAll(toDelete)
                  .then(incorrectCourtRepository.saveAll(toSave).collectList())
                  .map(
                      savedIncorrectCourtList -> {
                        documentUnitDTO.setIncorrectCourts(savedIncorrectCourtList);
                        return documentUnitDTO;
                      });
            });
  }

  private Mono<DocumentUnitDTO> saveActiveCitations(
      DocumentUnitDTO documentUnitDTO, DocumentUnit documentUnit) {
    if (log.isDebugEnabled()) {
      log.debug("save active citations: {}", documentUnit.uuid());
    }

    List<ActiveCitation> activeCitations = Collections.emptyList();
    if (documentUnit.contentRelatedIndexing() != null
        && documentUnit.contentRelatedIndexing().activeCitations() != null) {
      activeCitations = documentUnit.contentRelatedIndexing().activeCitations();
    }

    return Flux.fromIterable(activeCitations)
        .filter(activeCitation -> activeCitation.getUuid() != null)
        .flatMap(
            activeCitation -> {
              if (activeCitation.hasNoValues()) {
                return unlinkDocumentUnit(
                    documentUnitDTO.getUuid(),
                    activeCitation.getUuid(),
                    DocumentationUnitLinkType.ACTIVE_CITATION);
              } else {
                return repository
                    .findByUuid(activeCitation.getUuid())
                    .filter(
                        activeCitationDocumentationUnitDTO ->
                            activeCitationDocumentationUnitDTO.getDataSource()
                                == DataSource.ACTIVE_CITATION)
                    .flatMap(
                        activeCitationDocumentationUnitDTO ->
                            updateLinkedDocumentationUnit(
                                activeCitationDocumentationUnitDTO, activeCitation))
                    .then(
                        documentationUnitLinkRepository
                            .findByParentDocumentationUnitUuidAndChildDocumentationUnitUuidAndType(
                                documentUnitDTO.getUuid(),
                                activeCitation.getUuid(),
                                DocumentationUnitLinkType.ACTIVE_CITATION)
                            .flatMap(
                                documentationUnitLinkDTO -> {
                                  UUID citationStyleUuid = null;

                                  if (activeCitation.getCitationStyle() != null) {
                                    citationStyleUuid = activeCitation.getCitationStyle().uuid();
                                  }

                                  return documentationUnitLinkRepository.save(
                                      documentationUnitLinkDTO.toBuilder()
                                          .citationStyleUuid(citationStyleUuid)
                                          .build());
                                }));
              }
            })
        .then(
            unlinkLinkedDocumentationUnit(documentUnit, DocumentationUnitLinkType.ACTIVE_CITATION))
        .then(injectActiveCitations(documentUnitDTO));
  }

  private Mono<DocumentUnitDTO> saveProceedingDecisions(
      DocumentUnitDTO documentUnitDTO, DocumentUnit documentUnit) {
    if (log.isDebugEnabled()) {
      log.debug("save precious decisions: {}", documentUnit.uuid());
    }

    List<ProceedingDecision> proceedingDecisions = Collections.emptyList();
    if (documentUnit.proceedingDecisions() != null) {
      proceedingDecisions = documentUnit.proceedingDecisions();
    }

    return Flux.fromIterable(proceedingDecisions)
        .filter(proceedingDecision -> proceedingDecision.getUuid() != null)
        .flatMap(
            proceedingDecision -> {
              if (proceedingDecision.hasNoValues()) {
                return unlinkDocumentUnit(
                    documentUnitDTO.getUuid(),
                    proceedingDecision.getUuid(),
                    DocumentationUnitLinkType.PREVIOUS_DECISION);
              } else {
                return repository
                    .findByUuid(proceedingDecision.getUuid())
                    .filter(
                        proceedingDecisionDTO ->
                            proceedingDecisionDTO.getDataSource() == DataSource.PROCEEDING_DECISION)
                    .flatMap(
                        proceedingDecisionDTO ->
                            updateLinkedDocumentationUnit(
                                proceedingDecisionDTO, proceedingDecision))
                    .then(
                        documentationUnitLinkRepository
                            .findByParentDocumentationUnitUuidAndChildDocumentationUnitUuidAndType(
                                documentUnitDTO.getUuid(),
                                proceedingDecision.getUuid(),
                                DocumentationUnitLinkType.PREVIOUS_DECISION));
              }
            })
        .then(
            unlinkLinkedDocumentationUnit(
                documentUnit, DocumentationUnitLinkType.PREVIOUS_DECISION))
        .then(injectProceedingDecisions(documentUnitDTO));
  }

  private Mono<DocumentUnitDTO> saveProcedure(
      DocumentUnitDTO documentUnitDTO, DocumentUnit documentUnit) {

    String documentationOfficeLabel = documentUnit.coreData().documentationOffice().label();
    Optional.ofNullable(documentUnit.coreData().procedure())
        .map(procedure -> findOrCreateProcedure(procedure, documentationOfficeLabel))
        .ifPresent(
            procedureDTO -> {
              if (!areCurrentlyLinked(documentUnitDTO, procedureDTO)) {
                procedureLinkRepository.save(
                    JPAProcedureLinkDTO.builder()
                        .procedureDTO(procedureDTO)
                        .documentationUnitId(documentUnitDTO.uuid)
                        .build());
              }

              documentUnitDTO.setProcedure(procedureDTO);
            });

    return Mono.just(documentUnitDTO);
  }

  private JPAProcedureDTO findOrCreateProcedure(
      Procedure procedure, String documentationOfficeLabel) {
    JPADocumentationOfficeDTO documentationOfficeDTO =
        documentationOfficeRepository.findByLabel(documentationOfficeLabel);

    return Optional.ofNullable(
            procedureRepository.findByLabelAndDocumentationOfficeOrderByCreatedAtDesc(
                procedure.label(), documentationOfficeDTO))
        .orElseGet(
            () ->
                procedureRepository.save(
                    JPAProcedureDTO.builder()
                        .label(procedure.label())
                        .documentationOffice(documentationOfficeDTO)
                        .build()));
  }

  private Boolean areCurrentlyLinked(
      DocumentUnitDTO documentUnitDTO, JPAProcedureDTO procedureDTO) {
    return Optional.ofNullable(
            procedureLinkRepository.findFirstByDocumentationUnitIdOrderByCreatedAtDesc(
                documentUnitDTO.uuid))
        .map(linkDTO -> linkDTO.getProcedureDTO().equals(procedureDTO))
        .orElse(false);
  }

  private Mono<DocumentUnit> unlinkLinkedDocumentationUnit(
      DocumentUnit documentUnit, DocumentationUnitLinkType type) {
    if (log.isDebugEnabled()) {
      log.debug("unlink linked document unit of type {} for parent {}", type, documentUnit.uuid());
    }

    return findAllLinkedDocumentUnitsByParentDocumentUnitUuidAndType(documentUnit.uuid(), type)
        .filter(
            linkedDocumentationUnit ->
                filterUnlinkedDocumentUnit(documentUnit, linkedDocumentationUnit, type))
        .flatMap(
            linkedDocumentationUnit ->
                unlinkDocumentUnit(documentUnit.uuid(), linkedDocumentationUnit.getUuid(), type)
                    .thenReturn(linkedDocumentationUnit.getUuid()))
        .flatMap(this::deleteIfOrphanedLinkedDocumentationUnit)
        .then()
        .thenReturn(documentUnit);
  }

  private boolean filterUnlinkedDocumentUnit(
      DocumentUnit documentUnit,
      LinkedDocumentationUnit linkedDocumentationUnit,
      DocumentationUnitLinkType type) {
    if (type == DocumentationUnitLinkType.ACTIVE_CITATION) {
      return documentUnit.contentRelatedIndexing() == null
          || documentUnit.contentRelatedIndexing().activeCitations() == null
          || documentUnit.contentRelatedIndexing().activeCitations().isEmpty()
          || documentUnit.contentRelatedIndexing().activeCitations().stream()
              .map(LinkedDocumentationUnit::getUuid)
              .noneMatch(linkedUuid -> linkedDocumentationUnit.getUuid().equals(linkedUuid));
    } else if (type == DocumentationUnitLinkType.PREVIOUS_DECISION) {
      return documentUnit.proceedingDecisions() == null
          || documentUnit.proceedingDecisions().isEmpty()
          || documentUnit.proceedingDecisions().stream()
              .map(LinkedDocumentationUnit::getUuid)
              .noneMatch(linkedUuid -> linkedDocumentationUnit.getUuid().equals(linkedUuid));
    } else {
      throw new DocumentationUnitException("Couldn't filter for unknown link type '" + type + "'");
    }
  }

  private Mono<DocumentUnitMetadataDTO> updateLinkedDocumentationUnit(
      DocumentUnitDTO linkedDocumentationUnitDTO, LinkedDocumentationUnit linkedDocumentationUnit) {
    return enrichDocumentType(linkedDocumentationUnitDTO, linkedDocumentationUnit.getDocumentType())
        .map(
            documentUnitDTO -> {
              DocumentUnitDTOBuilder<?, ?> builder = documentUnitDTO.toBuilder();

              if (linkedDocumentationUnit.getCourt() != null) {
                builder
                    .courtLocation(linkedDocumentationUnit.getCourt().location())
                    .courtType(linkedDocumentationUnit.getCourt().type());
              } else {
                builder.courtLocation(null).courtType(null);
              }

              return builder
                  .decisionDate(linkedDocumentationUnit.getDecisionDate())
                  .dateKnown(linkedDocumentationUnit.isDateKnown())
                  .build();
            })
        .flatMap(metadataRepository::save)
        .flatMap(
            documentUnitDTO ->
                saveFileNumbers(
                    documentUnitDTO,
                    linkedDocumentationUnit.getFileNumber() == null
                        ? Collections.emptyList()
                        : List.of(linkedDocumentationUnit.getFileNumber())));
  }

  @Override
  public Mono<DocumentUnit> attachFile(
      UUID documentUnitUuid, String fileUuid, String type, String fileName) {
    return repository
        .findByUuid(documentUnitUuid)
        .map(
            documentUnitDTO -> {
              documentUnitDTO.setS3path(fileUuid);
              documentUnitDTO.setFilename(fileName);
              documentUnitDTO.setFiletype(type);
              documentUnitDTO.setFileuploadtimestamp(Instant.now());

              return documentUnitDTO;
            })
        .flatMap(repository::save)
        .map(DocumentUnitTransformer::transformDTO);
  }

  @Override
  public Mono<DocumentUnit> removeFile(UUID documentUnitId) {
    return repository
        .findByUuid(documentUnitId)
        .map(
            documentUnitDTO -> {
              documentUnitDTO.setS3path(null);
              documentUnitDTO.setFilename(null);
              documentUnitDTO.setFiletype(null);
              documentUnitDTO.setFileuploadtimestamp(null);

              return documentUnitDTO;
            })
        .flatMap(repository::save)
        .map(DocumentUnitTransformer::transformDTO);
  }

  @Override
  public Mono<Void> delete(DocumentUnit documentUnit) {
    return repository
        .findByUuid(documentUnit.uuid())
        .flatMap(documentUnitDTO -> repository.deleteById(documentUnitDTO.getId()));
  }

  private Mono<DocumentUnitDTO> injectAdditionalInformation(DocumentUnitDTO documentUnitDTO) {
    if (log.isDebugEnabled()) {
      log.debug("inject additional information: {}", documentUnitDTO.getUuid());
    }

    return injectMetadataInformation(documentUnitDTO)
        .flatMap(this::injectDeviatingFileNumbers)
        .flatMap(this::injectProceedingDecisions)
        .flatMap(this::injectDeviatingEclis)
        .flatMap(this::injectDeviatingDecisionDates)
        .flatMap(this::injectIncorrectCourt)
        .flatMap(this::injectKeywords)
        .flatMap(this::injectNorms)
        .flatMap(this::injectFieldsOfLaw)
        .flatMap(this::injectDocumentationOffice)
        .flatMap(this::injectStatus)
        .flatMap(this::injectActiveCitations)
        .flatMap(this::injectProcedure)
        .flatMap(this::injectPreviousProcedures);
  }

  private <T extends DocumentUnitMetadataDTO> Mono<T> injectMetadataInformation(
      T documentUnitMetadataDTO) {

    if (log.isDebugEnabled()) {
      log.debug("inject metadata information: {}", documentUnitMetadataDTO.getUuid());
    }

    return injectFileNumbers(documentUnitMetadataDTO).flatMap(this::injectDocumentType);
  }

  private Mono<DocumentUnitDTO> injectProceedingDecisions(DocumentUnitDTO documentUnitDTO) {
    return documentationUnitLinkRepository
        .findAllByParentDocumentationUnitUuidAndTypeOrderByIdAsc(
            documentUnitDTO.getUuid(), DocumentationUnitLinkType.PREVIOUS_DECISION)
        .map(DocumentationUnitLinkDTO::getChildDocumentationUnitUuid)
        .flatMap(metadataRepository::findByUuid)
        .flatMap(this::injectMetadataInformation)
        .collectList()
        .map(
            proceedingDecisionDTOs -> {
              documentUnitDTO.setProceedingDecisions(proceedingDecisionDTOs);
              return documentUnitDTO;
            });
  }

  private Mono<DocumentUnitDTO> injectActiveCitations(DocumentUnitDTO parentDTO) {
    if (log.isDebugEnabled()) {
      log.debug("inject active citations: {}", parentDTO.getUuid());
    }

    return documentationUnitLinkRepository
        .findAllByParentDocumentationUnitUuidAndTypeOrderByIdAsc(
            parentDTO.getUuid(), DocumentationUnitLinkType.ACTIVE_CITATION)
        .map(DocumentationUnitLinkDTO::getChildDocumentationUnitUuid)
        .flatMapSequential(metadataRepository::findByUuid)
        .flatMapSequential(this::injectMetadataInformation)
        .flatMapSequential(childDTO -> injectCitationStyle(parentDTO.getUuid(), childDTO))
        .collectList()
        .map(
            activeCitations -> {
              parentDTO.setActiveCitations(activeCitations);
              return parentDTO;
            });
  }

  private Mono<ActiveCitation> injectCitationStyle(
      UUID parentUUID, DocumentUnitMetadataDTO documentUnitMetadataDTO) {
    if (log.isDebugEnabled()) {
      log.debug(
          "inject citation style in child '{}' for parent '{}'",
          documentUnitMetadataDTO.getUuid(),
          parentUUID);
    }

    return documentationUnitLinkRepository
        .findByParentDocumentationUnitUuidAndChildDocumentationUnitUuidAndType(
            parentUUID,
            documentUnitMetadataDTO.getUuid(),
            DocumentationUnitLinkType.ACTIVE_CITATION)
        .flatMap(
            documentationUnitLinkDTO ->
                citationStyleRepository
                    .findByUuid(documentationUnitLinkDTO.getCitationStyleUuid())
                    .map(
                        citationStyleDTO ->
                            documentationUnitLinkDTO.toBuilder()
                                .citationStyleDTO(citationStyleDTO)
                                .build())
                    .defaultIfEmpty(documentationUnitLinkDTO))
        .map(
            documentationUnitLinkDTO ->
                ActiveCitationTransformer.transformToDomain(
                    documentUnitMetadataDTO, documentationUnitLinkDTO));
  }

  private <T extends DocumentUnitMetadataDTO> Mono<T> injectFileNumbers(T documentUnitMetadataDTO) {
    return fileNumberRepository
        .findAllByDocumentUnitId(documentUnitMetadataDTO.getId())
        .collectList()
        .map(
            fileNumbers -> {
              documentUnitMetadataDTO.setFileNumbers(
                  fileNumbers.stream()
                      .filter(fileNumberDTO -> !fileNumberDTO.getIsDeviating())
                      .toList());
              return documentUnitMetadataDTO;
            });
  }

  private Mono<DocumentUnitDTO> injectDeviatingFileNumbers(DocumentUnitDTO documentUnitDTO) {
    return fileNumberRepository
        .findAllByDocumentUnitId(documentUnitDTO.getId())
        .collectList()
        .flatMap(
            fileNumbers -> {
              documentUnitDTO.setDeviatingFileNumbers(
                  fileNumbers.stream().filter(FileNumberDTO::getIsDeviating).toList());
              return Mono.just(documentUnitDTO);
            });
  }

  private Mono<DocumentUnitDTO> injectDeviatingEclis(DocumentUnitDTO documentUnitDTO) {
    return deviatingEcliRepository
        .findAllByDocumentUnitId(documentUnitDTO.getId())
        .collectList()
        .flatMap(
            deviatingEcliDTOs -> {
              documentUnitDTO.setDeviatingEclis(deviatingEcliDTOs);
              return Mono.just(documentUnitDTO);
            });
  }

  private Mono<DocumentUnitDTO> injectDeviatingDecisionDates(DocumentUnitDTO documentUnitDTO) {
    return deviatingDecisionDateRepository
        .findAllByDocumentUnitId(documentUnitDTO.getId())
        .collectList()
        .flatMap(
            deviatingDecisionDateDTOs -> {
              documentUnitDTO.setDeviatingDecisionDates(deviatingDecisionDateDTOs);
              return Mono.just(documentUnitDTO);
            });
  }

  private <T extends DocumentUnitMetadataDTO> Mono<T> injectDocumentType(
      T documentUnitMetadataDTO) {
    if (documentUnitMetadataDTO.getDocumentTypeId() == null) {
      return Mono.just(documentUnitMetadataDTO);
    }
    return databaseDocumentTypeRepository
        .findById(documentUnitMetadataDTO.getDocumentTypeId())
        .defaultIfEmpty(DocumentTypeDTO.builder().build())
        .map(
            documentTypeDTO -> {
              documentUnitMetadataDTO.setDocumentTypeDTO(documentTypeDTO);
              return documentUnitMetadataDTO;
            });
  }

  private Mono<DocumentUnitDTO> injectIncorrectCourt(DocumentUnitDTO documentUnitDTO) {
    return incorrectCourtRepository
        .findAllByDocumentUnitId(documentUnitDTO.getId())
        .collectList()
        .map(
            incorrectCourtDTOs -> {
              documentUnitDTO.setIncorrectCourts(incorrectCourtDTOs);
              return documentUnitDTO;
            });
  }

  private Mono<DocumentUnitDTO> injectFieldsOfLaw(DocumentUnitDTO documentUnitDTO) {
    return documentUnitFieldsOfLawRepository
        .findAllByDocumentUnitId(documentUnitDTO.getId())
        .map(DocumentUnitFieldsOfLawDTO::fieldOfLawId)
        .collectList()
        .flatMapMany(fieldOfLawRepository::findAllById)
        .collectList()
        .map(fieldsOfLaw -> documentUnitDTO.toBuilder().fieldsOfLaw(fieldsOfLaw).build());
  }

  private Mono<DocumentUnitDTO> injectKeywords(DocumentUnitDTO documentUnitDTO) {
    return keywordRepository
        .findAllByDocumentUnitIdOrderById(documentUnitDTO.getId())
        .collectList()
        .map(
            keywordDTO -> {
              documentUnitDTO.setKeywords(keywordDTO);
              return documentUnitDTO;
            });
  }

  private Mono<DocumentUnitDTO> injectNorms(DocumentUnitDTO documentUnitDTO) {
    return documentUnitNormRepository
        .findAllByDocumentUnitIdOrderById(documentUnitDTO.getId())
        .flatMapSequential(this::injectNormAbbreviation)
        .collectList()
        .map(
            documentUnitNormDTOs -> {
              documentUnitDTO.setNorms(documentUnitNormDTOs);
              return documentUnitDTO;
            });
  }

  private Mono<DocumentUnitNormDTO> injectNormAbbreviation(
      DocumentUnitNormDTO documentUnitNormDTO) {
    if (documentUnitNormDTO.getNormAbbreviationUuid() == null) {
      return Mono.just(documentUnitNormDTO);
    }

    return normAbbreviationRepository
        .findById(documentUnitNormDTO.getNormAbbreviationUuid())
        .defaultIfEmpty(NormAbbreviationDTO.builder().build())
        .map(
            normAbbreviationDTO -> {
              documentUnitNormDTO.setNormAbbreviation(normAbbreviationDTO);
              return documentUnitNormDTO;
            });
  }

  private <T extends DocumentUnitMetadataDTO> Mono<T> injectDocumentationOffice(
      T documentUnitMetadataDTO) {

    Optional.ofNullable(documentUnitMetadataDTO.getDocumentationOfficeId())
        .flatMap(documentationOfficeRepository::findById)
        .ifPresent(documentUnitMetadataDTO::setDocumentationOffice);

    return Mono.just(documentUnitMetadataDTO);
  }

  private <T extends DocumentUnitMetadataDTO> Mono<T> injectStatus(T documentUnitDTO) {
    return Mono.just(documentUnitDTO)
        .flatMap(
            dto ->
                databaseDocumentUnitStatusRepository
                    .findFirstByDocumentUnitIdOrderByCreatedAtDesc(documentUnitDTO.getUuid())
                    .map(
                        statusDTO -> {
                          dto.setStatus(
                              DocumentUnitStatus.builder()
                                  .publicationStatus(statusDTO.getPublicationStatus())
                                  .withError(statusDTO.isWithError())
                                  .build());
                          return dto;
                        })
                    .switchIfEmpty(
                        Mono.defer(
                            () -> {
                              dto.setStatus(
                                  DocumentUnitStatus.builder()
                                      .publicationStatus(PublicationStatus.PUBLISHED)
                                      .withError(false)
                                      .build());
                              return Mono.just(dto);
                            })));
  }

  private <T extends DocumentUnitMetadataDTO> Mono<T> injectProcedure(T documentUnitDTO) {
    Optional.ofNullable(
            procedureLinkRepository.findFirstByDocumentationUnitIdOrderByCreatedAtDesc(
                documentUnitDTO.uuid))
        .map(JPAProcedureLinkDTO::getProcedureDTO)
        .ifPresent(documentUnitDTO::setProcedure);

    return Mono.just(documentUnitDTO);
  }

  private <T extends DocumentUnitMetadataDTO> Mono<T> injectPreviousProcedures(T documentUnitDTO) {

    List<String> previousProcedures =
        procedureLinkRepository
            .findAllByDocumentationUnitIdOrderByCreatedAtDesc(documentUnitDTO.uuid)
            .stream()
            .skip(1)
            .map(JPAProcedureLinkDTO::getProcedureDTO)
            .map(JPAProcedureDTO::getLabel)
            .toList();

    documentUnitDTO.setPreviousProcedures(previousProcedures.isEmpty() ? null : previousProcedures);

    return Mono.just(documentUnitDTO);
  }

  @Override
  public Flux<LinkedDocumentationUnit> searchByLinkedDocumentationUnit(
      LinkedDocumentationUnit linkedDocumentationUnit, Pageable pageable) {

    return Mono.zip(
            extractDocumentUnitDTOIdsViaFileNumber(linkedDocumentationUnit).collectList(),
            extractDocumentTypeDTOId(linkedDocumentationUnit))
        .flatMapMany(
            tuple -> {
              Long[] documentUnitDTOIdsViaFileNumber =
                  convertListToArrayOrReturnNull(tuple.getT1());
              if (documentUnitDTOIdsViaFileNumber == null
                  && linkedDocumentationUnit.getFileNumber() != null) return Flux.empty();

              Long documentTypeDTOId = tuple.getT2() == -1L ? null : tuple.getT2();
              if (documentTypeDTOId == null && linkedDocumentationUnit.getDocumentType() != null)
                return Flux.empty();

              return metadataRepository.searchByLinkedDocumentationUnit(
                  extractCourtType(linkedDocumentationUnit),
                  extractCourtLocation(linkedDocumentationUnit),
                  extractDecisionDate(linkedDocumentationUnit),
                  documentUnitDTOIdsViaFileNumber,
                  documentTypeDTOId,
                  pageable.getPageSize(),
                  pageable.getOffset());
            })
        .flatMapSequential(this::injectAdditionalInformation)
        .map(
            documentUnitMetadataDTO ->
                LinkedDocumentationUnitTransformer.transformToDomain(
                    documentUnitMetadataDTO, null));
  }

  @Override
  public Mono<Long> countSearchByLinkedDocumentationUnit(
      LinkedDocumentationUnit linkedDocumentationUnit) {
    return Mono.zip(
            extractDocumentUnitDTOIdsViaFileNumber(linkedDocumentationUnit).collectList(),
            extractDocumentTypeDTOId(linkedDocumentationUnit))
        .flatMap(
            tuple -> {
              Long[] documentUnitDTOIdsViaFileNumber =
                  convertListToArrayOrReturnNull(tuple.getT1());
              if (documentUnitDTOIdsViaFileNumber == null
                  && linkedDocumentationUnit.getFileNumber() != null) return Mono.just(0L);

              Long documentTypeDTOId = tuple.getT2() == -1L ? null : tuple.getT2();
              if (documentTypeDTOId == null && linkedDocumentationUnit.getDocumentType() != null)
                return Mono.just(0L);

              return metadataRepository.countSearchByLinkedDocumentationUnit(
                  extractCourtType(linkedDocumentationUnit),
                  extractCourtLocation(linkedDocumentationUnit),
                  extractDecisionDate(linkedDocumentationUnit),
                  documentUnitDTOIdsViaFileNumber,
                  documentTypeDTOId);
            });
  }

  private String extractCourtType(LinkedDocumentationUnit linkedDocumentationUnit) {
    return Optional.ofNullable(linkedDocumentationUnit.getCourt()).map(Court::type).orElse(null);
  }

  private String extractCourtLocation(LinkedDocumentationUnit linkedDocumentationUnit) {
    return Optional.ofNullable(linkedDocumentationUnit.getCourt())
        .map(Court::location)
        .orElse(null);
  }

  private Instant extractDecisionDate(LinkedDocumentationUnit linkedDocumentationUnit) {
    return Optional.ofNullable(linkedDocumentationUnit.getDecisionDate())
        .map(date -> date.atZone(ZoneId.of("UTC")).toInstant())
        .orElse(null);
  }

  private Flux<Long> extractDocumentUnitDTOIdsViaFileNumber(
      LinkedDocumentationUnit linkedDocumentationUnit) {
    return fileNumberRepository
        .findByFileNumber(linkedDocumentationUnit.getFileNumber())
        .map(FileNumberDTO::getDocumentUnitId);
  }

  private Mono<Long> extractDocumentTypeDTOId(LinkedDocumentationUnit linkedDocumentationUnit) {
    return Mono.justOrEmpty(linkedDocumentationUnit.getDocumentType())
        .map(DocumentType::jurisShortcut)
        .flatMap(databaseDocumentTypeRepository::findByJurisShortcut)
        .mapNotNull(DocumentTypeDTO::getId)
        .switchIfEmpty(Mono.just(-1L));
  }

  private Long[] convertListToArrayOrReturnNull(List<Long> list) {
    return list.isEmpty() ? null : list.toArray(Long[]::new);
  }

  public Page<DocumentationUnitSearchEntry> searchByDocumentUnitSearchInput(
      Pageable pageable,
      DocumentationOffice documentationOffice,
      DocumentUnitSearchInput searchInput) {
    if (log.isDebugEnabled()) {
      log.debug("Find by overview search: {}, {}", documentationOffice, searchInput);
    }

    JPADocumentationOfficeDTO documentationOfficeDTO =
        documentationOfficeRepository.findByLabel(documentationOffice.label());

    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<DocumentationUnitSearchEntryDTO> query =
        builder.createQuery(DocumentationUnitSearchEntryDTO.class);
    Root<DocumentationUnitSearchEntryDTO> root = query.from(DocumentationUnitSearchEntryDTO.class);

    Predicate[] predicates = getPredicates(searchInput, documentationOfficeDTO, builder, root);
    query.where(predicates);
    query.orderBy(builder.desc(root.get("id")));

    CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
    Root<DocumentationUnitSearchEntryDTO> root1 =
        countQuery.from(DocumentationUnitSearchEntryDTO.class);

    predicates = getPredicates(searchInput, documentationOfficeDTO, builder, root1);
    countQuery.select(builder.count(root1)).where(predicates);

    List<DocumentationUnitSearchEntryDTO> resultList =
        entityManager
            .createQuery(query)
            .setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
            .setMaxResults(pageable.getPageSize())
            .getResultList();

    Long count = entityManager.createQuery(countQuery).getSingleResult();

    List<DocumentationUnitSearchEntry> searchEntries =
        resultList.stream().map(DocumentationUnitSearchEntryTransformer::transferDTO).toList();
    return new PageImpl<>(searchEntries, pageable, count);
  }

  @NotNull
  private static Predicate[] getPredicates(
      DocumentUnitSearchInput searchInput,
      JPADocumentationOfficeDTO documentationOfficeDTO,
      CriteriaBuilder builder,
      Root<DocumentationUnitSearchEntryDTO> root) {
    List<Predicate> restrictions = new ArrayList<>();

    if (searchInput.documentNumberOrFileNumber() != null) {
      String pattern = "%" + searchInput.documentNumberOrFileNumber().toLowerCase() + "%";
      Predicate documentNumberLike =
          builder.like(builder.lower(root.get("documentNumber")), pattern);
      Predicate firstFileNumberLike =
          builder.like(builder.lower(root.get("firstFileNumber")), pattern);
      restrictions.add(builder.or(documentNumberLike, firstFileNumberLike));
    }

    if (searchInput.court() != null) {
      if (searchInput.court().location() != null) {
        restrictions.add(builder.equal(root.get("courtLocation"), searchInput.court().location()));
      }

      if (searchInput.court().type() != null) {
        restrictions.add(builder.equal(root.get("courtType"), searchInput.court().type()));
      }
    }

    if (searchInput.decisionDate() != null) {
      if (searchInput.decisionDateEnd() != null) {
        restrictions.add(
            builder.between(
                root.get("decisionDate"),
                searchInput.decisionDate(),
                searchInput.decisionDateEnd()));
      } else {
        restrictions.add(builder.equal(root.get("decisionDate"), searchInput.decisionDate()));
      }
    }

    Predicate myDocOffice =
        builder.equal(root.get("documentationOfficeId"), documentationOfficeDTO.getId());

    if (searchInput.status() != null && searchInput.status().withError() == true) {
      Predicate statusWithError =
          builder.equal(root.get("withError"), searchInput.status().withError());
      restrictions.add(builder.and(myDocOffice, statusWithError));
    }

    if (searchInput.status() != null && searchInput.status().publicationStatus() != null) {
      Predicate status =
          builder.equal(root.get("publicationStatus"), searchInput.status().publicationStatus());
      if (searchInput.status().publicationStatus() == PublicationStatus.PUBLISHED) {
        status =
            root.get("publicationStatus")
                .in(
                    PublicationStatus.PUBLISHED,
                    PublicationStatus.TEST_DOC_UNIT,
                    PublicationStatus.JURIS_PUBLISHED);
      }
      if (searchInput.myDocOfficeOnly()
          || searchInput.status().publicationStatus() == PublicationStatus.UNPUBLISHED) {
        restrictions.add(builder.and(myDocOffice, status));
      } else {
        restrictions.add(status);
      }
    } else {
      Predicate status =
          root.get("publicationStatus")
              .in(
                  PublicationStatus.PUBLISHED,
                  PublicationStatus.PUBLISHING,
                  PublicationStatus.TEST_DOC_UNIT,
                  PublicationStatus.JURIS_PUBLISHED);
      if (searchInput.myDocOfficeOnly()) {
        restrictions.add(myDocOffice);
      } else {
        restrictions.add(builder.or(myDocOffice, status));
      }
    }

    return restrictions.toArray(new Predicate[0]);
  }

  @Override
  public Flux<LinkedDocumentationUnit> findAllLinkedDocumentUnitsByParentDocumentUnitUuidAndType(
      UUID parentDocumentUnitUuid, DocumentationUnitLinkType type) {
    if (log.isDebugEnabled()) {
      log.debug(
          "find all linked documentation units by parent documentation uuid '{}' and type '{}'",
          parentDocumentUnitUuid,
          type);
    }

    return documentationUnitLinkRepository
        .findAllByParentDocumentationUnitUuidAndTypeOrderByIdAsc(parentDocumentUnitUuid, type)
        .flatMap(
            linkDTO ->
                metadataRepository
                    .findByUuid(linkDTO.getChildDocumentationUnitUuid())
                    .flatMap(this::injectAdditionalInformation)
                    .zipWith(Mono.just(linkDTO)))
        .map(
            tuple ->
                LinkedDocumentationUnitTransformer.transformToDomain(tuple.getT1(), tuple.getT2()));
  }

  private Mono<DocumentUnitDTO> filterUnlinkedDocumentUnit(DocumentUnitDTO documentUnitDTO) {
    if (log.isDebugEnabled()) {
      log.debug("filter unlinked documentation unit: {}", documentUnitDTO.getUuid());
    }

    return documentationUnitLinkRepository
        .existsByChildDocumentationUnitUuid(documentUnitDTO.getUuid())
        .filter(isLinked -> !isLinked)
        .map(isLinked -> documentUnitDTO);
  }

  private Mono<DocumentUnitMetadataDTO> injectAdditionalInformation(
      DocumentUnitMetadataDTO documentUnitMetadataDTO) {
    if (log.isDebugEnabled()) {
      log.debug(
          "inject additional information for metadata documentation unit: {}",
          documentUnitMetadataDTO.getUuid());
    }

    return injectFileNumbers(documentUnitMetadataDTO)
        .flatMap(this::injectDocumentType)
        .flatMap(this::injectDocumentationOffice);
  }

  @Override
  public Mono<DocumentationUnitLink> linkDocumentUnits(
      UUID parentDocumentUnitUuid, UUID childDocumentUnitUuid, DocumentationUnitLinkType type) {
    if (log.isDebugEnabled()) {
      log.debug(
          "link documentation unitst: {}, {}, {}",
          parentDocumentUnitUuid,
          childDocumentUnitUuid,
          type);
    }

    if (parentDocumentUnitUuid == null || childDocumentUnitUuid == null || type == null) {
      return Mono.empty();
    }

    return documentationUnitLinkRepository
        .findByParentDocumentationUnitUuidAndChildDocumentationUnitUuidAndType(
            parentDocumentUnitUuid, childDocumentUnitUuid, type)
        .switchIfEmpty(
            documentationUnitLinkRepository.save(
                DocumentationUnitLinkDTO.builder()
                    .parentDocumentationUnitUuid(parentDocumentUnitUuid)
                    .childDocumentationUnitUuid(childDocumentUnitUuid)
                    .type(type)
                    .build()))
        .map(DocumentationUnitLinkTransformer::transferToDomain);
  }

  @Override
  public Mono<Void> unlinkDocumentUnit(
      UUID parentDocumentationUnitUuid,
      UUID childDocumentationUnitUuid,
      DocumentationUnitLinkType type) {
    if (log.isDebugEnabled()) {
      log.debug(
          "unlink document unit: {}, {}, {}",
          parentDocumentationUnitUuid,
          childDocumentationUnitUuid,
          type);
    }

    return documentationUnitLinkRepository
        .findByParentDocumentationUnitUuidAndChildDocumentationUnitUuidAndType(
            parentDocumentationUnitUuid, childDocumentationUnitUuid, type)
        .map(DocumentationUnitLinkDTO::getId)
        .flatMap(documentationUnitLinkRepository::deleteById);
  }

  @Override
  public Mono<Long> countLinksByChildDocumentUnitUuid(UUID childDocumentUnitUuid) {
    if (log.isDebugEnabled()) {
      log.debug("count links by child documentation unit uuid: {}", childDocumentUnitUuid);
    }

    return documentationUnitLinkRepository.countByChildDocumentationUnitUuid(childDocumentUnitUuid);
  }

  @Override
  public Mono<Long> count() {
    return metadataRepository.count();
  }

  @Override
  public Mono<Long> countSearchByDocumentUnitSearchInput(
      DocumentationOffice documentationOffice, DocumentUnitSearchInput searchInput) {
    if (log.isDebugEnabled()) {
      log.debug("count for overview search: {}, {}", documentationOffice, searchInput);
    }

    return Mono.just(documentationOfficeRepository.findByLabel(documentationOffice.label()))
        .flatMap(
            docOffice ->
                metadataRepository.countSearchByDocumentUnitSearchInput(
                    docOffice.getId(),
                    searchInput.documentNumberOrFileNumber(),
                    searchInput.court() == null ? null : searchInput.court().type(),
                    searchInput.court() == null ? null : searchInput.court().location(),
                    searchInput.decisionDate(),
                    searchInput.status() == null ? null : searchInput.status().publicationStatus(),
                    searchInput.myDocOfficeOnly()));
  }

  @Override
  public Mono<Void> deleteIfOrphanedLinkedDocumentationUnit(UUID documentUnitUuid) {
    if (log.isDebugEnabled()) {
      log.debug("delete if orphaned linked documentation unit: {}", documentUnitUuid);
    }

    return repository
        .findByUuid(documentUnitUuid)
        .filter(
            childDocumentUnit ->
                DataSource.PROCEEDING_DECISION == childDocumentUnit.getDataSource()
                    || DataSource.ACTIVE_CITATION == childDocumentUnit.getDataSource())
        .flatMap(this::filterUnlinkedDocumentUnit)
        .flatMap(repository::delete);
  }
}
