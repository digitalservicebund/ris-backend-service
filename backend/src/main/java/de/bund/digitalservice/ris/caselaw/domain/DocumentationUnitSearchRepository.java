package de.bund.digitalservice.ris.caselaw.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

@NoRepositoryBean
public interface DocumentationUnitSearchRepository {
  /**
   * Searches for documentation units based on the provided parameters. Lazy relationships that are
   * needed for the ListItem are pre-fetched to avoid N+1 problems.
   *
   * @param searchInput the search input
   * @param pageable pagination information
   * @param oidcUser currently logged-in user
   * @return a slice of documentation units matching the search criteria
   */
  Slice<DocumentationUnitListItem> searchByDocumentationUnitSearchInput(
      DocumentationUnitSearchInput searchInput, Pageable pageable, OidcUser oidcUser);
}
