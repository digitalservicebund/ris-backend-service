package de.bund.digitalservice.ris.caselaw.adapter.publication.adm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ActiveCitationAdmDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AdmActiveCaselawReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AdmDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CitationTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseAdmRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseCitationTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PassiveCitationAdmDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.RevokedAdm;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.RevokedAdmRepository;
import de.bund.digitalservice.ris.caselaw.adapter.publication.PortalPublicationService;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import({AdmCitationSyncService.class})
public class AdmCitationSyncServiceTest {

  @Autowired AdmCitationSyncService admCitationSyncService;

  @MockitoBean DatabaseDocumentationUnitRepository caselawRepository;
  @MockitoBean DatabaseAdmRepository admRepository;
  @MockitoBean RevokedAdmRepository revokedAdmRepository;
  @MockitoBean DatabaseCitationTypeRepository citationTypeRepository;
  @MockitoBean PortalPublicationService portalPublicationService;

  @Nested
  class handleNewlyPublishedAfter {

    @Test
    void shouldUpdateMetadataWhenPassiveCitationExists()
        throws DocumentationUnitNotExistsException {
      UUID admId = UUID.fromString("7475e016-082d-49e9-9b22-a275c9f65934");
      UUID caselawId = UUID.fromString("2fb43fd9-6414-4407-b705-1c474f0a1c7f");
      Instant now = Instant.now();

      var adm =
          AdmDTO.builder()
              .id(admId)
              .documentNumber("KSNR150060010")
              .jurisAbbreviation("VV DEU BMF 1972-02-29 F/IV B 2-S 2000-5/72")
              .publishedAt(now)
              .build();
      var admActiveCaselawReference =
          AdmActiveCaselawReferenceDTO.builder()
              .citationType("Vgl")
              .targetDocumentationUnitId(caselawId)
              .source(adm)
              .build();
      adm.setActiveCaselawReferences(List.of(admActiveCaselawReference));

      PassiveCitationAdmDTO passiveCitation =
          PassiveCitationAdmDTO.builder()
              .sourceId(admId)
              .sourceDirective("VV DEU OLD 1900-00-00")
              .citationType(CitationTypeDTO.builder().abbreviation("Vgl").build())
              .build();

      DecisionDTO decision =
          DecisionDTO.builder()
              .id(caselawId)
              .documentNumber("WBRE410005137")
              .passiveAdmCitations(new ArrayList<>(List.of(passiveCitation)))
              .build();

      when(admRepository.findAllByPublishedAtAfter(any())).thenReturn(List.of(adm));
      when(caselawRepository.findById(caselawId)).thenReturn(Optional.of(decision));

      // Execute
      admCitationSyncService.handleNewlyPublishedAfter(Instant.now());

      // Verify
      assertThat(decision.getPassiveAdmCitations()).hasSize(1).containsExactly(passiveCitation);
      assertThat(passiveCitation.getSourceId()).isEqualTo(admId);
      assertThat(passiveCitation.getSourceDocumentNumber()).isEqualTo("KSNR150060010");
      assertThat(passiveCitation.getSourceDirective())
          .isEqualTo("VV DEU BMF 1972-02-29 F/IV B 2-S 2000-5/72");

      verify(caselawRepository).save(decision);
      verify(portalPublicationService).publishDocumentationUnit("WBRE410005137");
    }

    @Test
    void shouldCreateMissingPassiveCitation() throws DocumentationUnitNotExistsException {
      UUID admId = UUID.fromString("7475e016-082d-49e9-9b22-a275c9f65934");
      UUID caselawId = UUID.fromString("2fb43fd9-6414-4407-b705-1c474f0a1c7f");
      Instant now = Instant.now();

      var adm =
          AdmDTO.builder()
              .id(admId)
              .documentNumber("KSNR150060010")
              .jurisAbbreviation("VV DEU BMF 1972-02-29 F/IV B 2-S 2000-5/72")
              .publishedAt(now)
              .build();
      var admActiveCaselawReference =
          AdmActiveCaselawReferenceDTO.builder()
              .citationType("Vgl")
              .targetDocumentationUnitId(caselawId)
              .source(adm)
              .build();
      adm.setActiveCaselawReferences(List.of(admActiveCaselawReference));

      DecisionDTO decision =
          DecisionDTO.builder()
              .id(caselawId)
              .documentNumber("WBRE410005137")
              .passiveAdmCitations(new ArrayList<>())
              .build();

      var citationType = CitationTypeDTO.builder().abbreviation("Vgl").build();

      when(admRepository.findAllByPublishedAtAfter(any())).thenReturn(List.of(adm));
      when(caselawRepository.findById(caselawId)).thenReturn(Optional.of(decision));
      when(citationTypeRepository.findByAbbreviation("Vgl")).thenReturn(Optional.of(citationType));

      // Execute
      admCitationSyncService.handleNewlyPublishedAfter(Instant.now());

      // Verify
      assertThat(decision.getPassiveAdmCitations()).hasSize(1);
      var passiveCitation = decision.getPassiveAdmCitations().getFirst();
      assertThat(passiveCitation.getSourceId()).isEqualTo(admId);
      assertThat(passiveCitation.getSourceDocumentNumber()).isEqualTo("KSNR150060010");
      assertThat(passiveCitation.getSourceDirective())
          .isEqualTo("VV DEU BMF 1972-02-29 F/IV B 2-S 2000-5/72");
      assertThat(passiveCitation.getCitationType()).isEqualTo(citationType);
      assertThat(passiveCitation.getRank()).isEqualTo(1);

      verify(caselawRepository).save(decision);
      verify(portalPublicationService).publishDocumentationUnit("WBRE410005137");
    }

