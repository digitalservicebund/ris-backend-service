package de.bund.digitalservice.ris.exceptions.exception

import de.bund.digitalservice.ris.exceptions.response.ErrorDetails

class ValidationException(
    val errors: MutableList<ErrorDetails> = mutableListOf(),
) : Exception("")
