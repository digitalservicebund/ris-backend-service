package de.bund.digitalservice.ris.norms.domain.entity

import java.time.LocalDate
import java.util.UUID

data class Norm(
    val guid: UUID,
    var longTitle: String,
    val articles: List<Article> = listOf()
) {
    val officialShortTitle: String? = null
    val officialAbbreviation: String? = null
    val referenceNumber: String? = null
    val publicationDate: LocalDate? = null
    val announcementDate: LocalDate? = null
    val citationDate: LocalDate? = null
    val frameKeywords: String? = null
    val authorEntity: String? = null
    val authorDecidingBody: String? = null
    val authorIsResolutionMajority: Boolean? = null
    val leadJurisdiction: String? = null
    val leadUnit: String? = null
    val participationType: String? = null
    val participationInstitution: String? = null
    val documentTypeName: String? = null
    val documentNormCategory: String? = null
    val documentTemplateName: String? = null
    val subjectFna: String? = null
    val subjectPreviousFna: String? = null
    val subjectGesta: String? = null
    val subjectBgb3: String? = null
}
