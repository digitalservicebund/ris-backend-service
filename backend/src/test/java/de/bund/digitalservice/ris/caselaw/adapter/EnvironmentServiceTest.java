package de.bund.digitalservice.ris.caselaw.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.domain.CurrentEnvironment;
import de.bund.digitalservice.ris.caselaw.domain.EnvironmentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EnvironmentServiceTest {

  private CurrentEnvironment currentEnvironment;

  private EnvironmentService environmentService;

  @BeforeEach
  void setUp() {
    currentEnvironment = mock(CurrentEnvironment.class);
    environmentService = new EnvironmentService(currentEnvironment);
  }

  @Test
  void getEnvironment_returnsExpectedResponse() {
    // Arrange
    when(currentEnvironment.name()).thenReturn("staging");
    when(currentEnvironment.portalUrl()).thenReturn("https://portal.local");

    // Act
    EnvironmentResponse response = environmentService.getEnvironment();

    // Assert
    assertThat(response.environment()).isEqualTo("staging");
    assertThat(response.portalUrl()).isEqualTo("https://portal.local");
  }

  @Test
  void getAccountManagementUrl_returnsCorrectUrl() {
    // Arrange
    when(currentEnvironment.accountManagementUrl()).thenReturn("https://accounts.local");

    // Act
    String url = environmentService.getAccountManagementUrl();

    // Assert
    assertThat(url).isEqualTo("https://accounts.local");
  }
}
