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

    @Column("announcement_date")
    var announcementDate: LocalDate? = null,
    @Column("publication_date")
    var publicationDate: LocalDate? = null,

    @Column("status_note")
    var statusNote: String? = null,
    @Column("status_description")
    var statusDescription: String? = null,
    @Column("status_date")
    var statusDate: LocalDate? = null,
    @Column("status_reference")
    var statusReference: String? = null,
    @Column("repeal_note")
    var repealNote: String? = null,
    @Column("repeal_article")
    var repealArticle: String? = null,
    @Column("repeal_date")
    var repealDate: LocalDate? = null,
    @Column("repeal_references")
    var repealReferences: String? = null,
    @Column("reissue_note")
    var reissueNote: String? = null,
    @Column("reissue_article")
    var reissueArticle: String? = null,
    @Column("reissue_date")
    var reissueDate: LocalDate? = null,
    @Column("reissue_reference")
    var reissueReference: String? = null,
    @Column("other_status_note")
    var otherStatusNote: String? = null,
) : Persistable<UUID> {

    @Transient
    var newEntry: Boolean = true

    override fun getId(): UUID = guid

    override fun isNew(): Boolean = newEntry
}
