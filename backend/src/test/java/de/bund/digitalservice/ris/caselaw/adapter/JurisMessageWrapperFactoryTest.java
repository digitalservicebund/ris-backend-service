package de.bund.digitalservice.ris.caselaw.adapter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mockStatic;

import de.bund.digitalservice.ris.domain.export.juris.response.ImportMessageWrapper;
import de.bund.digitalservice.ris.domain.export.juris.response.ProcessMessageWrapper;
import jakarta.mail.Message;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;

public class JurisMessageWrapperFactoryTest {
  private JurisMessageWrapperFactory factory;

  @Mock private Message message;

  @BeforeEach
  void setup() {
    factory =
        new JurisMessageWrapperFactory(
            List.of(ImportMessageWrapper.class, ProcessMessageWrapper.class));
  }

  @Test
  void testGetResponsibleWrapper_withMatchingWrapper() {
    try (MockedStatic<ImportMessageWrapper> mockedImportMessageWrapper =
            mockStatic(ImportMessageWrapper.class);
        MockedStatic<ProcessMessageWrapper> mockedProcessMessageWrapper =
            mockStatic(ProcessMessageWrapper.class)) {

      mockedImportMessageWrapper
          .when(() -> ImportMessageWrapper.canHandle(message))
          .thenReturn(false);
      mockedProcessMessageWrapper
          .when(() -> ProcessMessageWrapper.canHandle(message))
          .thenReturn(true);

      assertEquals(
          ProcessMessageWrapper.class, factory.getResponsibleWrapper(message).get().getClass());
    }
  }

  @Test
  void testGetResponsibleWrapper_withoutMatchingWrapper() {
    try (MockedStatic<ImportMessageWrapper> mockedImportMessageWrapper =
            mockStatic(ImportMessageWrapper.class);
        MockedStatic<ProcessMessageWrapper> mockedProcessMessageWrapper =
            mockStatic(ProcessMessageWrapper.class)) {

      mockedImportMessageWrapper
          .when(() -> ImportMessageWrapper.canHandle(message))
          .thenReturn(false);
      mockedProcessMessageWrapper
          .when(() -> ProcessMessageWrapper.canHandle(message))
          .thenReturn(false);

      assertEquals(Optional.empty(), factory.getResponsibleWrapper(message));
    }
  }
}
