package de.bund.digitalservice.ris.caselaw.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresXmlMailRepositoryImpl;
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

  @MockBean private PostgresXmlMailRepositoryImpl mailRepository;

  @Captor private ArgumentCaptor<XmlMail> xmlMailCaptor;

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
    XmlMail xmlMail =
        new XmlMail(
            TEST_UUID,
            "receiver",
            "subject",
            "xml",
            "200",
            new ArrayList<>(),
            "file",
            initialPublishDate,
            PublishState.SENT);

    when(mailRepository.getLastPublishedXmlMail(TEST_UUID)).thenReturn(Mono.just(xmlMail));
    when(mailRepository.save(any(XmlMail.class))).thenReturn(Mono.just(XmlMail.EMPTY));

    StepVerifier.create(service.setPublishState(TEST_UUID, expectedPublishState))
        .consumeNextWith(resultString -> assertThat(resultString).isEqualTo(TEST_UUID))
        .verifyComplete();

    verify(mailRepository).getLastPublishedXmlMail(TEST_UUID);
    verify(mailRepository).save(xmlMailCaptor.capture());

    assertThat(xmlMailCaptor.getValue().publishState()).isEqualTo(expectedPublishState);
    assertThat(xmlMailCaptor.getValue().publishDate()).isAfter(initialPublishDate);
  }

  @Test
  void testSetPublishState_withInvalidDocumentUnitUuid() {
    when(mailRepository.getLastPublishedXmlMail(TEST_UUID)).thenReturn(Mono.empty());

    StepVerifier.create(service.setPublishState(TEST_UUID, PublishState.SUCCESS)).verifyComplete();

    verify(mailRepository).getLastPublishedXmlMail(TEST_UUID);
  }
}
