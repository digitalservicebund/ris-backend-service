package de.bund.digitalservice.ris.exceptions.response
data class ErrorResponse(
    val errors: MutableList<ErrorDetails>? = null,
)
