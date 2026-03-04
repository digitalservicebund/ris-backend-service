package de.bund.digitalservice.ris.caselaw.adapter.publication.sli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ActiveCitationSliEntity;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseSliRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PassiveCitationSliEntity;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.SliDTO;
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
@Import({SliCitationPublishService.class})
public class SliCitationPublishServiceTest {

  @Autowired SliCitationPublishService sliCitationPublishService;

  @MockitoBean DatabaseSliRepository sliRepository;

  @Nested
  class updatePassiveCitationSourceWithInformationFromSource {
    @Test
    void shouldReturnSameReferenceIfNoDocumentNumberIsGiven() {
      var passiveCitation =
          PassiveCitationSliEntity.builder()
              .sourceAuthor("Gernhuber")
              .sourceBookTitle("Rechtsprechung, Erlasse und Gesetzesänderungen (12)")
              .rank(1)
              .target(DecisionDTO.builder().build())
              .build();

      var result =
          sliCitationPublishService.updatePassiveCitationSourceWithInformationFromSource(
              passiveCitation);

      assertThat(result).contains(passiveCitation);
      assertThat(result.get().getSourceAuthor()).isEqualTo("Gernhuber");
      assertThat(result.get().getSourceBookTitle())
          .isEqualTo("Rechtsprechung, Erlasse und Gesetzesänderungen (12)");
      assertThat(result.get().getSourceYearOfPublication()).isNull();
      assertThat(result.get().getSourceDocumentNumber()).isNull();
    }

    @Test
    void shouldReturnEmptyIfDocumentNumberIsGivenButNoSliIsFound() {
      var passiveCitation =
          PassiveCitationSliEntity.builder()
              .sourceId(UUID.fromString("1027f2e0-82e3-41af-93f8-35dba142c46f"))
              .sourceDocumentNumber("KSNR150060010")
              .sourceAuthor("Gernhuber")
              .sourceBookTitle("Rechtsprechung, Erlasse und Gesetzesänderungen (12)")
              .target(DecisionDTO.builder().build())
              .rank(1)
              .build();

      when(sliRepository.findById(UUID.fromString("1027f2e0-82e3-41af-93f8-35dba142c46f")))
          .thenReturn(Optional.empty());

      var result =
          sliCitationPublishService.updatePassiveCitationSourceWithInformationFromSource(
              passiveCitation);

      assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnUpdatedPassiveCitationWhenSliIsFound() {
      var uuid = UUID.fromString("8a0b92e8-2e89-4808-83da-4d85204f5cdc");
      var passiveCitation =
          PassiveCitationSliEntity.builder()
              .sourceId(uuid)
              .rank(1)
              .target(DecisionDTO.builder().build())
              .build();

      var sli =
          SliDTO.builder()
              .id(uuid)
              .documentNumber("KSNR150060010")
              .author("Beitel, Willibald")
              .bookTitle("Rechtsprechung, Erlasse und Gesetzesänderungen (12)")
              .yearOfPublication("2005")
              .build();

      when(sliRepository.findById(uuid)).thenReturn(Optional.of(sli));

      var result =
          sliCitationPublishService.updatePassiveCitationSourceWithInformationFromSource(
              passiveCitation);

      assertThat(result).contains(passiveCitation);
      assertThat(result.get().getSourceDocumentNumber()).isEqualTo("KSNR150060010");
      assertThat(result.get().getSourceAuthor()).isEqualTo("Beitel, Willibald");
      assertThat(result.get().getSourceBookTitle())
          .isEqualTo("Rechtsprechung, Erlasse und Gesetzesänderungen (12)");
      assertThat(result.get().getSourceYearOfPublication()).isEqualTo("2005");
      assertThat(result.get().getSourceId()).isEqualTo(uuid);
    }
  }

  @Nested
  class updateActiveCitationTargetWithInformationFromTarget {
    @Test
    void shouldReturnSameReferenceIfNoIdIsGiven() {
      var activeCitation =
          ActiveCitationSliEntity.builder()
              .targetAuthor("Gernhuber")
              .targetBookTitle("Rechtsprechung, Erlasse und Gesetzesänderungen (12)")
              .rank(1)
              .source(DecisionDTO.builder().build())
              .build();

      var result =
          sliCitationPublishService.updateActiveCitationTargetWithInformationFromTarget(
              activeCitation);

      assertThat(result).isEqualTo(activeCitation);
      assertThat(result.getTargetId()).isNull();
      assertThat(result.getTargetDocumentNumber()).isNull();
      assertThat(result.getTargetAuthor()).isEqualTo("Gernhuber");
      assertThat(result.getTargetBookTitle())
          .isEqualTo("Rechtsprechung, Erlasse und Gesetzesänderungen (12)");
      assertThat(result.getTargetYearOfPublication()).isNull();
    }

    @Test
    void shouldReturnSameReferenceWithNullDocumentNumberAndIdIfSliIsNotFound() {
      var uuid = UUID.fromString("9e266182-f4c0-4f7f-9987-1e7b5603aa2b");
      var activeCitation =
          ActiveCitationSliEntity.builder()
              .targetAuthor("Gernhuber")
              .targetBookTitle("Rechtsprechung, Erlasse und Gesetzesänderungen (12)")
              .targetId(uuid)
              .targetDocumentNumber("KSNR150060010")
              .rank(1)
              .source(DecisionDTO.builder().build())
              .build();

      when(sliRepository.findById(uuid)).thenReturn(Optional.empty());

      var result =
          sliCitationPublishService.updateActiveCitationTargetWithInformationFromTarget(
              activeCitation);

      assertThat(result).isEqualTo(activeCitation);
      assertThat(result.getTargetDocumentNumber()).isNull();
      assertThat(result.getTargetId()).isNull();
      assertThat(result.getTargetAuthor()).isEqualTo("Gernhuber");
      assertThat(result.getTargetBookTitle())
          .isEqualTo("Rechtsprechung, Erlasse und Gesetzesänderungen (12)");
      assertThat(result.getTargetYearOfPublication()).isNull();
    }

    @Test
    void shouldReturnUpdatedActiveCitationWhenSliIsFound() {
      var uuid = UUID.fromString("82948f54-94c7-43f2-9562-7b9efc6ce4fb");
      var activeCitation =
          ActiveCitationSliEntity.builder()
              .targetId(uuid)
              .targetDocumentNumber("KSNR150060010")
              .rank(1)
              .source(DecisionDTO.builder().build())
              .build();

      var sli =
          SliDTO.builder()
              .id(uuid)
              .documentNumber("KSNR150060010")
              .author("Beitel, Willibald")
              .bookTitle("Rechtsprechung, Erlasse und Gesetzesänderungen (12)")
              .yearOfPublication("2005")
              .build();

      when(sliRepository.findById(uuid)).thenReturn(Optional.of(sli));

      var result =
          sliCitationPublishService.updateActiveCitationTargetWithInformationFromTarget(
              activeCitation);

      assertThat(result).isEqualTo(activeCitation);
      assertThat(result.getTargetId()).isEqualTo(uuid);
      assertThat(result.getTargetDocumentNumber()).isEqualTo("KSNR150060010");
      assertThat(result.getTargetAuthor()).isEqualTo("Beitel, Willibald");
      assertThat(result.getTargetBookTitle())
          .isEqualTo("Rechtsprechung, Erlasse und Gesetzesänderungen (12)");
      assertThat(result.getTargetYearOfPublication()).isEqualTo("2005");
    }
  }
}
