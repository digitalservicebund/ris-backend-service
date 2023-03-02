package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import de.bund.digitalservice.ris.caselaw.adapter.DocumentUnitBuilder;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.CourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseSubjectFieldRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.LegalEffect;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.StateDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.StateRepository;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DeviatingDecisionDateTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentUnitTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.IncorrectCourtTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.PreviousDecisionTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.PreviousDecision;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Repository
public class PostgresDocumentUnitRepositoryImpl implements DocumentUnitRepository {
  private final DatabaseDocumentUnitRepository repository;
  private final DatabasePreviousDecisionRepository previousDecisionRepository;
  private final FileNumberRepository fileNumberRepository;
  private final DeviatingEcliRepository deviatingEcliRepository;
  private final DatabaseDeviatingDecisionDateRepository deviatingDecisionDateRepository;

  private final DatabaseIncorrectCourtRepository incorrectCourtRepository;
  private final CourtRepository courtRepository;
  private final StateRepository stateRepository;
  private final DocumentTypeRepository documentTypeRepository;
  private final DatabaseSubjectFieldRepository subjectFieldRepository;
  private final DatabaseDocumentUnitFieldsOfLawRepository documentUnitFieldsOfLawRepository;

  public PostgresDocumentUnitRepositoryImpl(
      DatabaseDocumentUnitRepository repository,
      FileNumberRepository fileNumberRepository,
      DeviatingEcliRepository deviatingEcliRepository,
      DatabasePreviousDecisionRepository previousDecisionRepository,
      DatabaseDeviatingDecisionDateRepository deviatingDecisionDateRepository,
      DatabaseIncorrectCourtRepository incorrectCourtRepository,
      CourtRepository courtRepository,
      StateRepository stateRepository,
      DocumentTypeRepository documentTypeRepository,
      DatabaseSubjectFieldRepository subjectFieldRepository,
      DatabaseDocumentUnitFieldsOfLawRepository documentUnitFieldsOfLawRepository) {

    this.repository = repository;
    this.previousDecisionRepository = previousDecisionRepository;
    this.fileNumberRepository = fileNumberRepository;
    this.deviatingEcliRepository = deviatingEcliRepository;
    this.deviatingDecisionDateRepository = deviatingDecisionDateRepository;
    this.incorrectCourtRepository = incorrectCourtRepository;
    this.courtRepository = courtRepository;
    this.stateRepository = stateRepository;
    this.documentTypeRepository = documentTypeRepository;
    this.subjectFieldRepository = subjectFieldRepository;
    this.documentUnitFieldsOfLawRepository = documentUnitFieldsOfLawRepository;
  }

  @Override
  public Mono<DocumentUnit> findByDocumentNumber(String documentNumber) {
    return repository
        .findByDocumentnumber(documentNumber)
        .flatMap(this::injectAdditionalInformation)
        .map(
            documentUnitDTO ->
                DocumentUnitBuilder.newInstance().setDocumentUnitDTO(documentUnitDTO).build());
  }

  @Override
  public Mono<DocumentUnit> findByUuid(UUID uuid) {
    return repository
        .findByUuid(uuid)
        .flatMap(this::injectAdditionalInformation)
        .map(
            documentUnitDTO ->
                DocumentUnitBuilder.newInstance().setDocumentUnitDTO(documentUnitDTO).build());
  }

