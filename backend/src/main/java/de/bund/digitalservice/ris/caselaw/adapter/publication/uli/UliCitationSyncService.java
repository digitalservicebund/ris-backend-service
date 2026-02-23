package de.bund.digitalservice.ris.caselaw.adapter.publication.uli;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class UliCitationSyncService {

  private final DatabaseDocumentationUnitRepository documentationUnitRepository;

  public UliCitationSyncService(DatabaseDocumentationUnitRepository documentationUnitRepository) {
    this.documentationUnitRepository = documentationUnitRepository;
  }

  /**
   * Case 2: Find documents that need new passive ULI citations based on active citations from other
   * streams. * @return Set of document numbers that need to be republished.
   */
  @Transactional
  public Set<String> syncUliPassiveCitations() {
    log.info("Starting ULI passive citation sync");
    Set<String> documentsToRepublish = new HashSet<>();

    // Todo: Search in uli schema for documents, that have activelinks to caselaw,
    // Add passive citation, if no existing passive citation on the caselaw side.
    // Update passive citation with metadata, if existing passive citation's metadata different to
    // uli document.
    // documentsToRepublish.addAll(documentationUnitRepository.findDocumetsMissingPassiveLinks());

    log.info(
        "ULI passive citation sync finished. Found {} documents to update.",
        documentsToRepublish.size());
    return documentsToRepublish;
  }

  /**
   * Case 3: Identify documents that point to repealed or deleted ULI documents. * @return Set of
   * document numbers (the sources of the links) that need to be republished to remove the links
   * from the portal.
   */
  @Transactional
  public Set<String> handleUliRepeals() {
    log.info("Starting ULI repeal sync");
    Set<String> documentsToRepublish = new HashSet<>();

    // Find in repeal table all ULI documentNumbers, that are the target of an activecitation or the
    // source of a
    // passive citation in caselaw and republish them.
    // TODO:
    // documentsToRepublish.addAll(documentationUnitRepository.findDocumentsPointingToRepealedUli());

    log.info(
        "ULI repeal sync finished. Found {} documents affected by repeals.",
        documentsToRepublish.size());
    return documentsToRepublish;
  }
}
