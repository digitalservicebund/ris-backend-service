package de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table(name = "norms")
data class NormDto(
    @Id
    val id: Int,
    val guid: UUID,
    @Column("long_title")
    val longTitle: String
) {
    @Transient
    var articles: List<ArticleDto>? = null

    fun setArticles(articles: List<ArticleDto>): NormDto {
        this.articles = articles
        return this
    }
}
