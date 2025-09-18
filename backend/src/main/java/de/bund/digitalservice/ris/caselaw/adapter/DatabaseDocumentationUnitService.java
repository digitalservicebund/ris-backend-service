package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseProcessStepRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseUserRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitProcessStepDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ProcessStepDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.UserDTO;
import de.bund.digitalservice.ris.caselaw.domain.AbstractDocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitProcessStep;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitException;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatabaseDocumentationUnitService extends AbstractDocumentationUnitService {
  private final DatabaseDocumentationUnitRepository repository;
  private final DatabaseProcessStepRepository processStepRepository;
  private final DatabaseUserRepository userRepository;

  public DatabaseDocumentationUnitService(
      DatabaseDocumentationUnitRepository repository,
      DatabaseProcessStepRepository processStepRepository,
      DatabaseUserRepository userRepository) {
    super();
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
