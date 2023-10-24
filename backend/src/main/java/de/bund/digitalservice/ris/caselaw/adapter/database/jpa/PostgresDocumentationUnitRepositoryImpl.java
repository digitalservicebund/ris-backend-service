package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationUnitSearchEntryTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationUnitTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitNorm;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitSearchInput;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitException;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitLink;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitLinkType;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitSearchEntry;
import de.bund.digitalservice.ris.caselaw.domain.LinkedDocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@Slf4j
@Primary
public class PostgresDocumentationUnitRepositoryImpl implements DocumentUnitRepository {
  private final DatabaseDocumentationUnitRepository repository;
  private final DatabaseFileNumberRepository fileNumberRepository;
  private final DatabaseDocumentTypeRepository databaseDocumentTypeRepository;
  private final DatabaseDocumentCategoryRepository databaseDocumentCategoryRepository;
  private final DatabaseNormReferenceRepository documentUnitNormRepository;
  private final DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  private final DatabaseNormAbbreviationRepository normAbbreviationRepository;
  private final EntityManager entityManager;

  public PostgresDocumentationUnitRepositoryImpl(
      DatabaseDocumentationUnitRepository repository,
      DatabaseFileNumberRepository fileNumberRepository,
      DatabaseDocumentTypeRepository databaseDocumentTypeRepository,
      DatabaseDocumentCategoryRepository databaseDocumentCategoryRepository,
      DatabaseNormReferenceRepository documentUnitNormRepository,
      DatabaseNormAbbreviationRepository normAbbreviationRepository,
      DatabaseDocumentationOfficeRepository documentationOfficeRepository,
      EntityManager entityManager) {

    this.repository = repository;
    this.fileNumberRepository = fileNumberRepository;
    this.databaseDocumentTypeRepository = databaseDocumentTypeRepository;
    this.databaseDocumentCategoryRepository = databaseDocumentCategoryRepository;
    this.documentUnitNormRepository = documentUnitNormRepository;
    this.documentationOfficeRepository = documentationOfficeRepository;
    this.normAbbreviationRepository = normAbbreviationRepository;
    this.entityManager = entityManager;
  }

  @Override
  public Mono<DocumentUnit> findByDocumentNumber(String documentNumber) {
    if (log.isDebugEnabled()) {
      log.debug("find by document number: {}", documentNumber);
    }

    return Mono.just(
        DocumentationUnitTransformer.transformDTO(
            repository.findByDocumentNumber(documentNumber).orElse(null)));
  }

  @Override
  public Mono<DocumentUnit> findByUuid(UUID uuid) {
    if (log.isDebugEnabled()) {
      log.debug("find by uuid: {}", uuid);
    }
    return Mono.just(
        DocumentationUnitTransformer.transformDTO(repository.findById(uuid).orElse(null)));
  }

  @Override
  public Mono<DocumentUnit> createNewDocumentUnit(
      String documentNumber, DocumentationOffice documentationOffice) {

    return Mono.just(documentationOfficeRepository.findByLabel(documentationOffice.label()))
        .flatMap(
            documentationOfficeDTO ->
                Mono.just(
                    repository.save(
                        DocumentationUnitDTO.builder()
                            .id(UUID.randomUUID())
                            .documentNumber(documentNumber)
                            // .documentationOfficeId(documentationOfficeDTO.getId())
                            // .documentationOffice(documentationOfficeDTO)
                            // TODO .dateKnown(true)
                            // .legalEffect(LegalEffect.NOT_SPECIFIED.getLabel())
                            .build())))
        .map(DocumentationUnitTransformer::transformDTO);
  }

  @Override
  @Transactional(transactionManager = "connectionFactoryTransactionManager")
  public Mono<DocumentUnit> save(DocumentUnit documentUnit) {

    var docUnitDto = repository.findById(documentUnit.uuid()).orElse(null);
    if (docUnitDto == null) {
      log.info("Can't save non-existing docUnit with id = " + documentUnit.uuid());
      return Mono.empty();
    }

    if (documentUnit.coreData() != null && documentUnit.coreData().documentType() != null) {
      docUnitDto.setDocumentType(getDbDocType(documentUnit.coreData().documentType()));
    }
    // .flatMap(documentUnitDTO -> enrichLegalEffect(documentUnitDTO, documentUnit))
    // .flatMap(documentUnitDTO -> enrichRegion(documentUnitDTO, documentUnit))

    DocumentationUnitTransformer.enrichDTO(docUnitDto, documentUnit);
    docUnitDto = repository.save(docUnitDto);
    saveNorms(docUnitDto, documentUnit);

    // .flatMap(documentUnitDTO -> saveProcedure(documentUnitDTO, documentUnit))
    //        .flatMap(
    //            documentUnitDTO -> {
    //              return Mono.just();
    //            })

    if (documentUnit.coreData() != null && documentUnit.coreData().fileNumbers() != null) {
      Set<FileNumberDTO> fileNumbers = new HashSet<>();
      fileNumbers.addAll(saveFileNumbers(docUnitDto, documentUnit.coreData().fileNumbers()));
      docUnitDto.setFileNumbers(fileNumbers);
    }

    //        .flatMap(documentUnitDTO -> saveDeviatingFileNumbers(documentUnitDTO,
    // documentUnit))
    //        .flatMap(documentUnitDTO -> saveDeviatingEcli(documentUnitDTO, documentUnit))
    //        .flatMap(documentUnitDTO -> saveDeviatingDecisionDate(documentUnitDTO,
    // documentUnit))
    //        .flatMap(documentUnitDTO -> saveIncorrectCourt(documentUnitDTO, documentUnit))
    //        .flatMap(this::injectStatus)
    //        .flatMap(this::injectKeywords)
    //        .flatMap(this::injectFieldsOfLaw)
    //        .flatMap(this::injectPreviousProcedures)
    //        .flatMap(documentUnitDTO -> saveActiveCitations(documentUnitDTO, documentUnit))
    //        .flatMap(documentUnitDTO -> saveProceedingDecisions(documentUnitDTO,
    // documentUnit))
    return Mono.just(DocumentationUnitTransformer.transformDTO(docUnitDto));
  }

