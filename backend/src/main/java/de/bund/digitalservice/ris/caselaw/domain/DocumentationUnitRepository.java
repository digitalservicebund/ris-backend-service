package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/** Domain repository for documentation units */
@NoRepositoryBean
public interface DocumentationUnitRepository {

  /**
   * Find a documentation unit by its document number
   *
   * @param documentNumber the document number
   * @return the documentation unit found (decision or pending proceeding)
   */
  DocumentationUnit findByDocumentNumber(String documentNumber)
      throws DocumentationUnitNotExistsException;

  /**
   * Find a documentation unit by its document number
   *
   * @param documentNumber the document number
   * @param user is optional and can be null
   * @return the documentation unit found (decision or pending proceeding)
   */
  DocumentationUnit findByDocumentNumber(String documentNumber, User user)
      throws DocumentationUnitNotExistsException;

  DocumentationUnitListItem findDocumentationUnitListItemByDocumentNumber(String documentNumber)
      throws DocumentationUnitNotExistsException;

  /**
   * Find a documentation unit by its UUID
   *
   * @param uuid the UUID to search for
   * @return the documentation unit found
   */
  DocumentationUnit findByUuid(UUID uuid, User user) throws DocumentationUnitNotExistsException;

  /**
   * Find a documentation unit by its UUID
   *
   * @param uuid the UUID to search for
   * @return the documentation unit found
   */
  DocumentationUnit findByUuid(UUID uuid) throws DocumentationUnitNotExistsException;

  /**
   * Create a new documentation unit with the given document number and documentation office
   *
   * @param documentationUnit the documentation unit to create
   * @param status the status of the new documentation unit
   * @param createdFromReference the reference the documentation unit is created from
   * @param fileNumber Aktenzeichen
   * @param user the {@link User}
   * @return the new documentation unit
   */
  DocumentationUnit createNewDocumentationUnit(
      DocumentationUnit documentationUnit,
      Status status,
      Reference createdFromReference,
      String fileNumber,
      User user);

  /**
   * Save a documentation unit
   *
   * @param documentationUnit the documentation unit to save
   */
  void save(DocumentationUnit documentationUnit);

  void save(DocumentationUnit documentationUnit, User currentUser);

  void save(DocumentationUnit documentationUnit, User currentUser, String description);

  /**
   * Save the keywords of a documentation unit
   *
   * @param documentationUnit the documentation unit to save the keywords for
   */
  void saveKeywords(DocumentationUnit documentationUnit);

  /**
   * Save the fields of law of a documentation unit
   *
   * @param documentationUnit the documentation unit to save the fields of law for
   */
  void saveFieldsOfLaw(DocumentationUnit documentationUnit);

  /**
   * Save the procedures of a documentation unit
   *
   * @param documentationUnit the documentation unit to save the procedures for
   */
  void saveProcedures(DocumentationUnit documentationUnit, User user);

  /**
   * Unassign all procedures of a documentation unit
   *
   * @param documentationUnitId the UUID of the documentation unit to unassign the procedures from
   */
  void unassignProcedures(UUID documentationUnitId);

  /**
   * When a documentation unit is successfully published, it leaves the inbox and gets an updated
   * publication timestamp.
   *
   * @param uuid the UUID of the documentation unit to update
   */
  void saveSuccessfulPublication(UUID uuid);

  /**
   * Save the newly assigned documentation office of a documentation unit
   *
   * @param id the UUID of the documentation unit to save the new documentation office for
   * @param documentationOffice the new {@link DocumentationOffice}
   * @param user the {@link User}
   */
  void saveDocumentationOffice(UUID id, DocumentationOffice documentationOffice, User user);

  /**
   * Delete a documentation unit
   *
   * @param documentationUnit the documentation unit to delete
   */
  void delete(DocumentationUnit documentationUnit);

  /**
   * Search for documentation units that are linkable to the given documentation unit
   *
   * @param relatedDocumentationUnit a object containing the given search parameters
   * @param documentationOffice the documentation office of the current user
   * @param documentNumberToExclude the document number (e.g. from the documentation unit that is
   *     edited) to exclude from the search
   * @param pageable the pageable to use for the search
   * @return the search result
   */
  Slice<RelatedDocumentationUnit> searchLinkableDocumentationUnits(
      RelatedDocumentationUnit relatedDocumentationUnit,
      DocumentationOffice documentationOffice,
      String documentNumberToExclude,
      Pageable pageable);

  /**
   * Search for documentation units with given search parameters
   *
   * @param pageable the pageable to use for the search
   * @param oidcUser current user via openid connect system
   * @param searchInput the search parameters
   * @return the search result containing the documentation units found
   */
  Slice<DocumentationUnitListItem> searchByDocumentationUnitSearchInput(
      Pageable pageable, OidcUser oidcUser, DocumentationUnitSearchInput searchInput);

  /**
   * Find existing links to a documentation unit with a given id. This can be used to check if a
   * documentation unit can safely be deleted.
   *
   * @param documentNumber the documentNumber of the documentation unit to find links to
   * @return a map containing the relation extension (e.g. previous decision) and the number of
   *     links with this relation in the documentation unit with the given id
   */
  Map<RelatedDocumentationType, Long> getAllRelatedDocumentationUnitsByDocumentNumber(
      String documentNumber);

  List<UUID> getRandomDocumentationUnitIds();

  /** Returns doc units with a scheduled publication date that is in the past. */
  List<Decision> getScheduledDocumentationUnitsDueNow();

  List<String> findAllDocumentNumbersByMatchingPublishCriteria();
}
