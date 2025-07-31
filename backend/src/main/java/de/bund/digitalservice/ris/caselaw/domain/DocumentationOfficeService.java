package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ProcessStepTransformer;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationOfficeNotExistsException;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class DocumentationOfficeService {
  private final DocumentationOfficeRepository documentationOfficeRepository;
  private final DatabaseDocumentationOfficeRepository databaseDocumentationOfficeRepository;

  public DocumentationOfficeService(
      DocumentationOfficeRepository documentationOfficeRepository,
      DatabaseDocumentationOfficeRepository databaseDocumentationOfficeRepository) {
    this.documentationOfficeRepository = documentationOfficeRepository;
    this.databaseDocumentationOfficeRepository = databaseDocumentationOfficeRepository;
  }

  /**
   * Returns documentation office objects in a list with optional search string.
   *
   * @param searchStr An optional search string, which filters the list.
   * @return all documentation offices containing the search string in their abbreviation
   */
  public List<DocumentationOffice> getDocumentationOffices(String searchStr) {
    if (searchStr != null && !searchStr.trim().isBlank()) {
      return documentationOfficeRepository.findBySearchStr(searchStr);
    }

    return documentationOfficeRepository.findAllOrderByAbbreviationAsc();
  }

  /**
   * Find a documentation office by its UUID
   *
   * @param uuid the UUID to search for
   * @return the found documentation office
   * @throws DocumentationOfficeNotExistsException if no documentation office found for given UUID.
   */
  public DocumentationOffice findByUuid(UUID uuid) throws DocumentationOfficeNotExistsException {
    return documentationOfficeRepository.findByUuid(uuid);
  }

  /**
   * Returns a list of all process steps associated to a documentation office, ordered by rank.
   *
   * @param docOfficeId the UUID of the docoffice
   * @return a list of all associated process steps, ordered by rank. If none found, returns an
   *     empty list.
   * @throws DocumentationOfficeNotExistsException if no documentation office found for given UUID.
   */
  @Transactional(transactionManager = "jpaTransactionManager")
  public List<ProcessStep> getProcessStepsForDocumentationOffice(UUID docOfficeId)
      throws DocumentationOfficeNotExistsException {
    DocumentationOfficeDTO docOffice =
        databaseDocumentationOfficeRepository
            .findById(docOfficeId)
            .orElseThrow(
                () ->
                    new DocumentationOfficeNotExistsException(
                        String.format(
                            "The documentation office with id %s doesn't exist.", docOfficeId)));

    return docOffice.getProcessSteps().stream().map(ProcessStepTransformer::toDomain).toList();
  }
}
