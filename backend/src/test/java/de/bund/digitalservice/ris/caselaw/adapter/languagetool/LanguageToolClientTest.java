package de.bund.digitalservice.ris.caselaw.adapter.languagetool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.google.gson.JsonObject;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class LanguageToolClientTest {

  @Mock private RestTemplate restTemplate;

  @Mock private LanguageToolConfig languageToolConfig;

  @InjectMocks private LanguageToolClient languageToolClient;

  private JsonObject sampleAnnotations;
  private LanguageToolResponse sampleSuccessResponse;

  @BeforeEach
  void setUp() {
    // A sample JsonObject to use as input for the test
    sampleAnnotations = new JsonObject();
    sampleAnnotations.addProperty("text", "This is a sample text.");

    // A mock LanguageToolResponse object for a successful API call
    sampleSuccessResponse =
        new LanguageToolResponse(
            List.of(
                Match.builder().length(4).offset(0).message("Typo").textContent("typo").build()));

    // Common mock setup for the LanguageToolConfig
    when(languageToolConfig.getUrl()).thenReturn("http://localhost:8081/v2/check");
    when(languageToolConfig.getLanguage()).thenReturn("de-DE");
    when(languageToolConfig.getDisabledRules()).thenReturn(List.of("RULE1", "RULE2"));
    when(languageToolConfig.getDisabledCategories()).thenReturn(List.of("CAT1", "CAT2"));
  }

  @Test
  void testCheckText_shouldSucceedOnValidResponse() {
    // Arrange: Mock the RestTemplate to return a successful response
    when(restTemplate.postForEntity(
            eq(languageToolConfig.getUrl()), any(HttpEntity.class), eq(LanguageToolResponse.class)))
        .thenReturn(ResponseEntity.ok(sampleSuccessResponse));

    // Act
    LanguageToolResponse response = languageToolClient.checkText(sampleAnnotations);

    // Assert: Verify the response is correct and contains the expected matches
    assertThat(response).isNotNull();
    assertThat(response.getMatches()).hasSize(1);
    assertThat(response.getMatches().get(0).getMessage()).isEqualTo("Typo");
  }

  @Test
  void testCheckText_shouldThrowExceptionWhenRestTemplateThrowsError() {
    // Arrange: Mock the RestTemplate to throw a RestClientException
    when(restTemplate.postForEntity(
            eq(languageToolConfig.getUrl()), any(HttpEntity.class), eq(LanguageToolResponse.class)))
        .thenThrow(new RestClientException("Connection refused"));

    // Act & Assert: Verify that the method re-throws the exception
    assertThatExceptionOfType(RestClientException.class)
        .isThrownBy(() -> languageToolClient.checkText(sampleAnnotations))
        .withMessage("Connection refused");
  }
}
