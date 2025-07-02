package de.bund.digitalservice.ris.caselaw.webtestclient;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

import java.time.Duration;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

public class RisHeaderAssertions {

  private final ResultActions resultActions;
  private final RisResponseSpec responseSpec;

  public RisHeaderAssertions(ResultActions resultActions, RisResponseSpec responseSpec) {
    this.resultActions = resultActions;
    this.responseSpec = responseSpec;
  }

  public RisResponseSpec contentType(MediaType mediaType) {
    try {
      resultActions.andExpect(header().string("Content-Type", mediaType.toString()));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return responseSpec;
  }

  public RisResponseSpec cacheControl(Duration duration) {
    String expectedValuePart = "max-age=" + duration.getSeconds();
    try {
      resultActions.andExpect(header().string("Cache-Control", expectedValuePart));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return responseSpec;
  }

  public RisResponseSpec valueEquals(String name, String value) {
    try {
      resultActions.andExpect(header().string(name, value));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return responseSpec;
  }
}
