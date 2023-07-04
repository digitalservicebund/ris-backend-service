package de.bund.digitalservice.ris.caselaw.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresXmlPublicationRepositoryImpl;
import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@Import(SendInBlueMailTrackingService.class)
class SendInBlueMailTrackingServiceTest {

  @SpyBean private SendInBlueMailTrackingService service;

  @MockBean private PostgresXmlPublicationRepositoryImpl mailRepository;

  @Captor private ArgumentCaptor<XmlPublication> xmlPublicationCaptor;

  private static final UUID TEST_UUID = UUID.fromString("88888888-4444-4444-4444-121212121212");

  @Test
  void testGetMappedPublishState_withSuccessfulState() {
    PublishState expectedPublishState = PublishState.SUCCESS;
    PublishState mappedPublishState = service.getMappedPublishState("delivered");

    assertThat(mappedPublishState).isEqualTo(expectedPublishState);
  }

  @Test
  void testGetMappedPublishState_withUnsuccessfulState() {
    PublishState expectedPublishState = PublishState.ERROR;
    PublishState mappedPublishState = service.getMappedPublishState("bounced");

    assertThat(mappedPublishState).isEqualTo(expectedPublishState);
  }

  @Test
  void testSetPublishState_withValidDocumentUnitUuid() {
    PublishState expectedPublishState = PublishState.SUCCESS;
    Instant initialPublishDate = Instant.now();
    XmlPublication xmlPublication =
        XmlPublication.builder()
            .documentUnitUuid(TEST_UUID)
            .publishDate(initialPublishDate)
            .receiverAddress("receiver")
            .mailSubject("subject")
            .xml("xml")
            .statusCode("200")
            .statusMessages(new ArrayList<>())
            .fileName("file")
            .publishState(PublishState.SENT)
            .build();

    when(mailRepository.getLastXmlPublication(TEST_UUID)).thenReturn(Mono.just(xmlPublication));
    when(mailRepository.save(any(XmlPublication.class)))
        .thenReturn(Mono.just(XmlPublication.builder().build()));

    StepVerifier.create(service.setPublishState(TEST_UUID, expectedPublishState))
        .consumeNextWith(resultString -> assertThat(resultString).isEqualTo(TEST_UUID))
        .verifyComplete();

    verify(mailRepository).getLastXmlPublication(TEST_UUID);
    verify(mailRepository).save(xmlPublicationCaptor.capture());

    assertThat(xmlPublicationCaptor.getValue().publishState()).isEqualTo(expectedPublishState);
    assertThat(xmlPublicationCaptor.getValue().getPublishDate()).isAfter(initialPublishDate);
  }

  @Test
  void testSetPublishState_withInvalidDocumentUnitUuid() {
    when(mailRepository.getLastXmlPublication(TEST_UUID)).thenReturn(Mono.empty());

    StepVerifier.create(service.setPublishState(TEST_UUID, PublishState.SUCCESS)).verifyComplete();

    verify(mailRepository).getLastXmlPublication(TEST_UUID);
  }
}
