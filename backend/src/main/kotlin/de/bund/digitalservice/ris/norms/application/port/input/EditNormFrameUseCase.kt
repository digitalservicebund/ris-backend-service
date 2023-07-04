package de.bund.digitalservice.ris.norms.application.port.input

import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import reactor.core.publisher.Mono
import java.util.UUID

fun interface EditNormFrameUseCase {
    fun editNormFrame(command: Command): Mono<Boolean>

    data class Command(val guid: UUID, val properties: NormFrameProperties)

    data class
    NormFrameProperties(
        val metadataSections: List<MetadataSection>,
    )
}
