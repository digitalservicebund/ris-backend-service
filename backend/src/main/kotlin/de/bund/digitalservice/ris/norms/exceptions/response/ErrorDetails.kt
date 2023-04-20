package de.bund.digitalservice.ris.norms.exceptions.response

data class ErrorDetails(
    val code: ErrorCode,
    val attribute: String? = null,
)
