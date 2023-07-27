package de.bund.digitalservice.ris.norms.exceptions.handler

import de.bund.digitalservice.ris.norms.exceptions.response.ErrorCode
import de.bund.digitalservice.ris.norms.exceptions.response.ErrorDetails
import de.bund.digitalservice.ris.norms.exceptions.response.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler
import reactor.core.publisher.Mono

@RestControllerAdvice
class ForbiddenExceptionHandler : ResponseEntityExceptionHandler() {

  @ExceptionHandler(HttpClientErrorException.Forbidden::class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  fun handleAllUncaughtException(exception: Exception): ResponseEntity<Mono<ErrorResponse>> =
      ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(
              Mono.just(
                  ErrorResponse(
                      mutableListOf(
                          ErrorDetails(ErrorCode.NOT_AUTHENTICATED, ""),
                      ),
                  ),
              ),
          )
}
