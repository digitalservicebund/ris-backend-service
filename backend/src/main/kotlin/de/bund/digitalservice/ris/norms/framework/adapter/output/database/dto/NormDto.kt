package de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate
import java.util.*

@Table(name = "norms")
data class NormDto(
    @Id
    val guid: UUID,
    @Column("official_long_title")
    val officialLongTitle: String,
    @Column("ris_abbreviation")
    var risAbbreviation: String? = null,
    @Column("document_number")
    var documentNumber: String? = null,
    @Column("document_category")
    var documentCategory: String? = null,

    @Column("official_short_title")
    var officialShortTitle: String? = null,
    @Column("official_abbreviation")
    var officialAbbreviation: String? = null,

    @Column("announcement_date")
    var announcementDate: LocalDate? = null,
    @Column("publication_date")
    var publicationDate: LocalDate? = null,

    @Column("complete_citation")
    var completeCitation: String? = null,

    @Column("celex_number")
    var celexNumber: String? = null,

    @Column("text")
    var text: String? = null,

) : Persistable<UUID> {

    @Transient
    var newEntry: Boolean = true

    override fun getId(): UUID = guid

    override fun isNew(): Boolean = newEntry
}
