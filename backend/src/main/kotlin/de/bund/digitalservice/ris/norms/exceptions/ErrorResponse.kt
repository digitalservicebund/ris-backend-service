package de.bund.digitalservice.ris.norms.exceptions
data class ErrorResponse(
    val errors: MutableList<ErrorDetails>? = null,
)