  private DocumentTypeDTO getDbDocType(DocumentType documentType) {
    if (documentType == null) {
      return null;
    }

    DocumentTypeDTO docTypeDTO =
        databaseDocumentTypeRepository.findFirstByAbbreviationAndCategory(
            documentType.jurisShortcut(), databaseDocumentCategoryRepository.findFirstByLabel("R"));

    if (docTypeDTO == null) {
      throw new DocumentationUnitException(
          "no document type for the shortcut '" + documentType.jurisShortcut() + "' found.");
    }
    return docTypeDTO;
  }

  private boolean hasCourtChanged(DocumentationUnitDTO documentUnitDTO, DocumentUnit documentUnit) {
    return documentUnit == null
        || documentUnit.coreData() == null
        || documentUnit.coreData().court() == null
    //        || !Objects.equals(documentUnitDTO.getCourtType(),
    // documentUnit.coreData().court().type())
    //        || !Objects.equals(
    //            documentUnitDTO.getCourtLocation(), documentUnit.coreData().court().location())
    ;
  }

  //  private DocumentationUnitDTO enrichLegalEffect(
  //          DocumentationUnitDTO documentUnitDTO, DocumentUnit documentUnit) {
  //    documentUnitDTO.setLegalEffect(
  //        LegalEffect.deriveFrom(documentUnit, hasCourtChanged(documentUnitDTO, documentUnit)));
  //    return documentUnitDTO;
  //  }

  //  private DocumentationUnitDTO enrichRegion(
  //          DocumentationUnitDTO documentUnitDTO, DocumentUnit documentUnit) {
  //    if (!hasCourtChanged(documentUnitDTO, documentUnit)) {
  //      return documentUnitDTO;
  //    }
  //
  //    return getCourt(documentUnit)
  //        .flatMap(
  //            courtDTO -> {
  //              if (courtDTO.getFederalstate() == null) {
  //                return Mono.just(StateDTO.builder().label(courtDTO.getRegion()).build());
  //              }
  //              return stateRepository
  //                  .findByJurisshortcut(courtDTO.getFederalstate())
  //                  .defaultIfEmpty(StateDTO.builder().build());
  //            })
  //        .map(
  //            stateDTO -> {
  //              documentUnitDTO.setRegions(stateDTO.getRegion());
  //              return documentUnitDTO;
  //            });
  //  }

  //  private Mono<CourtDTO> getCourt(DocumentUnit documentUnit) {
  //    if (documentUnit == null
  //        || documentUnit.coreData() == null
  //        || documentUnit.coreData().court() == null) {
  //      return Mono.just(CourtDTO.builder().build());
  //    }
  //
  //    return databaseCourtRepository
  //        .findByCourttypeAndCourtlocation(
  //            documentUnit.coreData().court().type(), documentUnit.coreData().court().location())
  //        .defaultIfEmpty(CourtDTO.builder().build());
  //  }

