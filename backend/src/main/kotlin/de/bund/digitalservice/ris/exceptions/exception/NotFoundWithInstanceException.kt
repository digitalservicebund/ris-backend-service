package de.bund.digitalservice.ris.exceptions.exception

import java.net.URI

class NotFoundWithInstanceException(
    val instance: URI,
) : Exception("")
