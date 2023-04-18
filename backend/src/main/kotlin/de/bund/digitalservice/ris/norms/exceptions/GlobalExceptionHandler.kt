package de.bund.digitalservice.ris.shared.exceptions

import de.bund.digitalservice.ris.norms.exceptions.ErrorCode
import de.bund.digitalservice.ris.norms.exceptions.ErrorDetails
import de.bund.digitalservice.ris.norms.exceptions.ErrorResponse
import lombok.extern.slf4j.Slf4j
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler
import reactor.core.publisher.Mono
import java.util.*

@Slf4j(topic = "GLOBAL_EXCEPTION_HANDLER")
@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    val serverError = ErrorResponse(mutableListOf(ErrorDetails(ErrorCode.SERVER_ERROR)))
    val notFoundError = ErrorResponse(mutableListOf(ErrorDetails(ErrorCode.NOT_FOUND)))

    @ExceptionHandler(NotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNoSuchElementFoundException(
        itemNotFoundException: NotFoundException,
    ): ResponseEntity<Mono<ErrorResponse>> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Mono.just(notFoundError))
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleAllUncaughtException(exception: Exception): ResponseEntity<Mono<ErrorResponse>> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Mono.just(serverError))
    }
}
