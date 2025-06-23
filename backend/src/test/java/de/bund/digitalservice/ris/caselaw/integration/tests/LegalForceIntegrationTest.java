package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseLegalForceTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseNormAbbreviationRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseRegionRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.LegalForceTypeTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.NormAbbreviationTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.RegionTransformer;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.LegalForce;
import de.bund.digitalservice.ris.caselaw.domain.NormReference;
import de.bund.digitalservice.ris.caselaw.domain.SingleNorm;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.LegalForceType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.NormAbbreviation;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.Region;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

@Sql(scripts = {"classpath:doc_office_init.sql", "classpath:legal_force_init.sql"})
@Sql(
    scripts = {"classpath:legal_force_cleanup.sql"},
    executionPhase = AFTER_TEST_METHOD)
class LegalForceIntegrationTest extends BaseIntegrationTest {
  @Autowired private RisWebTestClient risWebTestClient;
  @Autowired private DatabaseDocumentationUnitRepository repository;
  @Autowired private DatabaseNormAbbreviationRepository normAbbreviationRepository;
  @Autowired private DatabaseLegalForceTypeRepository legalForceTypeRepository;
  @Autowired private DatabaseRegionRepository regionRepository;

  @Transactional
  @Test
  void getNormReference_withoutLegalForce() {
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/documentnr001")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().contentRelatedIndexing().norms()).hasSize(1);
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .norms()
                          .get(0)
                          .singleNorms()
                          .get(0)
                          .legalForce())
                  .isNull();
            });
  }

  @Transactional
  @Test
  void getNormReference_withLegalForce() {
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/documentnr002")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().contentRelatedIndexing().norms()).hasSize(1);
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .norms()
                          .get(0)
                          .singleNorms()
                          .get(0)
                          .legalForce())
                  .isNotNull();
            });
  }

  @Transactional
  @Test
  void updateNormReference_addNewLegalForce() {
    NormAbbreviation normAbbreviation =
        NormAbbreviationTransformer.transformToDomain(
            normAbbreviationRepository
                .findById(UUID.fromString("33333333-2222-3333-4444-555555555555"))
                .get());

    LegalForceType legalForceType =
        LegalForceTypeTransformer.transformToDomain(
            legalForceTypeRepository
                .findById(UUID.fromString("11111111-2222-3333-4444-555555555555"))
                .get());

    Region region =
        RegionTransformer.transformDTO(
            regionRepository
                .findById(UUID.fromString("55555555-2222-3333-4444-555555555555"))
                .get());

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3"))
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .norms(
                        List.of(
                            NormReference.builder()
                                .normAbbreviation(normAbbreviation)
                                .singleNorms(
                                    List.of(
                                        SingleNorm.builder()
                                            .singleNorm("single norm")
                                            .legalForce(
                                                LegalForce.builder()
                                                    .region(region)
                                                    .type(legalForceType)
                                                    .build())
                                            .build()))
                                .build()))
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3")
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response ->
                assertThat(
                        response
                            .getResponseBody()
                            .contentRelatedIndexing()
                            .norms()
                            .get(0)
                            .singleNorms()
                            .get(0)
                            .legalForce())
                    .isNotNull());
  }

  @Transactional
  @Test
  void updateNormReference_addNewLegalForce_withoutNormReference() {
    LegalForceType legalForceType =
        LegalForceTypeTransformer.transformToDomain(
            legalForceTypeRepository
                .findById(UUID.fromString("11111111-2222-3333-4444-555555555555"))
                .get());

    Region region =
        RegionTransformer.transformDTO(
            regionRepository
                .findById(UUID.fromString("55555555-2222-3333-4444-555555555555"))
                .get());

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3"))
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .norms(
                        List.of(
                            NormReference.builder()
                                .singleNorms(
                                    List.of(
                                        SingleNorm.builder()
                                            .singleNorm("single norm")
                                            .legalForce(
                                                LegalForce.builder()
                                                    .region(region)
                                                    .type(legalForceType)
                                                    .build())
                                            .build()))
                                .build()))
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3")
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .is5xxServerError();
  }

  @Transactional
  @Test
  void updateNormReference_updateExistingLegalForce() {
    NormAbbreviation normAbbreviation =
        NormAbbreviationTransformer.transformToDomain(
            normAbbreviationRepository
                .findById(UUID.fromString("33333333-2222-3333-4444-555555555555"))
                .get());

    LegalForceType legalForceType =
        LegalForceTypeTransformer.transformToDomain(
            legalForceTypeRepository
                .findById(UUID.fromString("11111111-2222-3333-4444-555555555555"))
                .get());

    Region region =
        RegionTransformer.transformDTO(
            regionRepository
                .findById(UUID.fromString("55555555-2222-3333-4444-555555555555"))
                .get());

    LegalForce legalForce = LegalForce.builder().region(region).type(legalForceType).build();

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3"))
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .norms(
                        List.of(
                            NormReference.builder()
                                .normAbbreviation(normAbbreviation)
                                .singleNorms(
                                    List.of(
                                        SingleNorm.builder()
                                            .singleNorm("single norm")
                                            .legalForce(legalForce)
                                            .build()))
                                .build()))
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3")
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response ->
                assertThat(
                        response
                            .getResponseBody()
                            .contentRelatedIndexing()
                            .norms()
                            .get(0)
                            .singleNorms()
                            .get(0)
                            .legalForce())
                    .isNotNull());

    DocumentationUnitDTO result =
        repository.findById(UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3")).get();

    assertThat(result.getNormReferences()).hasSize(1);

    assertThat(result.getNormReferences().get(0).getLegalForce().getNormReference()).isNotNull();
  }

  @Transactional
  @Test
  void updateNormReference_deleteLegalForce() {
    NormAbbreviation normAbbreviation =
        NormAbbreviationTransformer.transformToDomain(
            normAbbreviationRepository
                .findById(UUID.fromString("33333333-2222-3333-4444-555555555555"))
                .get());

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3"))
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .norms(
                        List.of(
                            NormReference.builder()
                                .normAbbreviation(normAbbreviation)
                                .singleNorms(
                                    List.of(SingleNorm.builder().singleNorm("single norm").build()))
                                .build()))
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3")
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response ->
                assertThat(
                        response
                            .getResponseBody()
                            .contentRelatedIndexing()
                            .norms()
                            .get(0)
                            .singleNorms()
                            .get(0)
                            .legalForce())
                    .isNull());

    DocumentationUnitDTO result =
        repository.findById(UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3")).get();

    assertThat(result.getNormReferences()).hasSize(1);

    assertThat(result.getNormReferences().get(0).getLegalForce()).isNull();
  }

  @Transactional
  @Test
  void updateNormReference_deleteLegalForceRegion() {

    NormAbbreviation normAbbreviation =
        NormAbbreviationTransformer.transformToDomain(
            normAbbreviationRepository
                .findById(UUID.fromString("33333333-2222-3333-4444-555555555555"))
                .get());

    LegalForceType legalForceType =
        LegalForceTypeTransformer.transformToDomain(
            legalForceTypeRepository
                .findById(UUID.fromString("11111111-2222-3333-4444-555555555555"))
                .get());

    LegalForce legalForce = LegalForce.builder().type(legalForceType).build();

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3"))
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .norms(
                        List.of(
                            NormReference.builder()
                                .normAbbreviation(normAbbreviation)
                                .singleNorms(
                                    List.of(
                                        SingleNorm.builder()
                                            .singleNorm("single norm")
                                            .legalForce(legalForce)
                                            .build()))
                                .build()))
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3")
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .norms()
                          .get(0)
                          .singleNorms()
                          .get(0)
                          .legalForce())
                  .isNotNull();
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .norms()
                          .get(0)
                          .singleNorms()
                          .get(0)
                          .legalForce()
                          .region())
                  .isNull();
            });
  }

  @Transactional
  @Test
  void updateNormReference_deleteLegalForceType() {

    NormAbbreviation normAbbreviation =
        NormAbbreviationTransformer.transformToDomain(
            normAbbreviationRepository
                .findById(UUID.fromString("33333333-2222-3333-4444-555555555555"))
                .get());

    Region region =
        RegionTransformer.transformDTO(
            regionRepository
                .findById(UUID.fromString("55555555-2222-3333-4444-555555555555"))
                .get());

    LegalForce legalForce = LegalForce.builder().region(region).build();

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3"))
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .norms(
                        List.of(
                            NormReference.builder()
                                .normAbbreviation(normAbbreviation)
                                .singleNorms(
                                    List.of(
                                        SingleNorm.builder()
                                            .singleNorm("single norm")
                                            .legalForce(legalForce)
                                            .build()))
                                .build()))
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3")
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .norms()
                          .get(0)
                          .singleNorms()
                          .get(0)
                          .legalForce())
                  .isNotNull();
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .norms()
                          .get(0)
                          .singleNorms()
                          .get(0)
                          .legalForce()
                          .type())
                  .isNull();
            });
  }
}
