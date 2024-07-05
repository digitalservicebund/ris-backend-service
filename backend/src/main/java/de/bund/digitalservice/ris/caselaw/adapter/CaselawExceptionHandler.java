package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.exception.ImportApiKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class CaselawExceptionHandler extends ResponseEntityExceptionHandler {
  @ExceptionHandler({ImportApiKeyException.class})
  public ResponseEntity<Object> handleImportApiKeyException(ImportApiKeyException ex) {

    ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage());

    return new ResponseEntity<>(apiError, new HttpHeaders(), HttpStatus.BAD_REQUEST);
  }

  private record ApiError(HttpStatus status, String message) {}
}
