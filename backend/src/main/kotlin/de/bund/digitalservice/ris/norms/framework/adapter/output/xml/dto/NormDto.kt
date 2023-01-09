package de.bund.digitalservice.ris.norms.framework.adapter.output.xml.dto

class NormDto(
    val guid: String,
    val officialLongTitle: IdentifiedElement,
    var officialShortTitle: IdentifiedElement?,
    var publicationDate: String?,
    var documentTypeName: String?,
    var documentNormCategory: String?,
    var providerDecidingBody: String?,
    var participationInstitution: String?,
    var printAnnouncementGazette: String?,
    var printAnnouncementPage: String?,
    var europeanLegalIdentifier: String?,
    var articles: List<ArticleDto> = listOf()
)
