package de.bund.digitalservice.ris.caselaw.domain;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.repository.NoRepositoryBean;

/** Domain repository for documentation units */
@NoRepositoryBean
public interface DocumentUnitRepository {

  /**
   * Find a documentation unit by its document number
   *
   * @param documentNumber the document number
   * @return the documentation unit found
   */
  Optional<DocumentUnit> findByDocumentNumber(String documentNumber);

  /**
   * Find a documentation unit by its UUID
   *
   * @param uuid the UUID to search for
   * @return the documentation unit found
   */
  Optional<DocumentUnit> findByUuid(UUID uuid);

  /**
   * Create a new documentation unit with the given document number and documentation office
   *
   * @param documentNumber the document number to use
   * @param documentationOffice the documentation office
   * @return the new documentation unit
   */
  DocumentUnit createNewDocumentUnit(
      String documentNumber, DocumentationOffice documentationOffice);

  /**
   * Save a documentation unit
   *
   * @param documentUnit the documentation unit to save
   */
  void save(DocumentUnit documentUnit);

  /**
   * Save the keywords of a documentation unit
   *
   * @param documentUnit the documentation unit to save the keywords for
   */
  void saveKeywords(DocumentUnit documentUnit);

  /**
   * Save the fields of law of a documentation unit
   *
   * @param documentUnit the documentation unit to save the fields of law for
   */
  void saveFieldsOfLaw(DocumentUnit documentUnit);

  /**
   * Save the procedures of a documentation unit
   *
   * @param documentUnit the documentation unit to save the procedures for
   */
  void saveProcedures(DocumentUnit documentUnit);

  /**
   * Delete a documentation unit
   *
   * @param documentUnit the documentation unit to delete
   */
  void delete(DocumentUnit documentUnit);

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
   * @param documentationOffice the documentation office of the current user
   * @param searchInput the search parameters
   * @return the search result containing the documentation units found
   */
  Slice<DocumentationUnitListItem> searchByDocumentationUnitSearchInput(
      Pageable pageable,
      DocumentationOffice documentationOffice,
      DocumentationUnitSearchInput searchInput);

  /**
   * Find existing links to a documentation unit with a given id. This can be used to check if a
   * documentation unit can safely be deleted.
   *
   * @param documentUnitUuid the UUID of the documentation unit to find links to
   * @return a map containing the relation extension (e.g. previous decision) and the number of
   *     links with this relation in the documentation unit with the given id
   */
  Map<RelatedDocumentationType, Long> getAllDocumentationUnitWhichLink(UUID documentUnitUuid);

  /**
   * Update the ECLI of a documentation unit
   *
   * @param uuid the UUID of the documentation unit to update
   * @param ecli the new ECLI
   */
  void updateECLI(UUID uuid, String ecli);
}
