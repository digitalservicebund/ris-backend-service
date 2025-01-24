package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class LegalPeriodicalEditionService {
  private final LegalPeriodicalEditionRepository legalPeriodicalRepository;
  private final AuthService authService;

  public LegalPeriodicalEditionService(
      LegalPeriodicalEditionRepository legalPeriodicalRepository, AuthService authService) {
    this.legalPeriodicalRepository = legalPeriodicalRepository;
    this.authService = authService;
  }

  public Optional<LegalPeriodicalEdition> getById(OidcUser oidcUser, UUID id) {
    // Fetch the edition from the repository
    Optional<LegalPeriodicalEdition> edition = legalPeriodicalRepository.findById(id);

    // Transform the edition only if it is present
    return edition.map(
        e -> {
          // Update the references by transforming the documentationUnit property of each Reference
          List<Reference> updatedReferences =
              e.references().stream()
                  .map(
                      reference ->
                          reference.toBuilder() // Use toBuilder() to modify Reference
                              .documentationUnit(
                                  addPermissions(
                                      oidcUser,
                                      reference.documentationUnit())) // Update documentationUnit
                              .build()) // Build the updated Reference
                  .collect(Collectors.toList()); // NOSONAR: is correct for mutable list

          // Return a new LegalPeriodicalEdition with updated references
          return e.toBuilder().references(updatedReferences).build();
        });
  }

  public List<LegalPeriodicalEdition> getLegalPeriodicalEditions(UUID legalPeriodicalId) {
    return legalPeriodicalRepository.findAllByLegalPeriodicalId(legalPeriodicalId);
  }

  @Transactional(transactionManager = "jpaTransactionManager")
  public LegalPeriodicalEdition saveLegalPeriodicalEdition(
      OidcUser oidcUser, LegalPeriodicalEdition legalPeriodicalEdition) {

    LegalPeriodicalEdition edition = legalPeriodicalRepository.save(legalPeriodicalEdition);
    // Transform each Reference by updating its documentationUnit property
    List<Reference> updatedReferences =
        edition.references().stream()
            .map(
                reference ->
                    reference.toBuilder() // Use toBuilder() to modify Reference
                        .documentationUnit(
                            addPermissions(
                                oidcUser,
                                reference.documentationUnit())) // Update documentationUnit
                        .build()) // Build the updated Reference
            .collect(Collectors.toList()); // NOSONAR: is correct for mutable list

    return edition.toBuilder().references(updatedReferences).build();
  }

  private RelatedDocumentationUnit addPermissions(
      OidcUser oidcUser, RelatedDocumentationUnit relatedDocumentationUnit) {

    boolean hasReadAccess =
        authService.userHasReadAccess(
            oidcUser,
            relatedDocumentationUnit.creatingDocOffice,
            relatedDocumentationUnit.documentationOffice,
            relatedDocumentationUnit.status);

    return relatedDocumentationUnit.toBuilder().hasPreviewAccess(hasReadAccess).build();
  }

  public boolean delete(UUID editionId) {
    var edition = legalPeriodicalRepository.findById(editionId);
    if (edition.isPresent() && edition.get().references().isEmpty()) {
      legalPeriodicalRepository.delete(edition.get());
      return true;
    }
    return false;
  }
}
