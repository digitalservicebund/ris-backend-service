package de.bund.digitalservice.ris.norms.exceptions.response
data class ErrorResponse(
    val errors: MutableList<ErrorDetails>? = null,
)
