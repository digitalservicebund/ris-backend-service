package de.bund.digitalservice.ris.norms.framework.adapter.output.xml.dto

import java.time.LocalDate
import java.util.*

data class NormDto(
    val guid: UUID,
    val officialLongTitle: String,
    var officialShortTitle: String? = null,
    var providerEntity: String? = null,
    var entryIntoForceDate: LocalDate? = null,
    var articles: List<ArticleDto> = listOf()
)
