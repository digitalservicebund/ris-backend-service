package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeUserGroupRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeUserGroupDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class KeycloakUserService implements UserService {
  private final DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  private final DatabaseDocumentationOfficeUserGroupRepository
      databaseDocumentationOfficeUserGroupRepository;

  private static Map<String, String> documentationCenterClaims = Collections.emptyMap();

  public KeycloakUserService(
      DatabaseDocumentationOfficeRepository documentationOfficeRepository,
      DatabaseDocumentationOfficeUserGroupRepository documentationOfficeUserGroupRepository) {
    this.documentationOfficeRepository = documentationOfficeRepository;
    this.databaseDocumentationOfficeUserGroupRepository = documentationOfficeUserGroupRepository;
  }

  @EventListener
  public void onApplicationEvent(ContextRefreshedEvent event) {
    var userGroups = this.databaseDocumentationOfficeUserGroupRepository.findAll();
    KeycloakUserService.documentationCenterClaims =
        userGroups.stream()
            .collect(
                Collectors.toMap(
                    DocumentationOfficeUserGroupDTO::getUserGroupPathName,
                    (DocumentationOfficeUserGroupDTO group) ->
                        group.getDocumentationOffice().getAbbreviation()));
  }

  public User getUser(OidcUser oidcUser) {
    return extractDocumentationOffice(oidcUser)
        .map(documentationOffice -> createUser(oidcUser, documentationOffice))
        .orElse(createUser(oidcUser, null));
  }

  public DocumentationOffice getDocumentationOffice(OidcUser oidcUser) {
    return getUser(oidcUser).documentationOffice();
  }

  public String getEmail(OidcUser oidcUser) {
    return oidcUser.getEmail();
  }

  private User createUser(OidcUser oidcUser, DocumentationOffice documentationOffice) {
    return User.builder()
        .name(oidcUser.getAttribute("name"))
        .email(oidcUser.getEmail())
        .documentationOffice(documentationOffice)
        .build();
  }

  private Optional<DocumentationOffice> extractDocumentationOffice(OidcUser oidcUser) {
    List<String> groups = Objects.requireNonNull(oidcUser.getAttribute("groups"));
    String documentationOfficeKey =
        groups.stream()
            .filter(documentationCenterClaims::containsKey)
            .findFirst()
            .map(documentationCenterClaims::get)
            .orElse(null);

    return Optional.ofNullable(
            documentationOfficeRepository.findByAbbreviation(documentationOfficeKey))
        .map(
            documentationOfficeDTO ->
                DocumentationOffice.builder()
                    .abbreviation(documentationOfficeDTO.getAbbreviation())
                    .build());
  }
}