    @Test
    void shouldDoNothingIfNoNewAdmsFound() throws DocumentationUnitNotExistsException {
      when(admRepository.findAllByPublishedAtAfter(any())).thenReturn(List.of());

      admCitationSyncService.handleNewlyPublishedAfter(Instant.now());

      verify(caselawRepository, never()).save(any());
      verify(portalPublicationService, never()).publishDocumentationUnit(any());
    }
  }

  @Nested
  class handleRevokedAfter {

    @Test
    void shouldRemovePassiveCitationsWhenAdmIsRevoked() throws DocumentationUnitNotExistsException {
      UUID revokedUuid = UUID.randomUUID();
      Instant now = Instant.now();

      // Setup Delta for Revoked ADM
      RevokedAdm revokedEntry = RevokedAdm.builder().docUnitId(revokedUuid).revokedAt(now).build();
      when(revokedAdmRepository.findAllByRevokedAtAfter(any())).thenReturn(List.of(revokedEntry));

      // Setup Decision that has this ADM as a passive citation
      PassiveCitationAdmDTO passiveToStay =
          PassiveCitationAdmDTO.builder().sourceId(UUID.randomUUID()).build();
      PassiveCitationAdmDTO passiveToRemove =
          PassiveCitationAdmDTO.builder().sourceId(revokedUuid).build();

      List<PassiveCitationAdmDTO> passiveCitations =
          new ArrayList<>(List.of(passiveToStay, passiveToRemove));

      DecisionDTO decision =
          DecisionDTO.builder()
              .id(UUID.fromString("4eb14cae-7103-4c39-98f6-eff2e79de573"))
              .documentNumber("WBRE410005137")
              .passiveAdmCitations(passiveCitations)
              .build();

      when(caselawRepository.findAllByPassiveAdmSourceIdAndPendingRevocation(revokedUuid))
          .thenReturn(List.of(decision));

      // Execute
      admCitationSyncService.handleRevokedAfter(Instant.now());

      // Verify
      assertThat(decision.getPassiveAdmCitations()).hasSize(1);
      assertThat(decision.getPassiveAdmCitations()).containsExactly(passiveToStay);

      verify(caselawRepository).save(decision);
      verify(portalPublicationService).publishDocumentationUnit("WBRE410005137");
    }

    @Test
    void shouldRepublishDecisionWhenActiveAdmTargetIsRevoked()
        throws DocumentationUnitNotExistsException {
      UUID revokedUuid = UUID.randomUUID();

      RevokedAdm revokedEntry =
          RevokedAdm.builder().docUnitId(revokedUuid).revokedAt(Instant.now()).build();
      when(revokedAdmRepository.findAllByRevokedAtAfter(any())).thenReturn(List.of(revokedEntry));

      // Decision targets an ADM that was revoked (Active Citation)
      DecisionDTO decision =
          DecisionDTO.builder()
              .id(UUID.fromString("6c2447a7-e155-4c6b-9244-37f0d40d8435"))
              .documentNumber("WBRE410005137")
              .activeAdmCitations(
                  List.of(ActiveCitationAdmDTO.builder().targetId(revokedUuid).build()))
              .build();

      when(caselawRepository.findAllByPassiveAdmSourceIdAndPendingRevocation(any()))
          .thenReturn(List.of());
      when(caselawRepository.findAllByActiveAdmTargetIdAndPendingRevocation(revokedUuid))
          .thenReturn(List.of(decision));

      // Execute
      admCitationSyncService.handleRevokedAfter(Instant.now());

      // Verify: Document is marked for republish, but nothing is removed/saved inside the doc
      verify(caselawRepository, never()).save(any());
      verify(portalPublicationService).publishDocumentationUnit("WBRE410005137");
    }
  }
}
