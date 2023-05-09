package utils.factory

import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.FileReference
import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import utils.randomString
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

fun norm(block: NormBuilder.() -> Unit): Norm = NormBuilder().apply(block).build()

class NormBuilder {
    var guid = UUID.randomUUID()
    var officialLongTitle = randomString()
    var risAbbreviation = randomString()
    var documentNumber = randomString()
    var documentCategory = null
    var documentTypeName = randomString()
    var documentNormCategory = null
    var documentTemplateName = randomString()
    var officialShortTitle = randomString()
    var officialAbbreviation = randomString()
    var entryIntoForceDate = LocalDate.now()
    var entryIntoForceDateState = null
    var principleEntryIntoForceDate = LocalDate.now()
    var principleEntryIntoForceDateState = null
    var divergentEntryIntoForceDate = LocalDate.now()
    var divergentEntryIntoForceDateState = null
    var entryIntoForceNormCategory = null
    var expirationDate = LocalDate.now()
    var expirationDateState = null
    var isExpirationDateTemp = null
    var principleExpirationDate = LocalDate.now()
    var principleExpirationDateState = null
    var divergentExpirationDate = LocalDate.now()
    var divergentExpirationDateState = null
    var expirationNormCategory = null
    var announcementDate = LocalDate.now()
    var publicationDate = LocalDate.now()
    var completeCitation = randomString()
    var statusNote = randomString()
    var statusDescription = randomString()
    var statusDate = LocalDate.now()
    var statusReference = randomString()
    var repealNote = randomString()
    var repealArticle = randomString()
    var repealDate = LocalDate.now()
    var repealReferences = randomString()
    var reissueNote = randomString()
    var reissueArticle = randomString()
    var reissueDate = LocalDate.now()
    var reissueReference = randomString()
    var otherStatusNote = randomString()
    var documentStatusWorkNote = randomString()
    var documentStatusDescription = randomString()
    var documentStatusDate = LocalDate.now()
    var documentStatusReference = randomString()
    var documentStatusEntryIntoForceDate = LocalDate.now()
    var documentStatusProof = randomString()
    var documentTextProof = randomString()
    var otherDocumentNote = randomString()
    var applicationScopeArea = randomString()
    var applicationScopeStartDate = null
    var applicationScopeEndDate = null
    var categorizedReference = randomString()
    var otherFootnote = randomString()
    var footnoteChange = randomString()
    var footnoteComment = randomString()
    var footnoteDecision = randomString()
    var footnoteStateLaw = randomString()
    var footnoteEuLaw = randomString()
    var digitalEvidenceLink = randomString()
    var digitalEvidenceRelatedData = randomString()
    var digitalEvidenceExternalDataNote = randomString()
    var digitalEvidenceAppendix = randomString()
    var celexNumber = randomString()
    var text = randomString()

    private val metadataSections = mutableListOf<MetadataSection>()
    private val articles = mutableListOf<Article>()
    private val files = mutableListOf<FileReference>()

    fun metadataSections(block: MetadataSections.() -> Unit) = metadataSections.addAll(MetadataSections().apply(block))
    fun articles(block: Articles.() -> Unit) = articles.addAll(Articles().apply(block))
    fun files(block: Files.() -> Unit) = files.addAll(Files().apply(block))

