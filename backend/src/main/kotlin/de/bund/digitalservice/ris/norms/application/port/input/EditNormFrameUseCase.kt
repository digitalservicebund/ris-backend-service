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
        val metadataSections: List<MetadataSection>,

        var announcementDate: LocalDate? = null,

        var statusNote: String? = null,
        var statusDescription: String? = null,
        var statusDate: LocalDate? = null,
        var statusReference: String? = null,
        var repealNote: String? = null,
        var repealArticle: String? = null,
        var repealDate: LocalDate? = null,
        var repealReferences: String? = null,
        var reissueNote: String? = null,
        var reissueArticle: String? = null,
        var reissueDate: LocalDate? = null,
        var reissueReference: String? = null,
        var otherStatusNote: String? = null,
    )
}
