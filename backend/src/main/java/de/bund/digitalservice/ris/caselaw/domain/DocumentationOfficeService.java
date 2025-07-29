package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationOfficeNotExistsException;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DocumentationOfficeService {
  private final DocumentationOfficeRepository documentationOfficeRepository;

  public DocumentationOfficeService(DocumentationOfficeRepository documentationOfficeRepository) {
    this.documentationOfficeRepository = documentationOfficeRepository;
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
   * @param uuid the UUID to search for
   * @return a list of all associated process steps, ordered by rank. If none found, returns an
   *     empty list.
   * @throws DocumentationOfficeNotExistsException if no documentation office found for given UUID.
   */
  public List<ProcessStep> getProcessStepsForDocumentationOffice(UUID uuid)
      throws DocumentationOfficeNotExistsException {
    return documentationOfficeRepository.findAllProcessStepsByUuid(uuid);
  }
}
