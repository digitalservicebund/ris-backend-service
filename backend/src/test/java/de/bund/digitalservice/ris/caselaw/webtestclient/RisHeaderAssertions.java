package de.bund.digitalservice.ris.caselaw.webtestclient;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

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
}
