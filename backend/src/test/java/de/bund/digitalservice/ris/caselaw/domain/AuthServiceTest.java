package de.bund.digitalservice.ris.caselaw.domain;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.AuthService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseApiKeyRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import({AuthService.class})
class AuthServiceTest {

  @SpyBean private AuthService service;

  @MockBean private DocumentationUnitService documentationUnitService;
  @MockBean private UserService userService;
  @MockBean private ProcedureService procedureService;
  @MockBean private DatabaseApiKeyRepository keyRepository;
  @MockBean private DatabaseDocumentationOfficeRepository officeRepository;
  @MockBean private SecurityContext securityContext;
  @MockBean private Authentication authentication;
  @MockBean private OidcUser oidcUser;

  @Test
  void testUserHasReadAccessByDocumentNumber_withStatusNull_shouldReturnTrue() {
    // Arrange
    String documentNumber = "DOC12345";
    DocumentationUnit documentationUnit =
        DocumentationUnit.builder().documentNumber(documentNumber).build();
    when(documentationUnitService.getByDocumentNumber(documentNumber))
        .thenReturn(documentationUnit);

    // Act
    Function<String, Boolean> result = service.userHasReadAccessByDocumentNumber();

    // Assert
    Assertions.assertEquals(true, result.apply("DOC12345"));
  }

  @Test
  void testUserHasReadAccessByDocumentNumber_withStatusPublished_shouldReturnTrue() {
    // Arrange
    String documentNumber = "DOC12345";
    DocumentationUnit documentationUnit =
        DocumentationUnit.builder()
            .documentNumber(documentNumber)
            .status(Status.builder().publicationStatus(PublicationStatus.PUBLISHED).build())
            .build();
    when(documentationUnitService.getByDocumentNumber(documentNumber))
        .thenReturn(documentationUnit);

    // Act
    Function<String, Boolean> result = service.userHasReadAccessByDocumentNumber();

    // Assert
    Assertions.assertEquals(true, result.apply("DOC12345"));
  }

  @Test
  void testUserHasReadAccessByDocumentNumber_withSameDocOffice_shouldReturnTrue() {
    // Arrange
    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(oidcUser);
    String documentNumber = "DOC12345";
    DocumentationOffice office = DocumentationOffice.builder().abbreviation("DS").build();
    DocumentationUnit documentationUnit =
        DocumentationUnit.builder()
            .documentNumber(documentNumber)
            .status(Status.builder().publicationStatus(PublicationStatus.DUPLICATED).build())
            .coreData(CoreData.builder().documentationOffice(office).build())
            .build();
    when(documentationUnitService.getByDocumentNumber(documentNumber))
        .thenReturn(documentationUnit);
    when(userService.getDocumentationOffice(any())).thenReturn(office);

    // Act
    Function<String, Boolean> result = service.userHasReadAccessByDocumentNumber();

    // Assert
    Assertions.assertEquals(true, result.apply(documentNumber));
  }

  @Test
  void testUserIsInternal_withInternalUser_shouldReturnTrue() {
    // Arrange
    when(oidcUser.getClaimAsStringList("roles")).thenReturn(List.of("Internal"));

    // Act
    Function<OidcUser, Boolean> result = service.userIsInternal();

    // Assert
    Assertions.assertEquals(true, result.apply(oidcUser));
  }

  @Test
  void TestUserIsInternal_withExternalUser_shouldReturnFalse() {
    // Arrange
    when(oidcUser.getClaimAsStringList("roles")).thenReturn(List.of("External"));

    // Act
    Function<OidcUser, Boolean> result = service.userIsInternal();

    // Assert
    Assertions.assertEquals(false, result.apply(oidcUser));
  }

  @Test
  void testUserHasWriteAccessByProcedureId_withSameDocOffice_shouldReturnTrue() {
    // Arrange
    UUID uuid = UUID.randomUUID();
    when(procedureService.getDocumentationOfficeByUUID(uuid))
        .thenReturn(DocumentationOffice.builder().abbreviation("DS").build());
    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(oidcUser);
    DocumentationOffice office = DocumentationOffice.builder().abbreviation("DS").build();
    when(userService.getDocumentationOffice(any())).thenReturn(office);

    // Act
    Function<UUID, Boolean> result = service.userHasWriteAccessByProcedureId();

    // Assert
    Assertions.assertEquals(true, result.apply(uuid));
  }

  @Test
  void testUserHasWriteAccessByProcedureId_withOtherDocOffice_shouldReturnTrue() {
    // Arrange
    UUID uuid = UUID.randomUUID();
    when(procedureService.getDocumentationOfficeByUUID(uuid))
        .thenReturn(DocumentationOffice.builder().abbreviation("BGH").build());
    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(oidcUser);
    DocumentationOffice office = DocumentationOffice.builder().abbreviation("DS").build();
    when(userService.getDocumentationOffice(any())).thenReturn(office);

    // Act
    Function<UUID, Boolean> result = service.userHasWriteAccessByProcedureId();

    // Assert
    Assertions.assertEquals(false, result.apply(uuid));
  }
}
