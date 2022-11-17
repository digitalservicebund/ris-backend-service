package de.bund.digitalservice.ris.norms.domain.entity

import java.time.LocalDate
import java.util.UUID

data class Norm(
    val guid: UUID,
    val longTitle: String,
    val articles: List<Article> = listOf(),
    var officialShortTitle: String? = "",
    var officialAbbreviation: String? = "",
    var referenceNumber: String? = "",
    var publicationDate: LocalDate? = LocalDate.EPOCH,
    var announcementDate: LocalDate? = LocalDate.EPOCH,
    var citationDate: LocalDate? = LocalDate.EPOCH,
    var frameKeywords: String? = "",
    var authorEntity: String? = "",
    var authorDecidingBody: String? = "",
    var authorIsResolutionMajority: Boolean? = false,
    var leadJurisdiction: String? = "",
    var leadUnit: String? = "",
    var participationType: String? = "",
    var participationInstitution: String? = "",
    var documentTypeName: String? = "",
    var documentNormCategory: String? = "",
    var documentTemplateName: String? = "",
    var subjectFna: String? = "",
    var subjectPreviousFna: String? = "",
    var subjectGesta: String? = "",
    var subjectBgb3: String? = ""
)
