package de.bund.digitalservice.ris.caselaw.integration.tests;

public class RisEntityExchangeResult<T> {
  private T body;

  public RisEntityExchangeResult(T body) {
    this.body = body;
  }

  public T getResponseBody() {
    return body;
  }
}
