package de.bund.digitalservice.ris.caselaw.webtestclient;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.web.servlet.ResultActions;

public class RisStatusAssertions {

  private final ResultActions resultActions;
  private final RisResponseSpec responseSpec;

  public RisStatusAssertions(ResultActions resultActions, RisResponseSpec responseSpec) {
    this.resultActions = resultActions;
    this.responseSpec = responseSpec;
  }

  public RisResponseSpec isOk() {
    try {
      resultActions.andExpect(status().isOk());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return responseSpec;
  }

  public RisResponseSpec is4xxClientError() {
    try {
      resultActions.andExpect(status().is4xxClientError());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return responseSpec;
  }

  public RisResponseSpec isNotFound() {
    try {
      resultActions.andExpect(status().isNotFound());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return responseSpec;
  }

  public RisResponseSpec isNoContent() {
    try {
      resultActions.andExpect(status().isNoContent());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return responseSpec;
  }

  public RisResponseSpec is5xxServerError() {
    try {
      resultActions.andExpect(status().is5xxServerError());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return responseSpec;
  }

  public RisResponseSpec isBadRequest() {
    try {
      resultActions.andExpect(status().isBadRequest());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return responseSpec;
  }

  public RisResponseSpec isCreated() {
    try {
      resultActions.andExpect(status().isCreated());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return responseSpec;
  }

  public RisResponseSpec isForbidden() {
    try {
      resultActions.andExpect(status().isForbidden());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return responseSpec;
  }

  public RisResponseSpec isUnauthorized() {
    try {
      resultActions.andExpect(status().isUnauthorized());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return responseSpec;
  }

  public RisResponseSpec is2xxSuccessful() {
    try {
      resultActions.andExpect(status().is2xxSuccessful());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return responseSpec;
  }
}
