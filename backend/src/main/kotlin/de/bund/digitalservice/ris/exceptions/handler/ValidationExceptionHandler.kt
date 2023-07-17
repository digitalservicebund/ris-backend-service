package de.bund.digitalservice.ris.exceptions.handler

import de.bund.digitalservice.ris.exceptions.exception.ValidationException
import de.bund.digitalservice.ris.exceptions.response.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler
import reactor.core.publisher.Mono

@RestControllerAdvice
class ValidationExceptionHandler : ResponseEntityExceptionHandler() {

  @ExceptionHandler(ValidationException::class)
  fun handleValidationException(
      exception: ValidationException
  ): ResponseEntity<Mono<ErrorResponse>> {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
        .body(Mono.just(ErrorResponse(exception.errors.toMutableList())))
  }
}
