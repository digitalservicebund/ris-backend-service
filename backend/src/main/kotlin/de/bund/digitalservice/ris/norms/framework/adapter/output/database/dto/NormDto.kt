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

    @Column("document_status_work_note")
    var documentStatusWorkNote: String? = null,
    @Column("document_status_description")
    var documentStatusDescription: String? = null,
    @Column("document_status_date")
    var documentStatusDate: LocalDate? = null,
    @Column("document_status_reference")
    var documentStatusReference: String? = null,
    @Column("document_status_entry_into_force_date")
    var documentStatusEntryIntoForceDate: LocalDate? = null,
    @Column("document_status_proof")
    var documentStatusProof: String? = null,
    @Column("document_text_proof")
    var documentTextProof: String? = null,
    @Column("other_document_note")
    var otherDocumentNote: String? = null,

    @Column("application_scope_area")
    var applicationScopeArea: String? = null,
    @Column("application_scope_start_date")
    var applicationScopeStartDate: LocalDate? = null,
    @Column("application_scope_end_date")
    var applicationScopeEndDate: LocalDate? = null,

    @Column("other_footnote")
    var otherFootnote: String? = null,
    @Column("footnote_change")
    var footnoteChange: String? = null,
    @Column("footnote_comment")
    var footnoteComment: String? = null,
    @Column("footnote_decision")
    var footnoteDecision: String? = null,
    @Column("footnote_state_law")
    var footnoteStateLaw: String? = null,
    @Column("footnote_eu_law")
    var footnoteEuLaw: String? = null,

    @Column("digital_evidence_link")
    var digitalEvidenceLink: String? = null,
    @Column("digital_evidence_related_data")
    var digitalEvidenceRelatedData: String? = null,
    @Column("digital_evidence_external_data_note")
    var digitalEvidenceExternalDataNote: String? = null,
    @Column("digital_evidence_appendix")
    var digitalEvidenceAppendix: String? = null,

    @Column("celex_number")
    var celexNumber: String? = null,

    @Column("text")
    var text: String? = null,

)
