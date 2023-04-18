package de.bund.digitalservice.ris.norms.exceptions

data class ErrorDetails(
    val code: ErrorCode,
    val attribute: String? = null,
)
