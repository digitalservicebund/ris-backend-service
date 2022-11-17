package de.bund.digitalservice.ris.norms.domain.entity

import java.time.LocalDate
import java.util.UUID

data class Norm(
    val guid: UUID,
    var longTitle: String,
    val articles: List<Article> = listOf(),
    val officialShortTitle: String?,
    val officialAbbreviation: String?,
    val referenceNumber: String?,
    val publicationDate: LocalDate?,
    val announcementDate: LocalDate?,
    val citationDate: LocalDate?,
    val frameKeywords: String?,
    val authorEntity: String?,
    val authorDecidingBody: String?,
    val authorIsResolutionMajority: Boolean?,
    val leadJurisdiction: String?,
    val leadUnit: String?,
    val participationType: String?,
    val participationInstitution: String?,
    val documentTypeName: String?,
    val documentNormCategory: String?,
    val documentTemplateName: String?,
    val subjectFna: String?,
    val subjectPreviousFna: String?,
    val subjectGesta: String?,
    val subjectBgb3: String?
)
