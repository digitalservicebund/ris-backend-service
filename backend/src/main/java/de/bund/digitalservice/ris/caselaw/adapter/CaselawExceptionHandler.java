package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.exception.BucketException;
import de.bund.digitalservice.ris.caselaw.adapter.exception.ChangelogException;
import de.bund.digitalservice.ris.caselaw.adapter.exception.LdmlTransformationException;
import de.bund.digitalservice.ris.caselaw.adapter.exception.PublishException;
import de.bund.digitalservice.ris.caselaw.domain.HandoverException;
import de.bund.digitalservice.ris.caselaw.domain.exception.ImportApiKeyException;
import de.bund.digitalservice.ris.caselaw.domain.exception.TextCheckNotAllowedCategoryTypeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Slf4j
public class CaselawExceptionHandler extends ResponseEntityExceptionHandler {
  @ExceptionHandler({ImportApiKeyException.class})
  public ResponseEntity<Object> handleImportApiKeyException(ImportApiKeyException ex) {

    ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage());

    return new ResponseEntity<>(apiError, new HttpHeaders(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler({LdmlTransformationException.class})
  public ResponseEntity<Object> handleLdmlTransformationException(LdmlTransformationException ex) {

    ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage());

    return new ResponseEntity<>(apiError, new HttpHeaders(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler({PublishException.class})
  public ResponseEntity<Object> handlePublishException(PublishException ex) {

    ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());

    return new ResponseEntity<>(apiError, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler({ChangelogException.class})
  public ResponseEntity<Object> handleChangelogException(ChangelogException ex) {

    ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());

    return new ResponseEntity<>(apiError, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler({BucketException.class})
  public ResponseEntity<Object> handleBucketException(BucketException ex) {

    ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());

    return new ResponseEntity<>(apiError, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler({TextCheckNotAllowedCategoryTypeException.class})
  public ResponseEntity<Object> handleTextCheckNotAllowedCategoryTypeException(
      TextCheckNotAllowedCategoryTypeException ex) {
    log.error("", ex);

    ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage());

    return new ResponseEntity<>(apiError, new HttpHeaders(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler({HandoverException.class})
  public ResponseEntity<Object> handleHandoverException(HandoverException ex) {

    ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());

    return new ResponseEntity<>(apiError, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  public record ApiError(HttpStatus status, String message) {}
}
