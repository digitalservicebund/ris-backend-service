package utils.factory

import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.FileReference
import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import utils.randomString
import java.time.LocalDate
import java.util.UUID

fun norm(block: NormBuilder.() -> Unit): Norm = NormBuilder().apply(block).build()

class NormBuilder {
    var guid: UUID = UUID.randomUUID()
    var officialLongTitle = randomString()
    var risAbbreviation = randomString()
    var documentNumber = randomString()
    var documentCategory = randomString()
    var documentTypeName = randomString()
    var documentNormCategory = randomString()
    var documentTemplateName = randomString()
    var officialShortTitle = randomString()
    var officialAbbreviation = randomString()
    var entryIntoForceDate = LocalDate.now()
    var entryIntoForceDateState = null
    var principleEntryIntoForceDate = LocalDate.now()
    var principleEntryIntoForceDateState = null
    var divergentEntryIntoForceDate = LocalDate.now()
    var divergentEntryIntoForceDateState = null
    var entryIntoForceNormCategory = randomString()
    var expirationDate = LocalDate.now()
    var expirationDateState = null
    var isExpirationDateTemp = true
    var principleExpirationDate = LocalDate.now()
    var principleExpirationDateState = null
    var divergentExpirationDate = LocalDate.now()
    var divergentExpirationDateState = null
    var expirationNormCategory = randomString()
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
    var applicationScopeStartDate = LocalDate.now()
    var applicationScopeEndDate = LocalDate.now()
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
        risAbbreviation = risAbbreviation,
        documentNumber = documentNumber,
        documentCategory = documentCategory,
        officialShortTitle = officialShortTitle,
        officialAbbreviation = officialAbbreviation,
        entryIntoForceDate = entryIntoForceDate,
        entryIntoForceDateState = entryIntoForceDateState,
        principleEntryIntoForceDate = principleEntryIntoForceDate,
        principleEntryIntoForceDateState = principleEntryIntoForceDateState,
        expirationDate = expirationDate,
        expirationDateState = expirationDateState,
        isExpirationDateTemp = isExpirationDateTemp,
        principleExpirationDate = principleExpirationDate,
        principleExpirationDateState = principleExpirationDateState,
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

class MetadataSections : ArrayList<MetadataSection>() {
    fun metadataSection(block: MetadataSectionBuilder.() -> Unit) = add(MetadataSectionBuilder().apply(block).build())
}

class Articles : ArrayList<Article>() {
    fun article(block: ArticleBuilder.() -> Unit) = add(ArticleBuilder().apply(block).build())
}

class Files : ArrayList<FileReference>() {
    fun file(block: FileBuilder.() -> Unit) = add(FileBuilder().apply(block).build())
}
