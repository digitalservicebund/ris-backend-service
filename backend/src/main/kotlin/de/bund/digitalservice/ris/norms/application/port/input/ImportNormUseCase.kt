package de.bund.digitalservice.ris.norms.application.port.input

import de.bund.digitalservice.ris.norms.domain.value.UndefinedDate
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.util.UUID

interface ImportNormUseCase {
    fun importNorm(command: Command): Mono<UUID>

    data class Command(val data: NormData)

    data class NormData(
        val officialLongTitle: String,
        val articles: List<ArticleData>,
        val officialShortTitle: String? = null,
        val officialAbbreviation: String? = null,
        val referenceNumber: String? = null,
        val announcementDate: LocalDate? = null,
        val citationDate: LocalDate? = null,
        val frameKeywords: String? = null,
        val providerEntity: String? = null,
        val providerDecidingBody: String? = null,
        val providerIsResolutionMajority: Boolean? = null,
        val leadJurisdiction: String? = null,
        val leadUnit: String? = null,
        val participationType: String? = null,
        val participationInstitution: String? = null,
        val subjectFna: String? = null,
        val subjectGesta: String? = null,
        val documentNumber: String? = null,
        val documentCategory: String? = null,
        val risAbbreviationInternationalLaw: String? = null,
        val unofficialReference: String? = null,
        val applicationScopeArea: String? = null,
        val applicationScopeStartDate: LocalDate? = null,
        val applicationScopeEndDate: LocalDate? = null,
        val validityRule: String? = null,
        val celexNumber: String? = null,
        val definition: String? = null,
        val categorizedReference: String? = null,
        val otherFootnote: String? = null,
        val expirationDate: LocalDate? = null,
        val entryIntoForceDate: LocalDate? = null,
        val unofficialLongTitle: String? = null,
        val unofficialShortTitle: String? = null,
        val unofficialAbbreviation: String? = null,
        val risAbbreviation: String? = null,
        val printAnnouncementGazette: String? = null,
        val printAnnouncementYear: String? = null,
        val printAnnouncementPage: String? = null,
        val documentStatusWorkNote: String? = null,
        val documentStatusDescription: String? = null,
        val documentStatusDate: LocalDate? = null,
        val statusNote: String? = null,
        val statusDescription: String? = null,
        val statusDate: LocalDate? = null,
        val statusReference: String? = null,
        val repealNote: String? = null,
        val repealArticle: String? = null,
        val repealDate: LocalDate? = null,
        val repealReferences: String? = null,
        val reissueNote: String? = null,
        val reissueArticle: String? = null,
        val reissueDate: LocalDate? = null,
        val reissueReference: String? = null,
        val otherStatusNote: String? = null,
        val text: String? = null,
        val ageOfMajorityIndication: String? = null,
        val divergentExpirationDate: LocalDate? = null,
        val divergentExpirationDateState: UndefinedDate? = null,
        val principleExpirationDate: LocalDate? = null,
        val principleExpirationDateState: UndefinedDate? = null,
        val expirationNormCategory: String? = null,
        val divergentEntryIntoForceDate: LocalDate? = null,
        val divergentEntryIntoForceDateState: UndefinedDate? = null,
        val principleEntryIntoForceDate: LocalDate? = null,
        val principleEntryIntoForceDateState: UndefinedDate? = null,
        val entryIntoForceDateState: UndefinedDate? = null,
        val expirationDateState: UndefinedDate? = null
    ) {
        init {
            require(officialLongTitle.isNotBlank())
        }
    }

    data class ArticleData(
        val title: String? = null,
        val marker: String,
        val paragraphs: List<ParagraphData>
    ) {
        init {
            require(marker.isNotBlank())
        }
    }

    data class ParagraphData(val marker: String? = null, val text: String) {
        init {
            require(text.isNotBlank())
        }
    }
}
