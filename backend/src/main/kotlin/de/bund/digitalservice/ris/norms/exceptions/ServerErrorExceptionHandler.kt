package de.bund.digitalservice.ris.shared.exceptions

import de.bund.digitalservice.ris.norms.exceptions.response.ErrorCode
import de.bund.digitalservice.ris.norms.exceptions.response.ErrorDetails
import de.bund.digitalservice.ris.norms.exceptions.response.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler
import reactor.core.publisher.Mono
import java.util.*

@RestControllerAdvice
class ServerErrorExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleAllUncaughtException(exception: Exception): ResponseEntity<Mono<ErrorResponse>> =
        ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                Mono.just(
                    ErrorResponse(
                        mutableListOf(
                            ErrorDetails(ErrorCode.SERVER_ERROR),
                        ),
                    ),
                ),
            )
}
