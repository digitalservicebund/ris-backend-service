package de.bund.digitalservice.ris.caselaw.adapter.eurlex;

public class EurLexSearchException extends RuntimeException {
  public EurLexSearchException(Exception ex) {
    super(ex);
  }

  public EurLexSearchException(String message, Exception ex) {
    super(message, ex);
  }
}
