package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ApiKeyDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseApiKeyRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ApiKeyTransformer;
import de.bund.digitalservice.ris.caselaw.domain.ApiKey;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.ImportApiKeyException;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
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
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class AuthService {

  private final UserService userService;
  private final DocumentUnitService documentUnitService;
  private final DatabaseApiKeyRepository keyRepository;
  private final DatabaseDocumentationOfficeRepository officeRepository;

  public AuthService(
      UserService userService,
      DocumentUnitService documentUnitService,
      DatabaseApiKeyRepository keyRepository,
      DatabaseDocumentationOfficeRepository officeRepository) {

    this.userService = userService;
    this.documentUnitService = documentUnitService;
    this.keyRepository = keyRepository;
    this.officeRepository = officeRepository;
  }

  @Bean
  public Function<String, Mono<Boolean>> userHasReadAccessByDocumentNumber() {
    return documentNumber ->
        Mono.defer(
            () ->
                Mono.justOrEmpty(documentUnitService.getByDocumentNumber(documentNumber))
                    .flatMap(this::userHasReadAccess)
                    .switchIfEmpty(Mono.just(false)));
  }

  @Bean
  public Function<UUID, Mono<Boolean>> userHasReadAccessByDocumentUnitUuid() {
    return uuid ->
        Mono.defer(
            () ->
                Mono.justOrEmpty(documentUnitService.getByUuid(uuid))
                    .flatMap(this::userHasReadAccess)
                    .switchIfEmpty(Mono.just(false)));
  }

  @Bean
  public Function<UUID, Mono<Boolean>> userHasWriteAccessByDocumentUnitUuid() {
    return uuid ->
        Mono.defer(
            () ->
                Mono.justOrEmpty(documentUnitService.getByUuid(uuid))
                    .flatMap(this::userHasSameDocOfficeAsDocument)
                    .defaultIfEmpty(false)
                    .onErrorReturn(false));
  }

  private Mono<Boolean> userHasReadAccess(DocumentUnit documentUnit) {
    List<PublicationStatus> published =
        List.of(PublicationStatus.PUBLISHED, PublicationStatus.PUBLISHING);
    // legacy documents are published
    return (documentUnit.status() == null
                || (documentUnit.status().publicationStatus() != null
                    && published.contains(documentUnit.status().publicationStatus()))
            ? Mono.just(true)
            : userHasSameDocOfficeAsDocument(documentUnit))
        .defaultIfEmpty(false)
        .onErrorReturn(false);
  }

  private Mono<Boolean> userHasSameDocOfficeAsDocument(DocumentUnit documentUnit) {
    return ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .map(Authentication::getPrincipal)
        .flatMap(
            principal ->
                userService
                    .getDocumentationOffice((OidcUser) principal)
                    .map(
                        userOffice ->
                            documentUnit.coreData().documentationOffice().equals(userOffice)))
        .defaultIfEmpty(false);
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

    DocumentationOffice documentationOffice = userService.getDocumentationOffice(oidcUser).block();
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

      log.error("api key doesn't belongs to user");
      throw new ImportApiKeyException("Api key doesn't belongs to user");
    }

    ApiKeyDTO apiKeyDTO = apiKeyOptional.get();

    apiKeyDTO = apiKeyDTO.toBuilder().validUntil(Instant.now()).invalidated(true).build();

    keyRepository.save(apiKeyDTO);

    apiKeyOptional = keyRepository.findFirstByUserAccountOrderByValidUntilDesc(oidcUser.getEmail());

    return apiKeyOptional
        .map(ApiKeyTransformer::transformToDomain)
        .orElseThrow(ImportApiKeyException::new);
  }
}
