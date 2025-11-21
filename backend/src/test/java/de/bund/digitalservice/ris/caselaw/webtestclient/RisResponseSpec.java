package de.bund.digitalservice.ris.caselaw.webtestclient;

import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

public class RisResponseSpec {
  private final ResultActions resultActions;
  private final ObjectMapper objectMapper;

  public RisResponseSpec() {
    resultActions = null;
    objectMapper = null;
  }

  public RisResponseSpec(ResultActions resultActions, ObjectMapper objectMapper) {
    this.resultActions = resultActions;
    this.objectMapper = objectMapper;
  }

  public RisStatusAssertions expectStatus() {
    return new RisStatusAssertions(resultActions, this);
  }

  public <T> RisBodySpec<T> expectBody(Class<T> clazz) {
    return new RisBodySpec<>(resultActions, objectMapper, clazz);
  }

  public <T> RisBodySpec<T> expectBody(TypeReference<T> typeReference) {
    return new RisBodySpec<>(resultActions, objectMapper, typeReference);
  }

  public RisHeaderAssertions expectHeader() {
    return new RisHeaderAssertions(resultActions, this);
  }

  public RisHeaderAssertions and() {
    return new RisHeaderAssertions(resultActions, this);
  }
}
