package de.bund.digitalservice.ris.exceptions.response

import java.net.URI

data class ErrorDetails(
    val code: String,
    val instance: URI? = URI.create(""),
    val message: String? = "",
)
