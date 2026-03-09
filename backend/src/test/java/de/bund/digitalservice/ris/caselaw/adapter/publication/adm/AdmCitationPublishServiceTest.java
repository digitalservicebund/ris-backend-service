package de.bund.digitalservice.ris.caselaw.adapter.publication.adm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ActiveCitationAdmDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AdmDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseAdmRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PassiveCitationAdmDTO;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import({AdmCitationPublishService.class})
public class AdmCitationPublishServiceTest {

  @Autowired AdmCitationPublishService admCitationPublishService;

  @MockitoBean DatabaseAdmRepository admRepository;

  @Nested
  class updatePassiveCitationSourceWithInformationFromSource {
    @Test
    void shouldReturnSameReferenceIfNoDocumentNumberIsGiven() {
      var passiveCitation =
          PassiveCitationAdmDTO.builder()
              .sourceDirective("VV DEU BMF 1972-02-29 F/IV B 2-S 2000-5/72")
              .rank(1)
              .build();

      var result =
          admCitationPublishService.updatePassiveCitationSourceWithInformationFromSource(
              passiveCitation);

      assertThat(result).contains(passiveCitation);
      assertThat(result.get().getSourceDirective())
          .isEqualTo("VV DEU BMF 1972-02-29 F/IV B 2-S 2000-5/72");
      assertThat(result.get().getSourceDocumentNumber()).isNull();
      assertThat(result.get().getCitationType()).isNull();
    }

    @Test
    void shouldReturnEmptyIfDocumentNumberIsGivenButNoAdmIsFound() {
      var passiveCitation =
          PassiveCitationAdmDTO.builder()
              .sourceId(UUID.fromString("1027f2e0-82e3-41af-93f8-35dba142c46f"))
              .sourceDocumentNumber("KSNR150060010")
              .rank(1)
              .build();

      when(admRepository.findById(UUID.fromString("1027f2e0-82e3-41af-93f8-35dba142c46f")))
          .thenReturn(Optional.empty());

      var result =
          admCitationPublishService.updatePassiveCitationSourceWithInformationFromSource(
              passiveCitation);

      assertThat(result).isEmpty();
    }

    @Test
    @Disabled(
        "this the functionality including the updating of references, but at the moment we do not want to update any data yet")
    void shouldReturnUpdatedPassiveCitationWhenAdmIsFound() {
      var uuid = UUID.fromString("8a0b92e8-2e89-4808-83da-4d85204f5cdc");
      var passiveCitation = PassiveCitationAdmDTO.builder().sourceId(uuid).rank(1).build();

      var adm =
          AdmDTO.builder()
              .id(uuid)
              .documentNumber("KSNR150060010")
              .jurisAbbreviation("VV DEU BMF 1972-02-29 F/IV B 2-S 2000-5/72")
              .build();

      when(admRepository.findById(uuid)).thenReturn(Optional.of(adm));

      var result =
          admCitationPublishService.updatePassiveCitationSourceWithInformationFromSource(
              passiveCitation);

      assertThat(result).contains(passiveCitation);
      assertThat(result.get().getSourceDocumentNumber()).isEqualTo("KSNR150060010");
      assertThat(result.get().getSourceDirective())
          .isEqualTo("VV DEU BMF 1972-02-29 F/IV B 2-S 2000-5/72");
      assertThat(result.get().getSourceId()).isEqualTo(uuid);
    }
  }

  @Nested
  class updateActiveCitationTargetWithInformationFromTarget {
    @Test
    void shouldReturnSameReferenceIfNoIdIsGiven() {
      var activeCitation =
          ActiveCitationAdmDTO.builder()
              .source(
                  DecisionDTO.builder()
                      .id(UUID.fromString("3314d0d0-936b-4832-a71b-19c7131de0db"))
                      .build())
              .targetDirective("VV DEU BMF 1972-02-29 F/IV B 2-S 2000-5/72")
              .rank(1)
              .build();

      var result =
          admCitationPublishService.updateActiveCitationTargetWithInformationFromTarget(
              activeCitation);

      assertThat(result).isEqualTo(activeCitation);
      assertThat(result.getTargetId()).isNull();
      assertThat(result.getTargetDocumentNumber()).isNull();
      assertThat(result.getTargetDirective())
          .isEqualTo("VV DEU BMF 1972-02-29 F/IV B 2-S 2000-5/72");
    }

    @Test
    void shouldReturnSameReferenceWithNullDocumentNumberAndIdIfAdmIsNotFound() {
      var uuid = UUID.fromString("9e266182-f4c0-4f7f-9987-1e7b5603aa2b");
      var activeCitation =
          ActiveCitationAdmDTO.builder()
              .source(
                  DecisionDTO.builder()
                      .id(UUID.fromString("3314d0d0-936b-4832-a71b-19c7131de0db"))
                      .build())
              .targetDirective("VV DEU BMF 1972-02-29 F/IV B 2-S 2000-5/72")
              .targetId(uuid)
              .targetDocumentNumber("KSNR150060010")
              .rank(1)
              .build();

      when(admRepository.findById(uuid)).thenReturn(Optional.empty());

      var result =
          admCitationPublishService.updateActiveCitationTargetWithInformationFromTarget(
              activeCitation);

      assertThat(result).isEqualTo(activeCitation);
      assertThat(result.getTargetDocumentNumber()).isNull();
      assertThat(result.getTargetId()).isNull();
    }

    @Test
    @Disabled("we want to start by just logging in these cases so we don't update them yet")
    void shouldReturnUpdatedActiveCitationWhenAdmIsFound() {
      var uuid = UUID.fromString("82948f54-94c7-43f2-9562-7b9efc6ce4fb");
      var activeCitation =
          ActiveCitationAdmDTO.builder()
              .targetId(uuid)
              .targetDocumentNumber("KSNR150060010")
              .rank(1)
              .build();

      var adm =
          AdmDTO.builder()
              .id(uuid)
              .documentNumber("KSNR150060010")
              .jurisAbbreviation("VV DEU BMF 1972-02-29 F/IV B 2-S 2000-5/72")
              .build();

      when(admRepository.findById(uuid)).thenReturn(Optional.of(adm));

      var result =
          admCitationPublishService.updateActiveCitationTargetWithInformationFromTarget(
              activeCitation);

      assertThat(result).isEqualTo(activeCitation);
      assertThat(result.getTargetId()).isEqualTo(uuid);
      assertThat(result.getTargetDocumentNumber()).isEqualTo("KSNR150060010");
      assertThat(result.getTargetDirective())
          .isEqualTo("VV DEU BMF 1972-02-29 F/IV B 2-S 2000-5/72");
    }
  }
}
