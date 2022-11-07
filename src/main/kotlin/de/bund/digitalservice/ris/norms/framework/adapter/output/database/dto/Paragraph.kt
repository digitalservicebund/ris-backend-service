package de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto

import java.util.UUID
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.Table

@Table(name = "paragraphs")
data class Paragraph(
    @Id
    @GeneratedValue
    val guid: UUID,
    val marker: String,
    val text: String
) {
    @ManyToOne
    lateinit var article: Article
}