    fun build(): Norm = Norm(
        guid = guid,
        officialLongTitle = officialLongTitle,
        metadataSections = metadataSections,
        files = files,
        articles = articles,
        risAbbreviation = randomString(),
        documentNumber = documentNumber,
        documentCategory = documentCategory,
        documentTypeName = documentTypeName,
        documentNormCategory = documentNormCategory,
        documentTemplateName = documentTemplateName,
        officialShortTitle = officialShortTitle,
        officialAbbreviation = officialAbbreviation,
        entryIntoForceDate = entryIntoForceDate,
        entryIntoForceDateState = entryIntoForceDateState,
        principleEntryIntoForceDate = principleEntryIntoForceDate,
        principleEntryIntoForceDateState = principleEntryIntoForceDateState,
        divergentEntryIntoForceDate = divergentEntryIntoForceDate,
        divergentEntryIntoForceDateState = divergentEntryIntoForceDateState,
        entryIntoForceNormCategory = entryIntoForceNormCategory,
        expirationDate = expirationDate,
        expirationDateState = expirationDateState,
        isExpirationDateTemp = isExpirationDateTemp,
        principleExpirationDate = principleExpirationDate,
        principleExpirationDateState = principleExpirationDateState,
        divergentExpirationDate = divergentExpirationDate,
        divergentExpirationDateState = divergentExpirationDateState,
        expirationNormCategory = expirationNormCategory,
        announcementDate = announcementDate,
        publicationDate = publicationDate,
        completeCitation = completeCitation,
        statusNote = statusNote,
        statusDescription = statusDescription,
        statusDate = statusDate,
        statusReference = statusReference,
        repealNote = repealNote,
        repealArticle = repealArticle,
        repealDate = repealDate,
        repealReferences = repealReferences,
        reissueNote = reissueNote,
        reissueArticle = reissueArticle,
        reissueDate = reissueDate,
        reissueReference = reissueReference,
        otherStatusNote = otherStatusNote,
        documentStatusWorkNote = documentStatusWorkNote,
        documentStatusDescription = documentStatusDescription,
        documentStatusDate = documentStatusDate,
        documentStatusReference = documentStatusReference,
        documentStatusEntryIntoForceDate = documentStatusEntryIntoForceDate,
        documentStatusProof = documentStatusProof,
        documentTextProof = documentTextProof,
        otherDocumentNote = otherDocumentNote,
        applicationScopeArea = applicationScopeArea,
        applicationScopeStartDate = applicationScopeStartDate,
        applicationScopeEndDate = applicationScopeEndDate,
        categorizedReference = categorizedReference,
        otherFootnote = otherFootnote,
        footnoteChange = footnoteChange,
        footnoteComment = footnoteComment,
        footnoteDecision = footnoteDecision,
        footnoteStateLaw = footnoteStateLaw,
        footnoteEuLaw = footnoteEuLaw,
        digitalEvidenceLink = digitalEvidenceLink,
        digitalEvidenceRelatedData = digitalEvidenceRelatedData,
        digitalEvidenceExternalDataNote = digitalEvidenceExternalDataNote,
        digitalEvidenceAppendix = digitalEvidenceAppendix,
        celexNumber = celexNumber,
        text = text,
    )
}

class MetadatumBuilder {
    var value: String? = null
    var type: MetadatumType = MetadatumType.LEAD_UNIT
    val order: Int = 1

    fun build(): Metadatum<*> = Metadatum(value, type, order)
}

class FileBuilder {
    var name: String = ""
    var hash: String = ""
    val createdAt: LocalDateTime = LocalDateTime.now()

    fun build(): FileReference = FileReference(name, hash, createdAt)
}

class ParagraphBuilder {
    var guid: UUID = UUID.randomUUID()
    var marker: String? = null
    var text: String = ""

    fun build(): Paragraph = Paragraph(guid, marker, text)
}

class ArticleBuilder {
    var guid: UUID = UUID.randomUUID()
    var title: String = ""
    var marker: String = ""
    private var paragraphs = mutableListOf<Paragraph>()

    fun paragraphs(block: Paragraphs.() -> Unit) = paragraphs.addAll(Paragraphs().apply(block))

    fun build(): Article = Article(guid, title, marker, paragraphs)
}

class MetadataSectionBuilder {
    var name: MetadataSectionName = MetadataSectionName.LEAD
    private val metadata = mutableListOf<Metadatum<*>>()
    var order: Int = 1
    var sections: List<MetadataSection>? = null

    fun metadata(block: Metadata.() -> Unit) = metadata.addAll(Metadata().apply(block))

    fun build(): MetadataSection = MetadataSection(name, metadata, order, sections)
}

class MetadataSections : ArrayList<MetadataSection>() {
    fun metadataSection(block: MetadataSectionBuilder.() -> Unit) = add(MetadataSectionBuilder().apply(block).build())
}

class Metadata : ArrayList<Metadatum<*>>() {
    fun metadatum(block: MetadatumBuilder.() -> Unit) = add(MetadatumBuilder().apply(block).build())
}

class Articles : ArrayList<Article>() {
    fun article(block: ArticleBuilder.() -> Unit) = add(ArticleBuilder().apply(block).build())
}

class Paragraphs : ArrayList<Paragraph>() {
    fun paragraph(block: ParagraphBuilder.() -> Unit) = add(ParagraphBuilder().apply(block).build())
}

class Files : ArrayList<FileReference>() {
    fun file(block: FileBuilder.() -> Unit) = add(FileBuilder().apply(block).build())
}
