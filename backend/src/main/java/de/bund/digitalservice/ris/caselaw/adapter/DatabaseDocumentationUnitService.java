package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseProcessStepRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseUserRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitProcessStepDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ProcessStepDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.UserDTO;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentService;
import de.bund.digitalservice.ris.caselaw.domain.AuthService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberRecyclingService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentTypeService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOfficeService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitHistoryLogService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitSearchRepository;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitStatusService;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateCheckService;
import de.bund.digitalservice.ris.caselaw.domain.ProcessStep;
import de.bund.digitalservice.ris.caselaw.domain.ProcessStepService;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.TransformationService;
import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitException;
import de.bund.digitalservice.ris.caselaw.domain.mapper.PatchMapperService;
import jakarta.transaction.Transactional;
import jakarta.validation.Validator;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class DatabaseDocumentationUnitService extends DocumentationUnitService {
  private final DatabaseDocumentationUnitRepository repository;
  private final DatabaseProcessStepRepository processStepRepository;
  private final DatabaseUserRepository userRepository;

  public DatabaseDocumentationUnitService(
      DatabaseDocumentationUnitRepository repository,
      DocumentationUnitRepository documentationUnitRepository,
      DocumentNumberService documentNumberService,
      DocumentTypeService documentTypeService,
      DocumentationUnitStatusService statusService,
      DocumentNumberRecyclingService documentNumberRecyclingService,
      UserService userService,
      Validator validator,
      AttachmentService attachmentService,
      TransformationService transformationService,
      @Lazy AuthService authService,
      PatchMapperService patchMapperService,
      DuplicateCheckService duplicateCheckService,
      DocumentationOfficeService documentationOfficeService,
      DocumentationUnitHistoryLogService historyLogService,
      DocumentationUnitSearchRepository docUnitSearchRepository,
      ProcessStepService processStepService,
      DatabaseProcessStepRepository processStepRepository,
      DatabaseUserRepository userRepository) {
    super(
        documentationUnitRepository,
        documentNumberService,
        documentTypeService,
        statusService,
        documentNumberRecyclingService,
        userService,
        validator,
        attachmentService,
        transformationService,
        authService,
        patchMapperService,
        duplicateCheckService,
        documentationOfficeService,
        historyLogService,
        docUnitSearchRepository,
        processStepService);
    this.repository = repository;
    this.processStepRepository = processStepRepository;
    this.userRepository = userRepository;
  }

  @Override
  @Transactional
  public UUID[] assignProcessStepAndUser(
      User currentUser, List<UUID> documentationUnitIds, ProcessStep processStep, User user) {
    List<DocumentationUnitDTO> documentationUnitDTOList = new ArrayList<>();
    List<UUID> documentationUnitIdsWithErrors = new ArrayList<>();

    documentationUnitIds.forEach(
        documentationUnitId -> {
          Optional<DocumentationUnitDTO> documentationUnitDTO =
              repository.findById(documentationUnitId);
          if (documentationUnitDTO.isPresent()
              && checkRightsToChangeDocumentationUnit(documentationUnitDTO.get(), currentUser)) {
            documentationUnitDTOList.add(documentationUnitDTO.get());
          } else {
            documentationUnitIdsWithErrors.add(documentationUnitId);
          }
        });

    if (!documentationUnitIdsWithErrors.isEmpty()) {
      return documentationUnitIdsWithErrors.toArray(new UUID[0]);
    }

    Optional<ProcessStepDTO> processStepDTO = processStepRepository.findByName(processStep.name());
    if (processStepDTO.isEmpty()) {
      throw new DocumentationUnitException(
          "Process step with name " + processStep.name() + " not found");
    }

    Optional<UserDTO> userDTO = userRepository.findByExternalId(user.externalId());
    if (userDTO.isEmpty()) {
      throw new DocumentationUnitException("User with id " + user.externalId() + " not found");
    }

    documentationUnitDTOList.forEach(
        documentationUnitDTO -> {
          DocumentationUnitProcessStepDTO newProcessStep =
              DocumentationUnitProcessStepDTO.builder()
                  .processStep(processStepDTO.get())
                  .documentationUnit(documentationUnitDTO)
                  .user(userDTO.get())
                  .createdAt(LocalDateTime.now())
                  .build();
          documentationUnitDTO.setCurrentProcessStep(newProcessStep);
          documentationUnitDTO.getProcessSteps().add(newProcessStep);
        });

    repository.saveAll(documentationUnitDTOList);

    return null;
  }

  private boolean checkRightsToChangeDocumentationUnit(
      DocumentationUnitDTO documentationUnitDTO, User currentUser) {
    if (!documentationUnitDTO
        .getDocumentationOffice()
        .getId()
        .equals(currentUser.documentationOffice().id())) {
      return false;
    }

    return documentationUnitDTO.getStatus().getPublicationStatus()
        != PublicationStatus.EXTERNAL_HANDOVER_PENDING;
  }
}
