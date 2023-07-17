package de.bund.digitalservice.ris.exceptions.handler

import de.bund.digitalservice.ris.exceptions.response.ErrorDetails
import de.bund.digitalservice.ris.exceptions.response.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler
import reactor.core.publisher.Mono

@RestControllerAdvice
class ForbiddenExceptionHandler : ResponseEntityExceptionHandler() {
  @ExceptionHandler(HttpClientErrorException.Forbidden::class)
  fun handleAllUncaughtException(exception: Exception): ResponseEntity<Mono<ErrorResponse>> =
      ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(
              Mono.just(
                  ErrorResponse(
                      mutableListOf(
                          ErrorDetails("NOT_AUTHENTICATED"),
                      ),
                  ),
              ),
          )
}
