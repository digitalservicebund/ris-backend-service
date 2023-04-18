package de.bund.digitalservice.ris.shared.exceptions

import de.bund.digitalservice.ris.norms.exceptions.ErrorCode
import de.bund.digitalservice.ris.norms.exceptions.ErrorDetails
import de.bund.digitalservice.ris.norms.exceptions.ErrorResponse
import lombok.extern.slf4j.Slf4j
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler
import java.util.*

@Slf4j(topic = "GLOBAL_EXCEPTION_HANDLER")
@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    protected fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders?,
        status: HttpStatus?,
        request: WebRequest?,
    ): ResponseEntity<Any> {
        val errors = mutableListOf<ErrorDetails>()
        for (fieldError in ex.bindingResult.fieldErrors) {
            errors.add(ErrorDetails(ErrorCode.VALIDATION_ERROR, fieldError.field))
        }
        val errorResponse = ErrorResponse(errors)
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse)
    }

    @ExceptionHandler(NotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNoSuchElementFoundException(
        itemNotFoundException: NotFoundException,
        request: WebRequest,
    ): ResponseEntity<Any> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse(mutableListOf(ErrorDetails(ErrorCode.NOT_FOUND))))
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleAllUncaughtException(exception: Exception, request: WebRequest): ResponseEntity<Any> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse(mutableListOf(ErrorDetails(ErrorCode.SERVER_ERROR))))
    }
}
