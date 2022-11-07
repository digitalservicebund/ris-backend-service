package de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto

import java.util.UUID
import javax.persistence.CascadeType
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.Table

@Table(name = "articles")
data class Article(
    @Id
    @GeneratedValue
    val guid: UUID,
    val title: String,
    val marker: String
) {
    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    lateinit var paragraphs: List<Paragraph>

    @ManyToOne
    lateinit var norm: Norm
}
