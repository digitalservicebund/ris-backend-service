package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ApiKeyDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseApiKeyRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ProcedureDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ApiKeyTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationOfficeTransformer;
import de.bund.digitalservice.ris.caselaw.domain.ApiKey;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.domain.ProcedureService;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import de.bund.digitalservice.ris.caselaw.domain.exception.ImportApiKeyException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthService {

  private final UserService userService;
  private final DocumentationUnitService documentationUnitService;
  private final ProcedureService procedureService;
  private final DatabaseApiKeyRepository keyRepository;
  private final DatabaseDocumentationOfficeRepository officeRepository;

  public AuthService(
      UserService userService,
      DocumentationUnitService documentationUnitService,
      ProcedureService procedureService,
      DatabaseApiKeyRepository keyRepository,
      DatabaseDocumentationOfficeRepository officeRepository) {

    this.userService = userService;
    this.documentationUnitService = documentationUnitService;
    this.procedureService = procedureService;
    this.keyRepository = keyRepository;
    this.officeRepository = officeRepository;
  }

  @Bean
  public Function<String, Boolean> userHasReadAccessByDocumentNumber() {
    return documentNumber ->
        Optional.ofNullable(documentationUnitService.getByDocumentNumber(documentNumber))
            .map(this::userHasReadAccess)
            .orElse(false);
  }

  @Bean
  public Function<UUID, Boolean> userHasReadAccessByDocumentationUnitId() {
    return uuid ->
        Optional.ofNullable(documentationUnitService.getByUuid(uuid))
            .map(this::userHasReadAccess)
            .orElse(false);
  }

  @Bean
  public Function<OidcUser, Boolean> userIsInternal() {
    // Todo: use role "Internal" when we have a test user.
    return oidcUser -> oidcUser.getClaimAsStringList("roles").contains("offline_access");
  }

  @Bean
  public Function<UUID, Boolean> userHasWriteAccessByProcedureId() {
    return uuid ->
        Optional.ofNullable(procedureService.getByUUID(uuid))
            .map(this::userHasSameDocOfficeAsProcedure)
            .orElse(false);
  }

  @Bean
  public Function<UUID, Boolean> userHasWriteAccessByDocumentationUnitId() {
    return uuid ->
        Optional.ofNullable(documentationUnitService.getByUuid(uuid))
            .map(this::userHasSameDocOfficeAsDocument)
            .orElse(false);
  }

  private boolean userHasReadAccess(DocumentationUnit documentationUnit) {
    List<PublicationStatus> published =
        List.of(PublicationStatus.PUBLISHED, PublicationStatus.PUBLISHING);
    // legacy documents are published
    return documentationUnit.status() == null
        || (documentationUnit.status().publicationStatus() != null
            && published.contains(documentationUnit.status().publicationStatus()))
        || userHasSameDocOfficeAsDocument(documentationUnit);
  }

  private boolean userHasSameDocOfficeAsDocument(DocumentationUnit documentationUnit) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof OidcUser principal) {
      DocumentationOffice documentationOffice = userService.getDocumentationOffice(principal);
      return documentationUnit.coreData().documentationOffice().equals(documentationOffice);
    }
    return false;
  }

  private boolean userHasSameDocOfficeAsProcedure(ProcedureDTO procedure) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof OidcUser principal) {
      DocumentationOffice documentationOfficeOfUser = userService.getDocumentationOffice(principal);
      DocumentationOffice documentationOffice =
          DocumentationOfficeTransformer.transformToDomain(procedure.getDocumentationOffice());
      return documentationOffice.equals(documentationOfficeOfUser);
    }
    return false;
  }

  /**
   * Generate an api key for the importer and for the user if the user doesn't have a valid one.
   * <br>
   * In the case of the existence of a key the method throws a {@link ImportApiKeyException}. <br>
   * The key was generated is 64 character long and contains a random sequence of lowercase and
   * uppercase letters and digits. The key is 30 days valid.
   *
   * @param oidcUser current user via openid connect system
   * @return the generated api key
   */
  public ApiKey generateImportApiKey(OidcUser oidcUser) {
    Optional<ApiKeyDTO> apiKeyOptional =
        keyRepository.findByUserAccountAndValidUntilAfter(oidcUser.getEmail(), Instant.now());

    if (apiKeyOptional.isPresent()) {
      log.error(
          "No new import api key couldn't generate because a valid api key for the user exist.");
      throw new ImportApiKeyException(
          "No new import api key couldn't generate because a valid api key for the user exist.");
    }

    char[][] allowedCharacters = {{'a', 'z'}, {'A', 'Z'}, {'0', '9'}};
    // " !\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~"
    RandomStringGenerator rsg =
        RandomStringGenerator.builder().withinRange(allowedCharacters).get();

    DocumentationOffice documentationOffice = userService.getDocumentationOffice(oidcUser);
    DocumentationOfficeDTO documentationOfficeDTO = null;
    if (documentationOffice != null) {
      documentationOfficeDTO =
          officeRepository.findByAbbreviation(documentationOffice.abbreviation());
    }

    ApiKeyDTO apiKeyDTO =
        ApiKeyDTO.builder()
            .apiKey(rsg.generate(64))
            .userAccount(oidcUser.getEmail())
            .documentationOffice(documentationOfficeDTO)
            .createdAt(Instant.now())
            .validUntil(Instant.now().plus(30, ChronoUnit.DAYS))
            .build();

    keyRepository.save(apiKeyDTO);

    return ApiKeyTransformer.transformToDomain(apiKeyDTO);
  }

  /**
   * Get the api key for the importer and the user.
   *
   * @param oidcUser current user via openid connect system
   * @return the last/current valid api key or null, if no key exists
   */
  public ApiKey getImportApiKey(OidcUser oidcUser) {
    Optional<ApiKeyDTO> apiKeyOptional =
        keyRepository.findFirstByUserAccountOrderByValidUntilDesc(oidcUser.getEmail());
    return apiKeyOptional.map(ApiKeyTransformer::transformToDomain).orElse(null);
  }

  /**
   * Invalidate the api key for the importer and the user. <br>
   * Throws an {@link ImportApiKeyException} if the api key doesn't exist in the database or the api
   * key doesn't belong to the user or after the invalidation no api key exist for the user.
   *
   * @param oidcUser current user via openid connect system
   * @param apiKey api key which should invalidated
   * @return the last/current valid api key or throws an {@link ImportApiKeyException}, if no key
   *     exists
   */
  public ApiKey invalidateImportApiKey(OidcUser oidcUser, String apiKey) {
    Optional<ApiKeyDTO> apiKeyOptional = keyRepository.findByApiKey(apiKey);

    if (apiKeyOptional.isEmpty()) {
      log.error("Can't invalidate api key '{}' because it doesn't exist!", apiKey);
      throw new ImportApiKeyException("Can't invalidate api key because it doesn't exist!");
    }

    if (apiKeyOptional.get().getUserAccount() == null
        || !apiKeyOptional.get().getUserAccount().equals(oidcUser.getEmail())) {

      log.error("api key doesn't belong to user");
      throw new ImportApiKeyException("Api key doesn't belong to user");
    }

    ApiKeyDTO apiKeyDTO =
        apiKeyOptional.get().toBuilder().validUntil(Instant.now()).invalidated(true).build();
    keyRepository.save(apiKeyDTO);

    apiKeyOptional = keyRepository.findFirstByUserAccountOrderByValidUntilDesc(oidcUser.getEmail());
    return apiKeyOptional
        .map(ApiKeyTransformer::transformToDomain)
        .orElseThrow(ImportApiKeyException::new);
  }
}
