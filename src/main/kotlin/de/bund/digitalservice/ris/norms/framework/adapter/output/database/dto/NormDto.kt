package de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate
import java.util.*

@Table(name = "norms")
data class NormDto(
    @Id
    val id: Int,
    val guid: UUID,
    @Column("long_title")
    val longTitle: String,
    @Column("official_short_title")
    var officialShortTitle: String? = null,
    @Column("official_abbreviation")
    var officialAbbreviation: String? = null,
    @Column("reference_number")
    var referenceNumber: String? = null,
    @Column("publication_date")
    var publicationDate: LocalDate? = null,
    @Column("announcement_date")
    var announcementDate: LocalDate? = null,
    @Column("citation_date")
    var citationDate: LocalDate? = null,
    @Column("frame_keywords")
    var frameKeywords: String? = null,
    @Column("author_entity")
    var authorEntity: String? = null,
    @Column("author_deciding_body")
    var authorDecidingBody: String? = null,
    @Column("author_is_resolution_majority")
    var authorIsResolutionMajority: Boolean? = null,
    @Column("lead_jurisdiction")
    var leadJurisdiction: String? = null,
    @Column("lead_unit")
    var leadUnit: String? = null,
    @Column("participation_type")
    var participationType: String? = null,
    @Column("participation_institution")
    var participationInstitution: String? = null,
    @Column("document_type_name")
    var documentTypeName: String? = null,
    @Column("document_norm_category")
    var documentNormCategory: String? = null,
    @Column("document_template_name")
    var documentTemplateName: String? = null,
    @Column("subject_fna")
    var subjectFna: String? = null,
    @Column("subject_previous_fna")
    var subjectPreviousFna: String? = null,
    @Column("subject_gesta")
    var subjectGesta: String? = null,
    @Column("subject_bgb3")
    var subjectBgb3: String? = null,
    @Column("unofficial_title")
    var unofficialTitle: String? = null,
    @Column("unofficial_short_title")
    var unofficialShortTitle: String? = null,
    @Column("unofficial_abbreviation")
    var unofficialAbbreviation: String? = null,
    @Column("ris_abbreviation")
    var risAbbreviation: String? = null
)
