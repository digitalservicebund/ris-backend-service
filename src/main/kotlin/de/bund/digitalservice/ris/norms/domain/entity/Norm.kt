package de.bund.digitalservice.ris.norms.domain.entity

import java.time.LocalDate
import java.util.UUID

data class Norm(
    val guid: UUID,
    val longTitle: String,
    val articles: List<Article> = listOf(),

    var officialShortTitle: String? = null,
    var officialAbbreviation: String? = null,
    var referenceNumber: String? = null,
    var publicationDate: LocalDate? = null,
    var announcementDate: LocalDate? = null,
    var citationDate: LocalDate? = null,
    var frameKeywords: String? = null,
    var authorEntity: String? = null,
    var authorDecidingBody: String? = null,
    var authorIsResolutionMajority: Boolean? = null,
    var leadJurisdiction: String? = null,
    var leadUnit: String? = null,
    var participationType: String? = null,
    var participationInstitution: String? = null,
    var documentTypeName: String? = null,
    var documentNormCategory: String? = null,
    var documentTemplateName: String? = null,
    var subjectFna: String? = null,
    var subjectPreviousFna: String? = null,
    var subjectGesta: String? = null,
    var subjectBgb3: String? = null,
    var unofficialTitle: String? = null,
    var unofficialShortTitle: String? = null,
    var unofficialAbbreviation: String? = null,
    var risAbbreviation: String? = null
)
