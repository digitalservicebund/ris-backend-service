package de.bund.digitalservice.ris.norms.application.port.output

import de.bund.digitalservice.ris.norms.domain.entity.FileReference
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import reactor.core.publisher.Mono

interface SaveFileReferenceOutputPort {
    fun saveFileReference(command: Command): Mono<Boolean>

    data class Command(val fileReference: FileReference, val norm: Norm)
}
