package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseProcessStepRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseUserRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
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
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitProcessStep;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitSearchRepository;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitStatusService;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateCheckService;
import de.bund.digitalservice.ris.caselaw.domain.ProcessStepService;
import de.bund.digitalservice.ris.caselaw.domain.TransformationService;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitException;
import de.bund.digitalservice.ris.caselaw.domain.mapper.PatchMapperService;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.coyote.BadRequestException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

  @Transactional(rollbackFor = BadRequestException.class)
  @Override
  public void bulkAssignProcessStep(
      @NotNull List<UUID> documentationUnitIds,
      DocumentationUnitProcessStep documentationUnitProcessStep)
      throws BadRequestException {

    Optional<ProcessStepDTO> processStepDTO =
        processStepRepository.findByName(documentationUnitProcessStep.getProcessStep().name());
    if (processStepDTO.isEmpty()) {
      throw new DocumentationUnitException(
          "Process step with name "
              + documentationUnitProcessStep.getProcessStep().name()
              + " not found");
    }

    Optional<UserDTO> userDTO;
    if (documentationUnitProcessStep.getUser() != null) {
      userDTO =
          userRepository.findByExternalId(documentationUnitProcessStep.getUser().externalId());
      if (userDTO.isEmpty()) {
        throw new DocumentationUnitException(
            "User with id " + documentationUnitProcessStep.getUser().externalId() + " not found");
      }
    } else {
      userDTO = Optional.empty();
    }

    for (UUID documentationUnitId : documentationUnitIds) {
      DocumentationUnitDTO documentationUnitDTO =
          repository.findById(documentationUnitId).orElse(null);
      if (documentationUnitDTO instanceof DecisionDTO) {
        DocumentationUnitProcessStepDTO newProcessStep =
            DocumentationUnitProcessStepDTO.builder()
                .processStep(processStepDTO.get())
                .documentationUnit(documentationUnitDTO)
                .user(userDTO.orElse(null))
                .createdAt(LocalDateTime.now())
                .build();
        documentationUnitDTO.setCurrentProcessStep(newProcessStep);
        documentationUnitDTO.getProcessSteps().add(newProcessStep);
        repository.save(documentationUnitDTO);
      } else {
        throw new BadRequestException("Can only assign process steps to decisions.");
      }
    }
  }
}
