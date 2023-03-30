package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.CourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseFieldOfLawRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.LegalEffect;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.StateDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.StateRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.proceedingdecision.DatabaseProceedingDecisionLinkRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.proceedingdecision.ProceedingDecisionLinkDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DeviatingDecisionDateTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentUnitTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.IncorrectCourtTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ProceedingDecisionTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DataSource;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitListEntry;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.ProceedingDecision;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class PostgresDocumentUnitRepositoryImpl implements DocumentUnitRepository {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(PostgresDocumentUnitRepositoryImpl.class);

  private final DatabaseDocumentUnitRepository repository;
  private final DatabaseDocumentUnitMetadataRepository metadataRepository;
  private final FileNumberRepository fileNumberRepository;
  private final DeviatingEcliRepository deviatingEcliRepository;
  private final DatabaseDeviatingDecisionDateRepository deviatingDecisionDateRepository;
  private final DatabaseProceedingDecisionLinkRepository proceedingDecisionLinkRepository;
  private final DatabaseIncorrectCourtRepository incorrectCourtRepository;
  private final CourtRepository courtRepository;
  private final StateRepository stateRepository;
  private final DocumentTypeRepository documentTypeRepository;
  private final DatabaseFieldOfLawRepository fieldOfLawRepository;
  private final DatabaseDocumentUnitFieldsOfLawRepository documentUnitFieldsOfLawRepository;
  private final DatabaseKeywordRepository keywordRepository;

  public PostgresDocumentUnitRepositoryImpl(
      DatabaseDocumentUnitRepository repository,
      DatabaseDocumentUnitMetadataRepository metadataRepository,
      FileNumberRepository fileNumberRepository,
      DeviatingEcliRepository deviatingEcliRepository,
      DatabaseProceedingDecisionLinkRepository proceedingDecisionLinkRepository,
      DatabaseDeviatingDecisionDateRepository deviatingDecisionDateRepository,
      DatabaseIncorrectCourtRepository incorrectCourtRepository,
      CourtRepository courtRepository,
      StateRepository stateRepository,
      DocumentTypeRepository documentTypeRepository,
      DatabaseFieldOfLawRepository fieldOfLawRepository,
      DatabaseDocumentUnitFieldsOfLawRepository documentUnitFieldsOfLawRepository,
      DatabaseKeywordRepository keywordRepository) {

    this.repository = repository;
    this.metadataRepository = metadataRepository;
    this.proceedingDecisionLinkRepository = proceedingDecisionLinkRepository;
    this.fileNumberRepository = fileNumberRepository;
    this.deviatingEcliRepository = deviatingEcliRepository;
    this.deviatingDecisionDateRepository = deviatingDecisionDateRepository;
    this.incorrectCourtRepository = incorrectCourtRepository;
    this.courtRepository = courtRepository;
    this.stateRepository = stateRepository;
    this.documentTypeRepository = documentTypeRepository;
    this.fieldOfLawRepository = fieldOfLawRepository;
    this.documentUnitFieldsOfLawRepository = documentUnitFieldsOfLawRepository;
    this.keywordRepository = keywordRepository;
  }

  @Override
  public Mono<DocumentUnit> findByDocumentNumber(String documentNumber) {
    return repository
        .findByDocumentnumber(documentNumber)
        .flatMap(this::injectAdditionalInformation)
        .map(DocumentUnitTransformer::transformDTO);
  }

  @Override
  public Mono<DocumentUnit> findByUuid(UUID uuid) {
    return repository
        .findByUuid(uuid)
        .flatMap(this::injectAdditionalInformation)
        .map(DocumentUnitTransformer::transformDTO);
  }

  @Override
  public Mono<DocumentUnit> createNewDocumentUnit(String documentNumber) {
    return metadataRepository
        .save(
            DocumentUnitMetadataDTO.builder()
                .uuid(UUID.randomUUID())
                .creationtimestamp(Instant.now())
                .documentnumber(documentNumber)
                .dataSource(DataSource.NEURIS)
                .legalEffect(LegalEffect.NOT_SPECIFIED.getLabel())
                .build())
        .map(DocumentUnitTransformer::transformMetadataToDomain);
  }

  @Override
  @Transactional(transactionManager = "connectionFactoryTransactionManager")
  public Mono<DocumentUnit> save(DocumentUnit documentUnit) {
    return repository
        .findByUuid(documentUnit.uuid())
        .flatMap(documentUnitDTO -> enrichDocumentType(documentUnitDTO, documentUnit))
        .flatMap(documentUnitDTO -> enrichLegalEffect(documentUnitDTO, documentUnit))
        .flatMap(documentUnitDTO -> enrichRegion(documentUnitDTO, documentUnit))
        .map(documentUnitDTO -> DocumentUnitTransformer.enrichDTO(documentUnitDTO, documentUnit))
        .flatMap(repository::save)
        .flatMap(documentUnitDTO -> saveFileNumbers(documentUnitDTO, documentUnit))
        .flatMap(documentUnitDTO -> saveDeviatingFileNumbers(documentUnitDTO, documentUnit))
        .flatMap(documentUnitDTO -> saveDeviatingEcli(documentUnitDTO, documentUnit))
        .flatMap(documentUnitDTO -> saveDeviatingDecisionDate(documentUnitDTO, documentUnit))
        .flatMap(documentUnitDTO -> saveIncorrectCourt(documentUnitDTO, documentUnit))
        .map(DocumentUnitTransformer::transformDTO);
  }

  private Mono<DocumentUnitDTO> enrichDocumentType(
      DocumentUnitDTO documentUnitDTO, DocumentUnit documentUnit) {
    if (documentUnit.coreData() == null || documentUnit.coreData().documentType() == null) {
      documentUnitDTO.setDocumentTypeId(null);
      return Mono.just(documentUnitDTO);
    }
    return documentTypeRepository
        .findByJurisShortcut(documentUnit.coreData().documentType().jurisShortcut())
        .map(
            documentTypeDTO -> {
              if (!documentTypeDTO
                  .getLabel()
                  .equals(documentUnit.coreData().documentType().label())) {
                throw new DocumentUnitException(
                    "DocumentType label does not match the database entry, this should not happen");
              }
              documentUnitDTO.setDocumentTypeDTO(documentTypeDTO);
              documentUnitDTO.setDocumentTypeId(documentTypeDTO.getId());
              return documentUnitDTO;
            });
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

    return courtRepository
        .findByCourttypeAndCourtlocation(
            documentUnit.coreData().court().type(), documentUnit.coreData().court().location())
        .defaultIfEmpty(CourtDTO.builder().build());
  }

  public Mono<DocumentUnitDTO> saveFileNumbers(
      DocumentUnitDTO documentUnitDTO, DocumentUnit documentUnit) {
    return fileNumberRepository
        .findAllByDocumentUnitIdAndIsDeviating(documentUnitDTO.getId(), false)
        .collectList()
        .flatMap(
            fileNumberDTOs -> {
              List<String> fileNumbers = new ArrayList<>();
              if (documentUnit.coreData() != null
                  && documentUnit.coreData().fileNumbers() != null) {
                fileNumbers.addAll(documentUnit.coreData().fileNumbers());
              }

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
    return injectFileNumbers(documentUnitDTO)
        .flatMap(this::injectDeviatingFileNumbers)
        .flatMap(this::injectProceedingDecisions)
        .flatMap(this::injectDeviatingEclis)
        .flatMap(this::injectDeviatingDecisionDates)
        .flatMap(this::injectIncorrectCourt)
        .flatMap(this::injectDocumentType)
        .flatMap(this::injectFieldsOfLaw)
        .flatMap(this::injectKeywords);
  }

  private Mono<DocumentUnitDTO> injectProceedingDecisions(DocumentUnitDTO documentUnitDTO) {
    return metadataRepository
        .findAllById(
            metadataRepository
                .findByUuid(documentUnitDTO.uuid)
                .map(DocumentUnitMetadataDTO::getId)
                .flatMapMany(proceedingDecisionLinkRepository::findAllByParentDocumentUnitId)
                .map(ProceedingDecisionLinkDTO::getChildDocumentUnitId))
        .flatMap(this::injectAdditionalProceedingDecisionInformation)
        .collectList()
        .map(
            proceedingDecisionDTOS -> {
              documentUnitDTO.setProceedingDecisions(proceedingDecisionDTOS);
              return documentUnitDTO;
            });
  }

  private <T extends DocumentUnitMetadataDTO> Mono<T> injectFileNumbers(T documentUnitMetadataDTO) {
    return fileNumberRepository
        .findAllByDocumentUnitId(documentUnitMetadataDTO.getId())
        .collectList()
        .flatMap(
            fileNumbers -> {
              documentUnitMetadataDTO.setFileNumbers(
                  fileNumbers.stream()
                      .filter(fileNumberDTO -> !fileNumberDTO.getIsDeviating())
                      .toList());
              return Mono.just(documentUnitMetadataDTO);
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
    return documentTypeRepository
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
        .flatMap(
            incorrectCourtDTOs -> {
              documentUnitDTO.setIncorrectCourts(incorrectCourtDTOs);
              return Mono.just(documentUnitDTO);
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
        .findAllByDocumentUnitId(documentUnitDTO.getId())
        .collectList()
        .flatMap(
            keywordDTO -> {
              documentUnitDTO.setKeywords(keywordDTO);
              return Mono.just(documentUnitDTO);
            });
  }

  private Mono<DocumentUnitMetadataDTO> injectAdditionalProceedingDecisionInformation(
      DocumentUnitMetadataDTO documentUnitMetadataDTO) {
    return injectProceedingDecisionFileNumbers(documentUnitMetadataDTO)
        .flatMap(this::injectProceedingDecisionDocumentType);
  }

  private Mono<DocumentUnitMetadataDTO> injectProceedingDecisionFileNumbers(
      DocumentUnitMetadataDTO documentUnitMetadataDTO) {
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

  private Mono<DocumentUnitMetadataDTO> injectProceedingDecisionDocumentType(
      DocumentUnitMetadataDTO documentUnitMetadataDTO) {
    if (documentUnitMetadataDTO.getDocumentTypeId() == null) {
      return Mono.just(documentUnitMetadataDTO);
    }
    return documentTypeRepository
        .findById(documentUnitMetadataDTO.getDocumentTypeId())
        .defaultIfEmpty(DocumentTypeDTO.builder().build())
        .map(
            documentTypeDTO -> {
              documentUnitMetadataDTO.setDocumentTypeDTO(documentTypeDTO);
              return documentUnitMetadataDTO;
            });
  }

  public Flux<ProceedingDecision> searchForDocumentUnityByProceedingDecisionInput(
      ProceedingDecision proceedingDecision) {
    String courtType;
    String courtLocation;
    Court court = proceedingDecision.court();
    courtType = (court == null || court.type() == null) ? null : court.type();
    courtLocation = (court == null || court.location() == null) ? null : court.location();
    Instant decisionDate = proceedingDecision.date();
    DocumentType docType = proceedingDecision.documentType();

    Mono<List<Long>> documentUnitDTOIdsViaFileNumber =
        fileNumberRepository
            .findByFileNumber(proceedingDecision.fileNumber())
            .map(FileNumberDTO::getDocumentUnitId)
            .collectList();
    Mono<Long> documentTypeDTOId =
        (docType == null || docType.jurisShortcut() == null)
            ? Mono.just(-1L)
            : documentTypeRepository
                .findByJurisShortcut(proceedingDecision.documentType().jurisShortcut())
                .mapNotNull(DocumentTypeDTO::getId);

    return Mono.zip(documentUnitDTOIdsViaFileNumber, documentTypeDTOId)
        .flatMapMany(
            tuple -> {
              Long[] docUnitIds;
              if (tuple.getT1().isEmpty()) {
                if (proceedingDecision.fileNumber() == null
                    || proceedingDecision.fileNumber().isBlank()) {
                  // @Query needs null and not empty list to ignore it
                  docUnitIds = null;
                } else {
                  // search string exists, but no matching file number found
                  // --> no chance for a search result
                  return Flux.empty();
                }
              } else {
                docUnitIds = tuple.getT1().toArray(Long[]::new);
              }
              Long docTypeId = tuple.getT2() == -1L ? null : tuple.getT2();
              LOGGER.debug(
                  "searchForProceedingDecisions params: {}, {}, {}, {}, {}",
                  courtType,
                  courtLocation,
                  decisionDate,
                  docUnitIds,
                  docTypeId);
              return metadataRepository.findByCourtDateFileNumberAndDocumentType(
                  courtType, courtLocation, decisionDate, docUnitIds, docTypeId);
            })
        .flatMap(this::injectAdditionalInformation)
        .map(
            documentUnitMetadataDTO ->
                ProceedingDecision.builder()
                    .uuid(documentUnitMetadataDTO.getUuid())
                    .documentNumber(documentUnitMetadataDTO.getDocumentnumber())
                    .dataSource(documentUnitMetadataDTO.getDataSource())
                    .court(
                        Court.builder()
                            .type(documentUnitMetadataDTO.getCourtType())
                            .location(documentUnitMetadataDTO.getCourtLocation())
                            .build())
                    .date(documentUnitMetadataDTO.getDecisionDate())
                    .fileNumber(
                        documentUnitMetadataDTO.getFileNumbers() == null
                                || documentUnitMetadataDTO.getFileNumbers().isEmpty()
                            ? null
                            : documentUnitMetadataDTO.getFileNumbers().get(0).getFileNumber())
                    .documentType(
                        documentUnitMetadataDTO.getDocumentTypeDTO() == null
                            ? null
                            : DocumentType.builder()
                                .label(documentUnitMetadataDTO.getDocumentTypeDTO().getLabel())
                                .jurisShortcut(
                                    documentUnitMetadataDTO.getDocumentTypeDTO().getJurisShortcut())
                                .build())
                    .build());
  }

  public Flux<DocumentUnitListEntry> findAll(Sort sort) {
    return metadataRepository
        .findAllByDataSourceLike(sort, DataSource.NEURIS.name())
        .flatMap(this::injectFileNumbers)
        .map(
            documentUnitDTO ->
                DocumentUnitListEntry.builder()
                    .uuid(documentUnitDTO.getUuid())
                    .documentNumber(documentUnitDTO.getDocumentnumber())
                    .creationTimestamp(documentUnitDTO.getCreationtimestamp())
                    .dataSource(documentUnitDTO.getDataSource())
                    .fileName(documentUnitDTO.getFilename())
                    .fileNumber(
                        documentUnitDTO.getFileNumbers() == null
                                || documentUnitDTO.getFileNumbers().isEmpty()
                            ? null
                            : documentUnitDTO.getFileNumbers().get(0).getFileNumber())
                    .build());
  }

  @Override
  public Flux<ProceedingDecision> findAllLinkedDocumentUnitsByParentDocumentUnitId(
      UUID parentDocumentUnitUuid) {
    return metadataRepository
        .findAllById(
            metadataRepository
                .findByUuid(parentDocumentUnitUuid)
                .map(DocumentUnitMetadataDTO::getId)
                .flatMapMany(proceedingDecisionLinkRepository::findAllByParentDocumentUnitId)
                .map(ProceedingDecisionLinkDTO::getChildDocumentUnitId))
        .flatMap(this::injectAdditionalInformation)
        .map(ProceedingDecisionTransformer::transformToDomain);
  }

  private Mono<DocumentUnitMetadataDTO> injectAdditionalInformation(
      DocumentUnitMetadataDTO documentUnitMetadataDTO) {
    return injectFileNumbers(documentUnitMetadataDTO).flatMap(this::injectDocumentType);
  }

  @Override
  public Mono<ProceedingDecisionLinkDTO> linkDocumentUnits(
      UUID parentDocumentUnitUuid, UUID childDocumentUnitUuid) {
    Mono<Long> parentDocumentUnitId =
        metadataRepository.findByUuid(parentDocumentUnitUuid).map(DocumentUnitMetadataDTO::getId);
    Mono<Long> childDocumentUnitId =
        metadataRepository.findByUuid(childDocumentUnitUuid).map(DocumentUnitMetadataDTO::getId);

    return Mono.zip(parentDocumentUnitId, childDocumentUnitId)
        .flatMap(
            tuple ->
                proceedingDecisionLinkRepository.save(
                    ProceedingDecisionLinkDTO.builder()
                        .parentDocumentUnitId(tuple.getT1())
                        .childDocumentUnitId(tuple.getT2())
                        .build()));
  }

  @Override
  public Mono<Void> unlinkDocumentUnits(UUID parentDocumentUnitUuid, UUID childDocumentUnitUuid) {
    Mono<Long> parentDocumentUnitId =
        metadataRepository.findByUuid(parentDocumentUnitUuid).map(DocumentUnitMetadataDTO::getId);
    Mono<Long> childDocumentUnitId =
        metadataRepository.findByUuid(childDocumentUnitUuid).map(DocumentUnitMetadataDTO::getId);

    return Mono.zip(parentDocumentUnitId, childDocumentUnitId)
        .flatMap(
            tuple ->
                proceedingDecisionLinkRepository
                    .findByParentDocumentUnitIdAndChildDocumentUnitId(tuple.getT1(), tuple.getT2())
                    .flatMap(
                        proceedingDecisionLinkDTO ->
                            proceedingDecisionLinkRepository.deleteById(
                                proceedingDecisionLinkDTO.getId())));
  }

  @Override
  public Mono<Long> countLinksByChildDocumentUnitUuid(UUID childDocumentUnitUuid) {
    return repository
        .findByUuid(childDocumentUnitUuid)
        .map(DocumentUnitMetadataDTO::getId)
        .flatMap(proceedingDecisionLinkRepository::countByChildDocumentUnitId);
  }
}
