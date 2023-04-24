package de.bund.digitalservice.ris.norms.domain.entity

import de.bund.digitalservice.ris.norms.domain.value.Eli
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import de.bund.digitalservice.ris.norms.domain.value.UndefinedDate
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

    var documentTypeName: String? = null,
    var documentNormCategory: String? = null,
    var documentTemplateName: String? = null,

    var providerEntity: String? = null,
    var providerDecidingBody: String? = null,
    var providerIsResolutionMajority: Boolean? = null,

    var officialShortTitle: String? = null,
    var officialAbbreviation: String? = null,

    var entryIntoForceDate: LocalDate? = null,
    var entryIntoForceDateState: UndefinedDate? = null,
    var principleEntryIntoForceDate: LocalDate? = null,
    var principleEntryIntoForceDateState: UndefinedDate? = null,
    var divergentEntryIntoForceDate: LocalDate? = null,
    var divergentEntryIntoForceDateState: UndefinedDate? = null,
    var entryIntoForceNormCategory: String? = null,

    var expirationDate: LocalDate? = null,
    var expirationDateState: UndefinedDate? = null,
    var isExpirationDateTemp: Boolean? = null,
    var principleExpirationDate: LocalDate? = null,
    var principleExpirationDateState: UndefinedDate? = null,
    var divergentExpirationDate: LocalDate? = null,
    var divergentExpirationDateState: UndefinedDate? = null,
    var expirationNormCategory: String? = null,

    var announcementDate: LocalDate? = null,
    var publicationDate: LocalDate? = null,

    var printAnnouncementGazette: String? = null,
    var printAnnouncementYear: String? = null,
    var printAnnouncementNumber: String? = null,
    var printAnnouncementPage: String? = null,
    var printAnnouncementInfo: String? = null,
    var printAnnouncementExplanations: String? = null,
    var digitalAnnouncementMedium: String? = null,
    var digitalAnnouncementDate: LocalDate? = null,
    var digitalAnnouncementEdition: String? = null,
    var digitalAnnouncementYear: String? = null,
    var digitalAnnouncementPage: String? = null,
    var digitalAnnouncementArea: String? = null,
    var digitalAnnouncementAreaNumber: String? = null,
    var digitalAnnouncementInfo: String? = null,
    var digitalAnnouncementExplanations: String? = null,
    var euAnnouncementGazette: String? = null,
    var euAnnouncementYear: String? = null,
    var euAnnouncementSeries: String? = null,
    var euAnnouncementNumber: String? = null,
    var euAnnouncementPage: String? = null,
    var euAnnouncementInfo: String? = null,
    var euAnnouncementExplanations: String? = null,
    var otherOfficialAnnouncement: String? = null,

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

    var categorizedReference: String? = null,

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
    val eli: Eli
        get() =
            Eli(
                getMetadataFromSections(MetadataSectionName.PRINT_ANNOUNCEMENT, MetadatumType.ANNOUNCEMENT_GAZETTE)?.let { it.value as String },
                announcementDate,
                getMetadataFromSections(MetadataSectionName.CITATION_DATE, MetadatumType.DATE)?.let { it.value as LocalDate },
                getMetadataFromSections(MetadataSectionName.CITATION_DATE, MetadatumType.YEAR)?.let { it.value as String },
                getMetadataFromSections(MetadataSectionName.PRINT_ANNOUNCEMENT, MetadatumType.PAGE)?.let { it.value as String },
            )

    private fun getMetadataFromSections(sectionName: MetadataSectionName, type: MetadatumType): Metadatum<*>? = metadataSections
        .filter { it.name == sectionName }
        .flatMap { it.metadata }
        .filter { it.type == type }
        .minByOrNull { it.order }
}
