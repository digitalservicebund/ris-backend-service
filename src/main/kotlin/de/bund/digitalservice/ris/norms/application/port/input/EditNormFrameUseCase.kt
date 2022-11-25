package de.bund.digitalservice.ris.norms.application.port.input

import reactor.core.publisher.Mono
import java.util.UUID

interface EditNormFrameUseCase {
    fun editNormFrame(command: Command): Mono<Boolean>

    data class Command(
        val guid: UUID,
        val longTitle: String,
        val officialShortTitle: String? = null,
        val officialAbbreviation: String? = null,
        val referenceNumber: String? = null,
        val publicationDate: String? = null,
        val announcementDate: String? = null,
        val citationDate: String? = null,
        val frameKeywords: String? = null,
        val authorEntity: String? = null,
        val authorDecidingBody: String? = null,
        val authorIsResolutionMajority: Boolean? = null,
        val leadJurisdiction: String? = null,
        val leadUnit: String? = null,
        val participationType: String? = null,
        val participationInstitution: String? = null,
        val documentTypeName: String? = null,
        val documentNormCategory: String? = null,
        val documentTemplateName: String? = null,
        val subjectFna: String? = null,
        val subjectPreviousFna: String? = null,
        val subjectGesta: String? = null,
        val subjectBgb3: String? = null
    )
}
