package de.bund.digitalservice.ris.exceptions.handler

import de.bund.digitalservice.ris.exceptions.exception.NotFoundWithInstanceException
import de.bund.digitalservice.ris.exceptions.response.ErrorDetails
import de.bund.digitalservice.ris.exceptions.response.ErrorResponse
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler
import reactor.core.publisher.Mono

@RestControllerAdvice
class NotFoundExceptionHandler : ResponseEntityExceptionHandler() {
  @ExceptionHandler(NotFoundException::class)
  fun handleNoSuchElementFoundException(
      itemNotFoundException: NotFoundException
  ): ResponseEntity<Mono<ErrorResponse>> =
      ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              Mono.just(
                  ErrorResponse(
                      mutableListOf(
                          ErrorDetails("NOT_FOUND"),
                      ),
                  ),
              ),
          )

  @ExceptionHandler(NotFoundWithInstanceException::class)
  fun handleNotFoundWithInstanceException(
      exception: NotFoundWithInstanceException
  ): ResponseEntity<Mono<ErrorResponse>> =
      ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              Mono.just(
                  ErrorResponse(
                      mutableListOf(
                          ErrorDetails("NOT_FOUND", instance = exception.instance),
                      ),
                  ),
              ),
          )
}
