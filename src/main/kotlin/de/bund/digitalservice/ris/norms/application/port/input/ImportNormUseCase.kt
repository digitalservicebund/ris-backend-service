package de.bund.digitalservice.ris.norms.application.port.input

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
        val risAbbreviation: String? = null
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
