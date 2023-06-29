package de.bund.digitalservice.ris.norms.application.port.input

import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.util.UUID

fun interface EditNormFrameUseCase {
    fun editNormFrame(command: Command): Mono<Boolean>

    data class Command(val guid: UUID, val properties: NormFrameProperties)

    data class
    NormFrameProperties(
        val officialLongTitle: String,
        val metadataSections: List<MetadataSection>,
        var risAbbreviation: String? = null,
        var documentNumber: String? = null,
        var documentCategory: String? = null,

        var officialShortTitle: String? = null,
        var officialAbbreviation: String? = null,

        var announcementDate: LocalDate? = null,
        var publicationDate: LocalDate? = null,

        var completeCitation: String? = null,

        var celexNumber: String? = null,

        var text: String? = null,
    )
}
