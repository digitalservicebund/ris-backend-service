package de.bund.digitalservice.ris.norms.domain.entity

import de.bund.digitalservice.ris.norms.domain.value.Guid

data class Norm(val guid: Guid, val longTitle: String, val articles: List<Article> = listOf())
