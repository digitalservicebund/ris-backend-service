package de.bund.digitalservice.ris.norms.application.port.input

import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.value.UndefinedDate
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.util.UUID

interface EditNormFrameUseCase {
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

        var entryIntoForceDate: LocalDate? = null,
        var entryIntoForceDateState: UndefinedDate? = null,
        var principleEntryIntoForceDate: LocalDate? = null,
        var principleEntryIntoForceDateState: UndefinedDate? = null,

        var expirationDate: LocalDate? = null,
        var expirationDateState: UndefinedDate? = null,
        var isExpirationDateTemp: Boolean? = null,
        var principleExpirationDate: LocalDate? = null,
        var principleExpirationDateState: UndefinedDate? = null,

        var announcementDate: LocalDate? = null,
        var publicationDate: LocalDate? = null,

        var completeCitation: String? = null,

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

        var documentStatusWorkNote: String? = null,
        var documentStatusDescription: String? = null,
        var documentStatusDate: LocalDate? = null,
        var documentStatusReference: String? = null,
        var documentStatusEntryIntoForceDate: LocalDate? = null,
        var documentStatusProof: String? = null,
        var documentTextProof: String? = null,
        var otherDocumentNote: String? = null,

        var applicationScopeArea: String? = null,
        var applicationScopeStartDate: LocalDate? = null,
        var applicationScopeEndDate: LocalDate? = null,

        var categorizedReference: String? = null,

        var otherFootnote: String? = null,
        var footnoteChange: String? = null,
        var footnoteComment: String? = null,
        var footnoteDecision: String? = null,
        var footnoteStateLaw: String? = null,
        var footnoteEuLaw: String? = null,

        var digitalEvidenceLink: String? = null,
        var digitalEvidenceRelatedData: String? = null,
        var digitalEvidenceExternalDataNote: String? = null,
        var digitalEvidenceAppendix: String? = null,

        var celexNumber: String? = null,

        var text: String? = null,
    )
}