  @Override
  public Mono<DocumentUnit> createNewDocumentUnit(String documentNumber) {
    return repository
        .save(
            DocumentUnitDTO.builder()
                .uuid(UUID.randomUUID())
                .creationtimestamp(Instant.now())
                .documentnumber(documentNumber)
                .dataSource(DataSourceDTO.NEURIS)
                .legalEffect(LegalEffect.NOT_SPECIFIED.getLabel())
                .build())
        .map(
            documentUnitDTO ->
                DocumentUnitBuilder.newInstance().setDocumentUnitDTO(documentUnitDTO).build());
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
        .flatMap(documentUnitDTO -> savePreviousDecisions(documentUnitDTO, documentUnit))
        .flatMap(documentUnitDTO -> saveFileNumbers(documentUnitDTO, documentUnit))
        .flatMap(documentUnitDTO -> saveDeviatingFileNumbers(documentUnitDTO, documentUnit))
        .flatMap(documentUnitDTO -> saveDeviatingEcli(documentUnitDTO, documentUnit))
        .flatMap(documentUnitDTO -> saveDeviatingDecisionDate(documentUnitDTO, documentUnit))
        .flatMap(documentUnitDTO -> saveIncorrectCourt(documentUnitDTO, documentUnit))
        .map(
            documentUnitDTO ->
                DocumentUnitBuilder.newInstance().setDocumentUnitDTO(documentUnitDTO).build());
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
        || !Objects.equals(documentUnitDTO.courtType, documentUnit.coreData().court().type())
        || !Objects.equals(
            documentUnitDTO.courtLocation, documentUnit.coreData().court().location());
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

  private Mono<DocumentUnitDTO> savePreviousDecisions(
      DocumentUnitDTO documentUnitDTO, DocumentUnit documentUnit) {
    return previousDecisionRepository
        .findAllByDocumentUnitId(documentUnitDTO.getId())
        .collectList()
        .flatMap(
            previousDecisionDTOs -> {
              List<Long> toDeleteIds = new ArrayList<>();
              List<PreviousDecisionDTO> toUpdate = new ArrayList<>();
              List<PreviousDecisionDTO> toInsert = new ArrayList<>();

              List<Long> updateIds = new ArrayList<>();

              if (documentUnit.previousDecisions() == null
                  || documentUnit.previousDecisions().isEmpty()) {
                toDeleteIds.addAll(
                    previousDecisionDTOs.stream().map(PreviousDecisionDTO::getId).toList());
              } else {
                toInsert.addAll(
                    documentUnit.previousDecisions().stream()
                        .filter(previousDecision -> previousDecision.id() == null)
                        .map(
                            previousDecision ->
                                PreviousDecisionTransformer.generateDTO(
                                    previousDecision, documentUnitDTO.getId()))
                        .toList());
                List<PreviousDecision> toUpdateOrToDelete =
                    new ArrayList<>(
                        documentUnit.previousDecisions().stream()
                            .filter(previousDecision -> previousDecision.id() != null)
                            .toList());

                previousDecisionDTOs.forEach(
                    previousDecisionDTO -> {
                      Optional<PreviousDecision> previousDecisionOptional =
                          toUpdateOrToDelete.stream()
                              .filter(
                                  previousDecision ->
                                      previousDecisionDTO.getId().equals(previousDecision.id()))
                              .findFirst();
                      if (previousDecisionOptional.isPresent()) {
                        toUpdate.add(
                            PreviousDecisionTransformer.enrichDTO(
                                previousDecisionDTO, previousDecisionOptional.get()));
                        updateIds.add(previousDecisionDTO.getId());
                        toUpdateOrToDelete.removeIf(
                            previousDecision ->
                                previousDecision.id().equals(previousDecisionDTO.getId()));
                      } else {
                        toDeleteIds.add(previousDecisionDTO.getId());
                      }
                    });

                updateIds.addAll(toUpdateOrToDelete.stream().map(PreviousDecision::id).toList());
              }

              List<PreviousDecisionDTO> savedPreviousDecisions = new ArrayList<>();

              return previousDecisionRepository
                  .findAllById(updateIds)
                  .collectList()
                  .map(
                      updateList -> {
                        updateList.forEach(
                            previousDecisionDTO -> {
                              if (!Objects.equals(
                                  previousDecisionDTO.documentUnitId, documentUnitDTO.getId())) {
                                throw new DocumentUnitException(
                                    "previous decision not for the right document unit");
                              }
                            });
                        return updateList;
                      })
                  .flatMap(list -> previousDecisionRepository.deleteAllById(toDeleteIds))
                  .then(previousDecisionRepository.saveAll(toUpdate).collectList())
                  .map(
                      updatedPreviousDecisions -> {
                        savedPreviousDecisions.addAll(updatedPreviousDecisions);
                        return documentUnitDTO;
                      })
                  .flatMap(v -> previousDecisionRepository.saveAll(toInsert).collectList())
                  .map(
                      insertedPreviousDecisions -> {
                        savedPreviousDecisions.addAll(insertedPreviousDecisions);
                        return documentUnitDTO;
                      })
                  .map(
                      oldDocumentUnitDTO -> {
                        oldDocumentUnitDTO.setPreviousDecisions(savedPreviousDecisions);
                        return oldDocumentUnitDTO;
                      });
            });
  }

  private Mono<DocumentUnitDTO> saveFileNumbers(
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
        .map(
            documentUnitDTO ->
                DocumentUnitBuilder.newInstance().setDocumentUnitDTO(documentUnitDTO).build());
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
        .map(
            documentUnitDTO ->
                DocumentUnitBuilder.newInstance().setDocumentUnitDTO(documentUnitDTO).build());
  }

  @Override
  public Mono<Void> delete(DocumentUnit documentUnit) {
    return repository
        .findByUuid(documentUnit.uuid())
        .flatMap(documentUnitDTO -> repository.deleteById(documentUnitDTO.getId()));
  }

  private Mono<DocumentUnitDTO> injectAdditionalInformation(DocumentUnitDTO documentUnitDTO) {
    return injectPreviousDecisions(documentUnitDTO)
        .flatMap(this::injectFileNumbers)
        .flatMap(this::injectDeviatingEclis)
        .flatMap(this::injectDeviatingDecisionDates)
        .flatMap(this::injectIncorrectCourt)
        .flatMap(this::injectDocumentType)
        .flatMap(this::injectFieldsOfLaw);
  }

  private Mono<DocumentUnitDTO> injectPreviousDecisions(DocumentUnitDTO documentUnitDTO) {
    return previousDecisionRepository
        .findAllByDocumentUnitId(documentUnitDTO.getId())
        .collectList()
        .map(
            list -> {
              documentUnitDTO.setPreviousDecisions(list);
              return documentUnitDTO;
            })
        .map(v -> documentUnitDTO);
  }

  private Mono<DocumentUnitDTO> injectFileNumbers(DocumentUnitDTO documentUnitDTO) {
    return fileNumberRepository
        .findAllByDocumentUnitId(documentUnitDTO.getId())
        .collectList()
        .flatMap(
            fileNumbers -> {
              documentUnitDTO.setFileNumbers(
                  fileNumbers.stream()
                      .filter(fileNumberDTO -> !fileNumberDTO.getIsDeviating())
                      .toList());
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

  private Mono<DocumentUnitDTO> injectDocumentType(DocumentUnitDTO documentUnitDTO) {
    if (documentUnitDTO.getDocumentTypeId() == null) {
      return Mono.just(documentUnitDTO);
    }
    return documentTypeRepository
        .findById(documentUnitDTO.getDocumentTypeId())
        .defaultIfEmpty(DocumentTypeDTO.builder().build())
        .map(
            documentTypeDTO -> {
              documentUnitDTO.setDocumentTypeDTO(documentTypeDTO);
              return documentUnitDTO;
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
        .findAllByDocumentUnitId(documentUnitDTO.id)
        .map(DocumentUnitFieldsOfLawDTO::fieldOfLawId)
        .collectList()
        .flatMapMany(subjectFieldRepository::findAllById)
        .collectList()
        .map(fieldsOfLaw -> documentUnitDTO.toBuilder().fieldsOfLaw(fieldsOfLaw).build());
  }
}
