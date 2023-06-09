package de.bund.digitalservice.ris.norms.domain.entity

import de.bund.digitalservice.ris.norms.domain.specification.norm.hasValidSections
import de.bund.digitalservice.ris.norms.domain.value.Eli
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import java.time.LocalDate
import java.util.UUID

data class Norm(
    val guid: UUID,
    val articles: List<Article> = emptyList(),
    val metadataSections: List<MetadataSection> = emptyList(),

    val officialLongTitle: String,
    var risAbbreviation: String? = null,
    var documentNumber: String? = null,
    var documentCategory: String? = null,

    var officialShortTitle: String? = null,
    var officialAbbreviation: String? = null,

    var announcementDate: LocalDate? = null,
    var publicationDate: LocalDate? = null,

    var completeCitation: String? = null,

    var statusNote: String? = null,
    var statusDescription: String? = null,
    var statusDate: LocalDate? = null,
    var statusReference: String? = null,
    var repealNote: String? = null,
    var repealArticle: String? = null,
    var repealDate: LocalDate? = null,
    var repealReferences: String? = null,
    var reissueNote: String? = null,
    var reissueArticle: String? = null,
    var reissueDate: LocalDate? = null,
    var reissueReference: String? = null,
    var otherStatusNote: String? = null,

    var documentStatusWorkNote: String? = null,
    var documentStatusDescription: String? = null,
    var documentStatusDate: LocalDate? = null,
    var documentStatusReference: String? = null,
    var documentStatusEntryIntoForceDate: LocalDate? = null,
    var documentStatusProof: String? = null,
    var documentTextProof: String? = null,
    var otherDocumentNote: String? = null,

    var applicationScopeArea: String? = null,
    var applicationScopeStartDate: LocalDate? = null,
    var applicationScopeEndDate: LocalDate? = null,

    var otherFootnote: String? = null,
    var footnoteChange: String? = null,
    var footnoteComment: String? = null,
    var footnoteDecision: String? = null,
    var footnoteStateLaw: String? = null,
    var footnoteEuLaw: String? = null,

    var digitalEvidenceLink: String? = null,
    var digitalEvidenceRelatedData: String? = null,
    var digitalEvidenceExternalDataNote: String? = null,
    var digitalEvidenceAppendix: String? = null,

    var celexNumber: String? = null,

    var text: String? = null,
    var files: List<FileReference> = listOf(),

) {
    init {
        require(hasValidSections.isSatisfiedBy(this)) {
            "Incorrect section for metadata"
        }
    }
    val eli: Eli
        get() =
            Eli(
                digitalAnnouncementMedium = getFirstMetadatum(MetadataSectionName.DIGITAL_ANNOUNCEMENT, MetadatumType.ANNOUNCEMENT_MEDIUM, MetadataSectionName.OFFICIAL_REFERENCE)?.let { it.value as String },
                printAnnouncementGazette = getFirstMetadatum(MetadataSectionName.PRINT_ANNOUNCEMENT, MetadatumType.ANNOUNCEMENT_GAZETTE, MetadataSectionName.OFFICIAL_REFERENCE)?.let { it.value as String },
                announcementDate = announcementDate,
                citationDate = getFirstMetadatum(MetadataSectionName.CITATION_DATE, MetadatumType.DATE)?.let { it.value as LocalDate },
                citationYear = getFirstMetadatum(MetadataSectionName.CITATION_DATE, MetadatumType.YEAR)?.let { it.value as String },
                printAnnouncementPage = getFirstMetadatum(MetadataSectionName.PRINT_ANNOUNCEMENT, MetadatumType.PAGE, MetadataSectionName.OFFICIAL_REFERENCE)?.let { it.value as String },
                digitalAnnouncementEdition = getFirstMetadatum(MetadataSectionName.DIGITAL_ANNOUNCEMENT, MetadatumType.EDITION, MetadataSectionName.OFFICIAL_REFERENCE)?.let { it.value as String },
                digitalAnnouncementPage = getFirstMetadatum(MetadataSectionName.DIGITAL_ANNOUNCEMENT, MetadatumType.PAGE, MetadataSectionName.OFFICIAL_REFERENCE)?.let { it.value as String },
            )

    fun getFirstMetadatum(section: MetadataSectionName, type: MetadatumType, parent: MetadataSectionName? = null): Metadatum<*>? =
        when (parent) {
            null -> metadataSections
            else -> metadataSections.filter { it.name == parent }.flatMap { it.sections ?: listOf() }
        }.filter { it.name == section }
            .minByOrNull { it.order }
            ?.let {
                it.metadata.filter { metadatum -> metadatum.type == type }.minByOrNull { metadatum -> metadatum.order }
            }
}