  public Mono<DocumentationUnitDTO> saveNorms(
      DocumentationUnitDTO documentUnitDTO, DocumentUnit documentUnit) {

    return Flux.fromIterable(
            documentUnitNormRepository.findAllByLegacyDocUnitIdOrderById(documentUnitDTO.getId()))
        .collectList()
        .flatMap(
            documentUnitNormDTOs -> {
              if (documentUnit.contentRelatedIndexing() == null
                  || documentUnit.contentRelatedIndexing().norms() == null)
                return Mono.just(documentUnitDTO);

              List<DocumentUnitNorm> documentUnitNorms =
                  new ArrayList<>(documentUnit.contentRelatedIndexing().norms());

              AtomicInteger normIndex = new AtomicInteger(0);
              List<NormReferenceDTO> toSave = new ArrayList<>();
              List<NormReferenceDTO> toDelete = new ArrayList<>();

              documentUnitNormDTOs.forEach(
                  documentUnitNormDTO -> {
                    int index = normIndex.getAndIncrement();
                    if (index < documentUnitNorms.size()) {
                      DocumentUnitNorm currentNorm = documentUnitNorms.get(index);
                      if (!isEmptyNorm(currentNorm)) {
                        documentUnitNormDTO.setId(currentNorm.id());
                        documentUnitNormDTO.setSingleNorm(currentNorm.singleNorm());
                        documentUnitNormDTO.setDateOfVersion(currentNorm.dateOfVersion());
                        documentUnitNormDTO.setDateOfRelevance(currentNorm.dateOfRelevance());
                        documentUnitNormDTO.setNormAbbreviation(
                            normAbbreviationRepository
                                .findById(currentNorm.normAbbreviation().id())
                                .orElse(null));
                        documentUnitNormDTO.setLegacyDocUnitId(documentUnitDTO.getId());
                        toSave.add(documentUnitNormDTO);
                      } else {
                        toDelete.add(documentUnitNormDTO);
                      }
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
                NormReferenceDTO normReferenceDTO =
                    NormReferenceDTO.builder()
                        .id(currentNorm.id())
                        .singleNorm(currentNorm.singleNorm())
                        .dateOfVersion(currentNorm.dateOfVersion())
                        .dateOfRelevance(currentNorm.dateOfRelevance())
                        .normAbbreviation(
                            normAbbreviationRepository
                                .findById(currentNorm.normAbbreviation().id())
                                .orElse(null))
                        .legacyDocUnitId(documentUnitDTO.getId())
                        .build();
                toSave.add(normReferenceDTO);
              }

              documentUnitNormRepository.deleteAll(toDelete);

              return Flux.fromIterable(documentUnitNormRepository.saveAll(toSave))
                  .collectList()
                  .map(
                      savedNormList -> {
                        // TODO List to set with better performance?
                        documentUnitDTO.setNormReferences(Set.copyOf(savedNormList));
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

  //  private Mono<DocumentationUnitDTO> saveDeviatingFileNumbers(
  //          DocumentationUnitDTO documentUnitDTO, DocumentUnit documentUnit) {
  //    return fileNumberRepository
  //        .findAllByDocumentUnitIdAndIsDeviating(documentUnitDTO.getId(), true)
  //        .collectList()
  //        .flatMap(
  //            deviatingFileNumberDTOs -> {
  //              List<String> deviatingFileNumbers = new ArrayList<>();
  //              if (documentUnit.coreData() != null
  //                  && documentUnit.coreData().deviatingFileNumbers() != null) {
  //                deviatingFileNumbers.addAll(documentUnit.coreData().deviatingFileNumbers());
  //              }
  //
  //              AtomicInteger deviatingFileNumberIndex = new AtomicInteger(0);
  //              List<FileNumberDTO> toSave = new ArrayList<>();
  //              List<FileNumberDTO> toDelete = new ArrayList<>();
  //
  //              deviatingFileNumberDTOs.forEach(
  //                  fileNumberDTO -> {
  //                    if (deviatingFileNumberIndex.get() < deviatingFileNumbers.size()) {
  //                      fileNumberDTO.fileNumber =
  //                          deviatingFileNumbers.get(deviatingFileNumberIndex.getAndIncrement());
  //                      fileNumberDTO.isDeviating = true;
  //                      toSave.add(fileNumberDTO);
  //                    } else {
  //                      toDelete.add(fileNumberDTO);
  //                    }
  //                  });
  //
  //              while (deviatingFileNumberIndex.get() < deviatingFileNumbers.size()) {
  //                FileNumberDTO fileNumberDTO =
  //                    FileNumberDTO.builder()
  //                        .fileNumber(
  //
  // deviatingFileNumbers.get(deviatingFileNumberIndex.getAndIncrement()))
  //                        .documentUnitId(documentUnitDTO.getId())
  //                        .isDeviating(true)
  //                        .build();
  //                toSave.add(fileNumberDTO);
  //              }
  //
  //              return fileNumberRepository
  //                  .deleteAll(toDelete)
  //                  .then(fileNumberRepository.saveAll(toSave).collectList())
  //                  .map(
  //                      savedDeviatingFileNumberList -> {
  //                        documentUnitDTO.setDeviatingFileNumbers(savedDeviatingFileNumberList);
  //                        return documentUnitDTO;
  //                      });
  //            });
  //  }

  //  private Mono<DocumentUnitDTO> saveDeviatingEcli(
  //      DocumentUnitDTO documentUnitDTO, DocumentUnit documentUnit) {
  //    return deviatingEcliRepository
  //        .findAllByDocumentUnitId(documentUnitDTO.getId())
  //        .collectList()
  //        .flatMap(
  //            deviatingEcliDTOs -> {
  //              List<String> deviatingEclis = new ArrayList<>();
  //              if (documentUnit.coreData() != null
  //                  && documentUnit.coreData().deviatingEclis() != null) {
  //                deviatingEclis.addAll(documentUnit.coreData().deviatingEclis());
  //              }
  //
  //              AtomicInteger deviatingEcliIndex = new AtomicInteger(0);
  //              List<DeviatingEcliDTO> toSave = new ArrayList<>();
  //              List<DeviatingEcliDTO> toDelete = new ArrayList<>();
  //
  //              deviatingEcliDTOs.forEach(
  //                  deviatingEcliDTO -> {
  //                    if (deviatingEcliIndex.get() < deviatingEclis.size()) {
  //                      deviatingEcliDTO.ecli =
  //                          deviatingEclis.get(deviatingEcliIndex.getAndIncrement());
  //                      toSave.add(deviatingEcliDTO);
  //                    } else {
  //                      toDelete.add(deviatingEcliDTO);
  //                    }
  //                  });
  //
  //              while (deviatingEcliIndex.get() < deviatingEclis.size()) {
  //                DeviatingEcliDTO deviatingEcliDTO =
  //                    DeviatingEcliDTO.builder()
  //                        .ecli(deviatingEclis.get(deviatingEcliIndex.getAndIncrement()))
  //                        .documentUnitId(documentUnitDTO.getId())
  //                        .build();
  //                toSave.add(deviatingEcliDTO);
  //              }
  //
  //              return deviatingEcliRepository
  //                  .deleteAll(toDelete)
  //                  .then(deviatingEcliRepository.saveAll(toSave).collectList())
  //                  .map(
  //                      savedDeviatingEcliList -> {
  //                        documentUnitDTO.setDeviatingEclis(savedDeviatingEcliList);
  //                        return documentUnitDTO;
  //                      });
  //            });
  //  }
  //
  //  private Mono<DocumentUnitDTO> saveDeviatingDecisionDate(
  //      DocumentUnitDTO documentUnitDTO, DocumentUnit documentUnit) {
  //    return deviatingDecisionDateRepository
  //        .findAllByDocumentUnitId(documentUnitDTO.getId())
  //        .collectList()
  //        .flatMap(
  //            deviatingDecisionDateDTOs -> {
  //              List<Instant> deviatingDecisionDates = new ArrayList<>();
  //              if (documentUnit.coreData() != null
  //                  && documentUnit.coreData().deviatingDecisionDates() != null) {
  //                deviatingDecisionDates.addAll(documentUnit.coreData().deviatingDecisionDates());
  //              }
  //
  //              AtomicInteger deviatingDecisionDateIndex = new AtomicInteger(0);
  //              List<DeviatingDecisionDateDTO> toSave = new ArrayList<>();
  //              List<DeviatingDecisionDateDTO> toDelete = new ArrayList<>();
  //
  //              deviatingDecisionDateDTOs.forEach(
  //                  deviatingDecisionDateDTO -> {
  //                    if (deviatingDecisionDateIndex.get() < deviatingDecisionDates.size()) {
  //                      deviatingDecisionDateDTO =
  //                          DeviatingDecisionDateTransformer.enrichDTO(
  //                              deviatingDecisionDateDTO,
  //                              deviatingDecisionDates.get(
  //                                  deviatingDecisionDateIndex.getAndIncrement()));
  //                      toSave.add(deviatingDecisionDateDTO);
  //                    } else {
  //                      toDelete.add(deviatingDecisionDateDTO);
  //                    }
  //                  });
  //
  //              while (deviatingDecisionDateIndex.get() < deviatingDecisionDates.size()) {
  //                DeviatingDecisionDateDTO deviatingDecisionDateDTO =
  //                    DeviatingDecisionDateDTO.builder()
  //                        .decisionDate(
  //                            deviatingDecisionDates.get(
  //                                deviatingDecisionDateIndex.getAndIncrement()))
  //                        .documentUnitId(documentUnitDTO.getId())
  //                        .build();
  //                toSave.add(deviatingDecisionDateDTO);
  //              }
  //
  //              return deviatingDecisionDateRepository
  //                  .deleteAll(toDelete)
  //                  .then(deviatingDecisionDateRepository.saveAll(toSave).collectList())
  //                  .map(
  //                      savedDeviatingDecisionDateList -> {
  //
  // documentUnitDTO.setDeviatingDecisionDates(savedDeviatingDecisionDateList);
  //                        return documentUnitDTO;
  //                      });
  //            });
  //  }
  //
  //  private Mono<DocumentUnitDTO> saveIncorrectCourt(
  //      DocumentUnitDTO documentUnitDTO, DocumentUnit documentUnit) {
  //    return incorrectCourtRepository
  //        .findAllByDocumentUnitId(documentUnitDTO.getId())
  //        .collectList()
  //        .flatMap(
  //            incorrectCourtDTOs -> {
  //              List<String> incorrectCourts = new ArrayList<>();
  //              if (documentUnit.coreData() != null
  //                  && documentUnit.coreData().incorrectCourts() != null) {
  //                incorrectCourts.addAll(documentUnit.coreData().incorrectCourts());
  //              }
  //
  //              AtomicInteger incorrectCourtIndex = new AtomicInteger(0);
  //              List<IncorrectCourtDTO> toSave = new ArrayList<>();
  //              List<IncorrectCourtDTO> toDelete = new ArrayList<>();
  //
  //              incorrectCourtDTOs.forEach(
  //                  incorrectCourtDTO -> {
  //                    if (incorrectCourtIndex.get() < incorrectCourts.size()) {
  //                      incorrectCourtDTO =
  //                          IncorrectCourtTransformer.enrichDTO(
  //                              incorrectCourtDTO,
  //                              incorrectCourts.get(incorrectCourtIndex.getAndIncrement()));
  //                      toSave.add(incorrectCourtDTO);
  //                    } else {
  //                      toDelete.add(incorrectCourtDTO);
  //                    }
  //                  });
  //
  //              while (incorrectCourtIndex.get() < incorrectCourts.size()) {
  //                IncorrectCourtDTO incorrectCourtDTO =
  //                    IncorrectCourtDTO.builder()
  //                        .court(incorrectCourts.get(incorrectCourtIndex.getAndIncrement()))
  //                        .documentUnitId(documentUnitDTO.getId())
  //                        .build();
  //                toSave.add(incorrectCourtDTO);
  //              }
  //
  //              return incorrectCourtRepository
  //                  .deleteAll(toDelete)
  //                  .then(incorrectCourtRepository.saveAll(toSave).collectList())
  //                  .map(
  //                      savedIncorrectCourtList -> {
  //                        documentUnitDTO.setIncorrectCourts(savedIncorrectCourtList);
  //                        return documentUnitDTO;
  //                      });
  //            });
  //  }
  //
  //  private Mono<DocumentUnitDTO> saveActiveCitations(
  //      DocumentUnitDTO documentUnitDTO, DocumentUnit documentUnit) {
  //    if (log.isDebugEnabled()) {
  //      log.debug("save active citations: {}", documentUnit.uuid());
  //    }
  //
  //    List<ActiveCitation> activeCitations = Collections.emptyList();
  //    if (documentUnit.contentRelatedIndexing() != null
  //        && documentUnit.contentRelatedIndexing().activeCitations() != null) {
  //      activeCitations = documentUnit.contentRelatedIndexing().activeCitations();
  //    }
  //
  //    return Flux.fromIterable(activeCitations)
  //        .filter(activeCitation -> activeCitation.getUuid() != null)
  //        .flatMap(
  //            activeCitation -> {
  //              if (activeCitation.hasNoValues()) {
  //                return unlinkDocumentUnit(
  //                    documentUnitDTO.getUuid(),
  //                    activeCitation.getUuid(),
  //                    DocumentationUnitLinkType.ACTIVE_CITATION);
  //              } else {
  //                return repository
  //                    .findByUuid(activeCitation.getUuid())
  //                    .filter(
  //                        activeCitationDocumentationUnitDTO ->
  //                            activeCitationDocumentationUnitDTO.getDataSource()
  //                                == DataSource.ACTIVE_CITATION)
  //                    .flatMap(
  //                        activeCitationDocumentationUnitDTO ->
  //                            updateLinkedDocumentationUnit(
  //                                activeCitationDocumentationUnitDTO, activeCitation))
  //                    .then(
  //                        documentationUnitLinkRepository
  //
  // .findByParentDocumentationUnitUuidAndChildDocumentationUnitUuidAndType(
  //                                documentUnitDTO.getUuid(),
  //                                activeCitation.getUuid(),
  //                                DocumentationUnitLinkType.ACTIVE_CITATION)
  //                            .flatMap(
  //                                documentationUnitLinkDTO -> {
  //                                  UUID citationStyleUuid = null;
  //
  //                                  if (activeCitation.getCitationStyle() != null) {
  //                                    citationStyleUuid =
  // activeCitation.getCitationStyle().uuid();
  //                                  }
  //
  //                                  return documentationUnitLinkRepository.save(
  //                                      documentationUnitLinkDTO.toBuilder()
  //                                          .citationStyleUuid(citationStyleUuid)
  //                                          .build());
  //                                }));
  //              }
  //            })
  //        .then(
  //            unlinkLinkedDocumentationUnit(documentUnit,
  // DocumentationUnitLinkType.ACTIVE_CITATION))
  //        .then(injectActiveCitations(documentUnitDTO));
  //  }
  //
  //  private Mono<DocumentUnitDTO> saveProceedingDecisions(
  //      DocumentUnitDTO documentUnitDTO, DocumentUnit documentUnit) {
  //    if (log.isDebugEnabled()) {
  //      log.debug("save precious decisions: {}", documentUnit.uuid());
  //    }
  //
  //    List<ProceedingDecision> proceedingDecisions = Collections.emptyList();
  //    if (documentUnit.proceedingDecisions() != null) {
  //      proceedingDecisions = documentUnit.proceedingDecisions();
  //    }
  //
  //    return Flux.fromIterable(proceedingDecisions)
  //        .filter(proceedingDecision -> proceedingDecision.getUuid() != null)
  //        .flatMap(
  //            proceedingDecision -> {
  //              if (proceedingDecision.hasNoValues()) {
  //                return unlinkDocumentUnit(
  //                    documentUnitDTO.getUuid(),
  //                    proceedingDecision.getUuid(),
  //                    DocumentationUnitLinkType.PREVIOUS_DECISION);
  //              } else {
  //                return repository
  //                    .findById(proceedingDecision.getUuid())
  //                    // TODO how to distinguish proceeding decisions
  //                    .filter(
  //                        proceedingDecisionDTO ->
  //                            proceedingDecisionDTO.getDataSource() ==
  // DataSource.PROCEEDING_DECISION)
  //                    .flatMap(
  //                        proceedingDecisionDTO ->
  //                            updateLinkedDocumentationUnit(
  //                                proceedingDecisionDTO, proceedingDecision))
  //                    .then(
  //                        documentationUnitLinkRepository
  //
  // .findByParentDocumentationUnitUuidAndChildDocumentationUnitUuidAndType(
  //                                documentUnitDTO.getUuid(),
  //                                proceedingDecision.getUuid(),
  //                                DocumentationUnitLinkType.PREVIOUS_DECISION));
  //              }
  //            })
  //        .then(
  //            unlinkLinkedDocumentationUnit(
  //                documentUnit, DocumentationUnitLinkType.PREVIOUS_DECISION))
  //        .then(injectProceedingDecisions(documentUnitDTO));
  //  }
  //
  //  private Mono<DocumentUnitDTO> saveProcedure(
  //      DocumentUnitDTO documentUnitDTO, DocumentUnit documentUnit) {
  //
  //    String documentationOfficeLabel = documentUnit.coreData().documentationOffice().label();
  //    Optional.ofNullable(documentUnit.coreData().procedure())
  //        .map(procedure -> findOrCreateProcedure(procedure, documentationOfficeLabel))
  //        .ifPresent(
  //            procedureDTO -> {
  //              if (!areCurrentlyLinked(documentUnitDTO, procedureDTO)) {
  //                procedureLinkRepository.save(
  //                    ProcedureLinkDTO.builder()
  //                        .procedureDTO(procedureDTO)
  //                        .documentationUnitId(documentUnitDTO.uuid)
  //                        .build());
  //              }
  //
  //              documentUnitDTO.setProcedure(procedureDTO);
  //            });
  //
  //    return Mono.just(documentUnitDTO);
  //  }
  //
  //  private ProcedureDTO findOrCreateProcedure(Procedure procedure, String
  // documentationOfficeLabel) {
  //    DocumentationOfficeDTO documentationOfficeDTO =
  //        documentationOfficeRepository.findByLabel(documentationOfficeLabel);
  //
  //    return Optional.ofNullable(
  //            procedureRepository.findByLabelAndDocumentationOfficeOrderByCreatedAtDesc(
  //                procedure.label(), documentationOfficeDTO))
  //        .orElseGet(
  //            () ->
  //                procedureRepository.save(
  //                    ProcedureDTO.builder()
  //                        .label(procedure.label())
  //                        .documentationOffice(documentationOfficeDTO)
  //                        .build()));
  //  }
  //
  //  private boolean areCurrentlyLinked(DocumentUnitDTO documentUnitDTO, ProcedureDTO procedureDTO)
  // {
  //    return Optional.ofNullable(
  //            procedureLinkRepository.findFirstByDocumentationUnitIdOrderByCreatedAtDesc(
  //                documentUnitDTO.uuid))
  //        .map(linkDTO -> linkDTO.getProcedureDTO().equals(procedureDTO))
  //        .orElse(false);
  //  }
  //
  //  private Mono<DocumentUnit> unlinkLinkedDocumentationUnit(
  //      DocumentUnit documentUnit, DocumentationUnitLinkType type) {
  //    if (log.isDebugEnabled()) {
  //      log.debug("unlink linked document unit of type {} for parent {}", type,
  // documentUnit.uuid());
  //    }
  //
  //    return findAllLinkedDocumentUnitsByParentDocumentUnitUuidAndType(documentUnit.uuid(), type)
  //        .filter(
  //            linkedDocumentationUnit ->
  //                filterUnlinkedDocumentUnit(documentUnit, linkedDocumentationUnit, type))
  //        .flatMap(
  //            linkedDocumentationUnit ->
  //                unlinkDocumentUnit(documentUnit.uuid(), linkedDocumentationUnit.getUuid(), type)
  //                    .thenReturn(linkedDocumentationUnit.getUuid()))
  //        .flatMap(this::deleteIfOrphanedLinkedDocumentationUnit)
  //        .then()
  //        .thenReturn(documentUnit);
  //  }
  //
  //  private boolean filterUnlinkedDocumentUnit(
  //      DocumentUnit documentUnit,
  //      LinkedDocumentationUnit linkedDocumentationUnit,
  //      DocumentationUnitLinkType type) {
  //    if (type == DocumentationUnitLinkType.ACTIVE_CITATION) {
  //      return documentUnit.contentRelatedIndexing() == null
  //          || documentUnit.contentRelatedIndexing().activeCitations() == null
  //          || documentUnit.contentRelatedIndexing().activeCitations().isEmpty()
  //          || documentUnit.contentRelatedIndexing().activeCitations().stream()
  //              .map(LinkedDocumentationUnit::getUuid)
  //              .noneMatch(linkedUuid -> linkedDocumentationUnit.getUuid().equals(linkedUuid));
  //    } else if (type == DocumentationUnitLinkType.PREVIOUS_DECISION) {
  //      return documentUnit.proceedingDecisions() == null
  //          || documentUnit.proceedingDecisions().isEmpty()
  //          || documentUnit.proceedingDecisions().stream()
  //              .map(LinkedDocumentationUnit::getUuid)
  //              .noneMatch(linkedUuid -> linkedDocumentationUnit.getUuid().equals(linkedUuid));
  //    } else {
  //      throw new DocumentationUnitException("Couldn't filter for unknown link type '" + type +
  // "'");
  //    }
  //  }
  //
  //  private Mono<DocumentUnitMetadataDTO> updateLinkedDocumentationUnit(
  //      DocumentationUnitDTO linkedDocumentationUnitDTO,
  //      LinkedDocumentationUnit linkedDocumentationUnit) {
  //    return metadataRepository
  //        .save(linkedDocumentationUnitDTO)
  //        .flatMap(
  //            documentUnitDTO ->
  //                saveFileNumbers(
  //                    documentUnitDTO,
  //                    linkedDocumentationUnit.getFileNumber() == null
  //                        ? Collections.emptyList()
  //                        : List.of(linkedDocumentationUnit.getFileNumber())));
  //  }

  public List<FileNumberDTO> saveFileNumbers(
      DocumentationUnitDTO documentUnitDTO, List<String> fileNumbers) {
    var oldFileNumbers = fileNumberRepository.findAllByDocumentationUnit(documentUnitDTO);

    List<FileNumberDTO> toSave = new ArrayList<>();
    List<FileNumberDTO> toDelete = new ArrayList<>();

    for (int i = 0; i < oldFileNumbers.size(); i++) {
      var fileNumberDTO = oldFileNumbers.get(i);
      if (i < fileNumbers.size()) {
        fileNumberDTO.setValue(fileNumbers.get(i));
        toSave.add(fileNumberDTO);
      } else {
        toDelete.add(fileNumberDTO);
      }
    }

    for (int i = oldFileNumbers.size(); i < fileNumbers.size(); i++) {
      FileNumberDTO fileNumberDTO =
          FileNumberDTO.builder()
              .value(fileNumbers.get(i))
              .documentationUnit(documentUnitDTO)
              .build();
      toSave.add(fileNumberDTO);
    }

    fileNumberRepository.deleteAll(toDelete);
    return fileNumberRepository.saveAll(toSave);
  }

  @Override
  public Mono<DocumentUnit> attachFile(
      UUID documentUnitUuid, String fileUuid, String type, String fileName) {
    var docUnitDto = repository.findById(documentUnitUuid).orElseThrow();
    docUnitDto.setOriginalFileDocument(
        OriginalFileDocumentDTO.builder()
            .s3ObjectPath(fileUuid)
            .filename(fileName)
            .extension(type)
            .uploadTimestamp(Instant.now())
            .build());
    docUnitDto = repository.save(docUnitDto);
    return Mono.just(DocumentationUnitTransformer.transformDTO(docUnitDto));
  }

  @Override
  public Mono<DocumentUnit> removeFile(UUID documentUnitId) {
    var docUnitDto = repository.findById(documentUnitId).orElseThrow();
    docUnitDto.setOriginalFileDocument(null);
    docUnitDto = repository.save(docUnitDto);
    return Mono.just(DocumentationUnitTransformer.transformDTO(docUnitDto));
  }

  @Override
  public Mono<Void> delete(DocumentUnit documentUnit) {
    repository.deleteById(documentUnit.uuid());
    return Mono.empty();
  }

  @Override
  public Flux<LinkedDocumentationUnit> searchByLinkedDocumentationUnit(
      LinkedDocumentationUnit linkedDocumentationUnit, Pageable pageable) {

    //    return Mono.zip(
    //            extractDocumentUnitDTOIdsViaFileNumber(linkedDocumentationUnit).collectList(),
    //            extractDocumentTypeDTOId(linkedDocumentationUnit))
    //        .flatMapMany(
    //            tuple -> {
    //              Long[] documentUnitDTOIdsViaFileNumber =
    //                  convertListToArrayOrReturnNull(tuple.getT1());
    //              if (documentUnitDTOIdsViaFileNumber == null
    //                  && linkedDocumentationUnit.getFileNumber() != null) return Flux.empty();
    //
    //              UUID documentTypeDTOId = tuple.getT2().equals(new UUID(0, 0)) ? null :
    // tuple.getT2();
    //              if (documentTypeDTOId == null && linkedDocumentationUnit.getDocumentType() !=
    // null)
    //                return Flux.empty();
    //
    //              return metadataRepository.searchByLinkedDocumentationUnit(
    //                  extractCourtType(linkedDocumentationUnit),
    //                  extractCourtLocation(linkedDocumentationUnit),
    //                  extractDecisionDate(linkedDocumentationUnit),
    //                  documentUnitDTOIdsViaFileNumber,
    //                  documentTypeDTOId,
    //                  pageable.getPageSize(),
    //                  pageable.getOffset());
    //            })
    //        .flatMapSequential(this::injectAdditionalInformation)
    //        .map(
    //            documentUnitMetadataDTO ->
    //                LinkedDocumentationUnitTransformer.transformToDomain(
    //                    documentUnitMetadataDTO, null));
    return null;
  }

  private String extractCourtType(LinkedDocumentationUnit linkedDocumentationUnit) {
    return Optional.ofNullable(linkedDocumentationUnit.getCourt()).map(Court::type).orElse(null);
  }

  private String extractCourtLocation(LinkedDocumentationUnit linkedDocumentationUnit) {
    return Optional.ofNullable(linkedDocumentationUnit.getCourt())
        .map(Court::location)
        .orElse(null);
  }

  //  private Instant extractDecisionDate(LinkedDocumentationUnit linkedDocumentationUnit) {
  //    return Optional.ofNullable(linkedDocumentationUnit.getDecisionDate())
  //        .map(date -> date.atZone(ZoneId.of("UTC")).toInstant())
  //        .orElse(null);
  //  }

  //  private Flux<Long> extractDocumentUnitDTOIdsViaFileNumber(
  //      LinkedDocumentationUnit linkedDocumentationUnit) {
  //    return fileNumberRepository
  //        .findByFileNumber(linkedDocumentationUnit.getFileNumber())
  //            .stream().map(FileNumberDTO::getDocumentUnitId);
  //  }

  private Mono<UUID> extractDocumentTypeDTOId(LinkedDocumentationUnit linkedDocumentationUnit) {
    return Mono.justOrEmpty(linkedDocumentationUnit.getDocumentType())
        .map(DocumentType::jurisShortcut)
        .flatMap(
            jurisShortcut ->
                Mono.justOrEmpty(
                    databaseDocumentTypeRepository.findFirstByAbbreviationAndCategory(
                        jurisShortcut, databaseDocumentCategoryRepository.findFirstByLabel("R"))))
        .mapNotNull(DocumentTypeDTO::getId)
        .switchIfEmpty(Mono.just(new UUID(0, 0)));
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

    DocumentationOfficeDTO documentationOfficeDTO =
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

  @Override
  public Flux<LinkedDocumentationUnit> findAllLinkedDocumentUnitsByParentDocumentUnitUuidAndType(
      UUID parentDocumentUnitUuid, DocumentationUnitLinkType type) {
    //    if (log.isDebugEnabled()) {
    //      log.debug(
    //          "find all linked documentation units by parent documentation uuid '{}' and type
    // '{}'",
    //          parentDocumentUnitUuid,
    //          type);
    //    }
    //
    //    return documentationUnitLinkRepository
    //        .findAllByParentDocumentationUnitUuidAndTypeOrderByIdAsc(parentDocumentUnitUuid, type)
    //        .flatMap(
    //            linkDTO ->
    //                metadataRepository
    //                    .findByUuid(linkDTO.getChildDocumentationUnitUuid())
    //                    .flatMap(this::injectAdditionalInformation)
    //                    .zipWith(Mono.just(linkDTO)))
    //        .map(
    //            tuple ->
    //                LinkedDocumentationUnitTransformer.transformToDomain(tuple.getT1(),
    // tuple.getT2()));
    return null;
  }

  @NotNull
  private static Predicate[] getPredicates(
      DocumentUnitSearchInput searchInput,
      DocumentationOfficeDTO documentationOfficeDTO,
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

    if (searchInput.courtLocation() != null) {
      restrictions.add(builder.equal(root.get("courtLocation"), searchInput.courtLocation()));
    }

    if (searchInput.courtType() != null) {
      restrictions.add(builder.equal(root.get("courtType"), searchInput.courtType()));
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

    if (searchInput.status() != null && searchInput.status().withError()) {
      Predicate statusWithError = builder.equal(root.get("withError"), true);
      restrictions.add(builder.and(myDocOffice, statusWithError));
    }

    if (searchInput.status() != null && searchInput.status().publicationStatus() != null) {
      Predicate status =
          builder.equal(root.get("publicationStatus"), searchInput.status().publicationStatus());
      if (searchInput.status().publicationStatus() == PublicationStatus.PUBLISHED) {
        status =
            root.get("publicationStatus")
                .in(PublicationStatus.PUBLISHED, PublicationStatus.JURIS_PUBLISHED);
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
                  PublicationStatus.JURIS_PUBLISHED);
      if (searchInput.myDocOfficeOnly()) {
        restrictions.add(myDocOffice);
      } else {
        restrictions.add(builder.or(myDocOffice, status));
      }
    }

    return restrictions.toArray(new Predicate[0]);
  }

  //  private Mono<DocumentUnitDTO> filterUnlinkedDocumentUnit(DocumentUnitDTO documentUnitDTO) {
  //    if (log.isDebugEnabled()) {
  //      log.debug("filter unlinked documentation unit: {}", documentUnitDTO.getUuid());
  //    }
  //
  //    return documentationUnitLinkRepository
  //        .existsByChildDocumentationUnitUuid(documentUnitDTO.getUuid())
  //        .filter(isLinked -> !isLinked)
  //        .map(isLinked -> documentUnitDTO);
  //  }

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

    //    return documentationUnitLinkRepository
    //        .findByParentDocumentationUnitUuidAndChildDocumentationUnitUuidAndType(
    //            parentDocumentUnitUuid, childDocumentUnitUuid, type)
    //        .switchIfEmpty(
    //            documentationUnitLinkRepository.save(
    //                DocumentationUnitLinkDTO.builder()
    //                    .parentDocumentationUnitUuid(parentDocumentUnitUuid)
    //                    .childDocumentationUnitUuid(childDocumentUnitUuid)
    //                    .type(type)
    //                    .build()))
    //        .map(DocumentationUnitLinkTransformer::transferToDomain);
    return null;
  }

  @Override
  public Mono<Void> unlinkDocumentUnit(
      UUID parentDocumentationUnitUuid,
      UUID childDocumentationUnitUuid,
      DocumentationUnitLinkType type) {
    //    if (log.isDebugEnabled()) {
    //      log.debug(
    //          "unlink document unit: {}, {}, {}",
    //          parentDocumentationUnitUuid,
    //          childDocumentationUnitUuid,
    //          type);
    //    }
    //
    //    return documentationUnitLinkRepository
    //        .findByParentDocumentationUnitUuidAndChildDocumentationUnitUuidAndType(
    //            parentDocumentationUnitUuid, childDocumentationUnitUuid, type)
    //        .map(DocumentationUnitLinkDTO::getId)
    //        .flatMap(documentationUnitLinkRepository::deleteById);
    return null;
  }

  @Override
  public Mono<Long> countLinksByChildDocumentUnitUuid(UUID childDocumentUnitUuid) {
    if (log.isDebugEnabled()) {
      log.debug("count links by child documentation unit uuid: {}", childDocumentUnitUuid);
    }
    return null;
    //    return
    // documentationUnitLinkRepository.countByChildDocumentationUnitUuid(childDocumentUnitUuid);
  }

  @Override
  // TODO how to delete orphaned without dataSource
  public Mono<Void> deleteIfOrphanedLinkedDocumentationUnit(UUID documentUnitUuid) {
    //    if (log.isDebugEnabled()) {
    //      log.debug("delete if orphaned linked documentation unit: {}", documentUnitUuid);
    //    }
    //
    //    return repository
    //        .findById(documentUnitUuid)
    //        .filter(
    //            childDocumentUnit ->
    //                DataSource.PROCEEDING_DECISION == childDocumentUnit.getDataSource()
    //                    || DataSource.ACTIVE_CITATION == childDocumentUnit.getDataSource())
    //        .flatMap(this::filterUnlinkedDocumentUnit)
    //        .flatMap(repository::delete);
    return null;
  }

  @Override
  public Mono<Long> count() {
    return Mono.just(repository.count());
  }

  @Override
  public Mono<Long> countSearchByLinkedDocumentationUnit(
      LinkedDocumentationUnit linkedDocumentationUnit) {
    //    return Mono.zip(
    //            extractDocumentUnitDTOIdsViaFileNumber(linkedDocumentationUnit).collectList(),
    //            extractDocumentTypeDTOId(linkedDocumentationUnit))
    //        .flatMap(
    //            tuple -> {
    //              Long[] documentUnitDTOIdsViaFileNumber =
    //                  convertListToArrayOrReturnNull(tuple.getT1());
    //              if (documentUnitDTOIdsViaFileNumber == null
    //                  && linkedDocumentationUnit.getFileNumber() != null) return Mono.just(0L);
    //
    //              UUID documentTypeDTOId = tuple.getT2().equals(new UUID(0, 0)) ? null :
    // tuple.getT2();
    //              if (documentTypeDTOId == null && linkedDocumentationUnit.getDocumentType() !=
    // null)
    //                return Mono.just(0L);
    //
    //              return metadataRepository.countSearchByLinkedDocumentationUnit(
    //                  extractCourtType(linkedDocumentationUnit),
    //                  extractCourtLocation(linkedDocumentationUnit),
    //                  extractDecisionDate(linkedDocumentationUnit),
    //                  documentUnitDTOIdsViaFileNumber,
    //                  documentTypeDTOId);
    //            });
    return null;
  }
}
