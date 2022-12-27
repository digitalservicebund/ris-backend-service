package de.bund.digitalservice.ris.norms.framework.adapter.output.xml.dto

import java.time.LocalDate
import java.util.*

class NormDto(
    val guid: UUID,
    val officialLongTitle: String,
    var officialShortTitle: String?,
    var publicationDate: String?,
    var documentTypeName: String?,
    var documentNormCategory: String?,
    var providerDecidingBody: String?,
    var participationInstitution: String?,
    var printAnnouncementGazette: String?,
    var printAnnouncementPage: String?,
    var articles: List<ArticleDto> = listOf()
) {
    var publicationYear: String? = publicationDate?.let { LocalDate.parse(it).year.toString() }
}
