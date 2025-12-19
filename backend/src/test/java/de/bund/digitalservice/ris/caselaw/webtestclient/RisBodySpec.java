package de.bund.digitalservice.ris.caselaw.webtestclient;

import de.bund.digitalservice.ris.caselaw.integration.tests.RisEntityExchangeResult;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Slf4j
public class RisBodySpec<T> {
  private final ResultActions resultActions;
  private final ObjectMapper objectMapper;
  private Class<T> clazz;
  private TypeReference<T> typeReference;

  public RisBodySpec(ResultActions resultActions, ObjectMapper objectMapper, Class<T> clazz) {
    this.resultActions = resultActions;
    this.objectMapper = objectMapper;
    this.clazz = clazz;
  }

  public RisBodySpec(
      ResultActions resultActions, ObjectMapper objectMapper, TypeReference<T> typeReference) {
    this.resultActions = resultActions;
    this.objectMapper = objectMapper;
    this.typeReference = typeReference;
  }

  public RisEntityExchangeResult<T> returnResult() {
    RisEntityExchangeResult<T> exchangeResult = getExchangeResult();
    if (exchangeResult != null) {
      return exchangeResult;
    }

    throw new NotImplementedException();
  }

  private RisEntityExchangeResult<T> getExchangeResult() {
    try {
      var rawResponse = resultActions.andReturn().getResponse();
      String response = rawResponse.getContentAsString(StandardCharsets.UTF_8);
      if (clazz != null) {
        if (clazz == String.class) {
          return (RisEntityExchangeResult<T>) new RisEntityExchangeResult<>(response);
        } else if (clazz == byte[].class) {
          return (RisEntityExchangeResult<T>)
              new RisEntityExchangeResult<>(rawResponse.getContentAsByteArray());
        } else {
          return new RisEntityExchangeResult<>(objectMapper.readValue(response, clazz));
        }
      } else if (typeReference != null) {
        return new RisEntityExchangeResult<>(objectMapper.readValue(response, typeReference));
      }
    } catch (Exception ex) {
      log.error("return result", ex);
    }

    return null;
  }

  public void consumeWith(Consumer<RisEntityExchangeResult<T>> consumer) {
    consumer.accept(getExchangeResult());
  }
}
