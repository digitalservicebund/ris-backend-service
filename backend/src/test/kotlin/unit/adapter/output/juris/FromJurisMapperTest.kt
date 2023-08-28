package unit.adapter.output.juris

import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.AGE_INDICATION
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.ANNOUNCEMENT_DATE
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.CATEGORIZED_REFERENCE
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.CITATION_DATE
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.DIGITAL_ANNOUNCEMENT
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_DEFINED
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.DIVERGENT_EXPIRATION
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.DIVERGENT_EXPIRATION_UNDEFINED
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.DOCUMENT_STATUS
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.DOCUMENT_STATUS_SECTION
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.DOCUMENT_TYPE
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.ENTRY_INTO_FORCE
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.EXPIRATION
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.FOOTNOTES
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.LEAD
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.NORM
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.NORM_PROVIDER
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.OFFICIAL_REFERENCE
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.OTHER_STATUS
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.PARTICIPATION
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.PRINCIPLE_ENTRY_INTO_FORCE
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.PRINCIPLE_EXPIRATION
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.PRINT_ANNOUNCEMENT
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.REISSUE
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.REPEAL
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.STATUS
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.STATUS_INDICATION
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName.SUBJECT_AREA
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import de.bund.digitalservice.ris.norms.domain.value.UndefinedDate
import de.bund.digitalservice.ris.norms.framework.adapter.output.juris.mapDataToDomain
import de.bund.digitalservice.ris.norms.juris.converter.model.Article as ArticleData
import de.bund.digitalservice.ris.norms.juris.converter.model.CategorizedReference
import de.bund.digitalservice.ris.norms.juris.converter.model.DigitalAnnouncement
import de.bund.digitalservice.ris.norms.juris.converter.model.DivergentEntryIntoForce
import de.bund.digitalservice.ris.norms.juris.converter.model.DivergentExpiration
import de.bund.digitalservice.ris.norms.juris.converter.model.DocumentStatus
import de.bund.digitalservice.ris.norms.juris.converter.model.DocumentType
import de.bund.digitalservice.ris.norms.juris.converter.model.Footnote
import de.bund.digitalservice.ris.norms.juris.converter.model.Lead
import de.bund.digitalservice.ris.norms.juris.converter.model.Norm as NormData
import de.bund.digitalservice.ris.norms.juris.converter.model.NormProvider
import de.bund.digitalservice.ris.norms.juris.converter.model.Paragraph
import de.bund.digitalservice.ris.norms.juris.converter.model.Participation
import de.bund.digitalservice.ris.norms.juris.converter.model.PrintAnnouncement
import de.bund.digitalservice.ris.norms.juris.converter.model.Reissue
import de.bund.digitalservice.ris.norms.juris.converter.model.Status
import de.bund.digitalservice.ris.norms.juris.converter.model.SubjectArea
import java.time.LocalDate
import java.util.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FromJurisMapperTest {

  @Test
  fun `it correctly maps the data to domain`() {
    val extractedData =
        NormData(
            articles =
                listOf(
                    ArticleData(
                        "articleTitle",
                        "articleMarker",
                        listOf(Paragraph("paragraphMarker", "paragraphText")),
                    ),
                ),
            ageIndicationStartList = listOf("ageIndicationStart"),
            ageOfMajorityIndicationList = listOf("ageOfMajorityIndication"),
            announcementDate = "2020-10-10",
            categorizedReferences = listOf(CategorizedReference("category")),
            celexNumber = "celexNumber",
            citationDateList = listOf("2020-10-01"),
            definitionList = listOf("definition"),
            digitalAnnouncementList = listOf(DigitalAnnouncement("2020", "1", "bgbl")),
            divergentDocumentNumber = "divergentDocumentNumber",
            divergentEntryIntoForceList =
                listOf(DivergentEntryIntoForce("2020-10-01", null, "normCategory")),
            divergentExpirationsList =
                listOf(DivergentExpiration("2020-10-02", "UNDEFINED_FUTURE", "normCategory1")),
            documentCategory = "documentCategory",
            documentStatus =
                listOf(
                    DocumentStatus(
                        listOf("documentStatusWorkNote"), "documentStatusDescription", "2021")),
            documentType =
                DocumentType("documentName", listOf("documentTemplateName"), listOf("category")),
            entryIntoForceDate = "2023-06-15",
            entryIntoForceDateState = null,
            expirationDate = null,
            expirationDateState = "UNDEFINED_UNKNOWN",
            frameKeywordList = listOf("frameKeyword"),
            leadList = listOf(Lead("jurisdiction", "unit")),
            normProviderList = listOf(NormProvider("providerEntity", "providerBody", true)),
            officialAbbreviation = "officialAbbreviation",
            officialLongTitle = "officialLongTitle",
            officialShortTitle = "officialShortTitle",
            participationList =
                listOf(Participation("participationType", "participationInstitution")),
            principleEntryIntoForceDate = "2024-10-10",
            principleEntryIntoForceDateState = null,
            principleExpirationDate = "2020-10-10",
            principleExpirationDateState = null,
            printAnnouncementList = listOf(PrintAnnouncement("2020", "1", "bgbl")),
            referenceNumberList = listOf("referenceNumber"),
            risAbbreviation = "risAbbreviation",
            risAbbreviationInternationalLawList = listOf("risAbbreviationInternationalLaw"),
            statusList =
                listOf(Status("statusNote", "statusDescription", "2010-02-03", "statusReference")),
            reissueList =
                listOf(Reissue("reissueNote", "reissueArticle", "2011-12-15", "reissueReference")),
            repealList = listOf("repealReferences"),
            otherStatusList = listOf("otherStatusNote"),
            subjectAreaList = listOf(SubjectArea("subjectFna", "subjectGesta")),
            text = "text",
            unofficialAbbreviationList = listOf("unofficialAbbreviation"),
            unofficialLongTitleList = listOf("unofficialLongTitle"),
            unofficialReferenceList = listOf("unofficialReference"),
            unofficialShortTitleList = listOf("unofficialShortTitle"),
            validityRuleList = listOf("validityRule"),
            footnotes =
                listOf(
                    Footnote(
                        reference = "reference 1",
                        footnoteChange =
                            listOf(Pair(1, "footnoteChange A"), Pair(3, "footnoteChange B")),
                        footnoteComment =
                            listOf(Pair(2, "footnoteComment A"), Pair(5, "footnoteComment B")),
                        footnoteDecision =
                            listOf(Pair(6, "footnoteDecision A"), Pair(4, "footnoteDecision B")),
                        footnoteStateLaw =
                            listOf(Pair(7, "footnoteStateLaw A"), Pair(8, "footnoteStateLaw B")),
                        footnoteEuLaw =
                            listOf(Pair(12, "footnoteEuLaw A"), Pair(10, "footnoteEuLaw B")),
                        otherFootnote =
                            listOf(Pair(9, "otherFootnote A"), Pair(11, "otherFootnote B")),
                    ),
                    Footnote(
                        reference = "reference 2",
                        footnoteChange =
                            listOf(
                                Pair(1, "another footnoteChange A"),
                                Pair(2, "another footnoteChange B"))),
                ),
        )

    val guid = UUID.randomUUID()

    val domainNorm = mapDataToDomain(guid, extractedData)

    assertThat(domainNorm.guid).isEqualTo(guid)
    assertThat(domainNorm.documentation).hasSize(1)

    assertThat(domainNorm.documentation.first()).isInstanceOf(Article::class.java)
    val article = domainNorm.documentation.first() as Article
    assertThat(article.heading).isEqualTo("articleTitle")
    assertThat(article.marker).isEqualTo("articleMarker")
    assertThat(article.order).isEqualTo(0)
    assertThat(article.paragraphs).hasSize(1)

    val paragraph = article.paragraphs.first()
    assertThat(paragraph.marker).isEqualTo("paragraphMarker")
    assertThat(paragraph.text).isEqualTo("paragraphText")

    val metadataSections = domainNorm.metadataSections
    assertSectionsHasMetadata(
        metadataSections, NORM, MetadatumType.OFFICIAL_LONG_TITLE, "officialLongTitle")
    assertSectionsHasMetadata(
        metadataSections, NORM, MetadatumType.OFFICIAL_SHORT_TITLE, "officialShortTitle")
    assertSectionsHasMetadata(
        metadataSections, NORM, MetadatumType.OFFICIAL_ABBREVIATION, "officialAbbreviation")
    assertSectionsHasMetadata(
        metadataSections, NORM, MetadatumType.DOCUMENT_CATEGORY, "documentCategory")
    assertSectionsHasMetadata(metadataSections, NORM, MetadatumType.TEXT, "text")
    assertSectionsHasMetadata(metadataSections, NORM, MetadatumType.CELEX_NUMBER, "celexNumber")
    assertSectionsHasMetadata(
        metadataSections, NORM, MetadatumType.RIS_ABBREVIATION, "risAbbreviation")
    assertSectionsHasMetadata(metadataSections, NORM, MetadatumType.KEYWORD, "frameKeyword")
    assertSectionsHasMetadata(
        metadataSections, NORM, MetadatumType.DIVERGENT_DOCUMENT_NUMBER, "divergentDocumentNumber")
    assertSectionsHasMetadata(
        metadataSections,
        NORM,
        MetadatumType.RIS_ABBREVIATION_INTERNATIONAL_LAW,
        "risAbbreviationInternationalLaw")
    assertSectionsHasMetadata(
        metadataSections, NORM, MetadatumType.UNOFFICIAL_ABBREVIATION, "unofficialAbbreviation")
    assertSectionsHasMetadata(
        metadataSections, NORM, MetadatumType.UNOFFICIAL_LONG_TITLE, "unofficialLongTitle")
    assertSectionsHasMetadata(
        metadataSections, NORM, MetadatumType.UNOFFICIAL_REFERENCE, "unofficialReference")
    assertSectionsHasMetadata(
        metadataSections, NORM, MetadatumType.UNOFFICIAL_SHORT_TITLE, "unofficialShortTitle")
    assertSectionsHasMetadata(metadataSections, NORM, MetadatumType.VALIDITY_RULE, "validityRule")
    assertSectionsHasMetadata(
        metadataSections, NORM, MetadatumType.REFERENCE_NUMBER, "referenceNumber")
    assertSectionsHasMetadata(metadataSections, NORM, MetadatumType.DEFINITION, "definition")
    assertSectionsHasMetadata(
        metadataSections, NORM, MetadatumType.AGE_OF_MAJORITY_INDICATION, "ageOfMajorityIndication")
    assertThat(
            metadataSections
                .first { it.name == SUBJECT_AREA && it.order == 0 }
                .metadata
                .first { it.type == MetadatumType.SUBJECT_FNA }
                .value)
        .isEqualTo("subjectFna")
    assertThat(
            metadataSections
                .first { it.name == SUBJECT_AREA && it.order == 1 }
                .metadata
                .first { it.type == MetadatumType.SUBJECT_GESTA }
                .value)
        .isEqualTo("subjectGesta")
    assertSectionsHasMetadata(metadataSections, LEAD, MetadatumType.LEAD_UNIT, "unit")
    assertSectionsHasMetadata(
        metadataSections, LEAD, MetadatumType.LEAD_JURISDICTION, "jurisdiction")
    assertSectionsHasMetadata(
        metadataSections, DOCUMENT_TYPE, MetadatumType.TEMPLATE_NAME, "documentTemplateName")
    assertSectionsHasMetadata(
        metadataSections, DOCUMENT_TYPE, MetadatumType.TYPE_NAME, "documentName")
    assertSectionsHasMetadata(
        metadataSections, PARTICIPATION, MetadatumType.PARTICIPATION_TYPE, "participationType")
    assertSectionsHasMetadata(
        metadataSections,
        PARTICIPATION,
        MetadatumType.PARTICIPATION_INSTITUTION,
        "participationInstitution")
    val officialReferenceSections =
        metadataSections
            .filter { it.name == OFFICIAL_REFERENCE }
            .flatMap { it.sections ?: emptyList() }
    assertSectionsHasMetadata(
        officialReferenceSections, PRINT_ANNOUNCEMENT, MetadatumType.ANNOUNCEMENT_GAZETTE, "bgbl")
    assertSectionsHasMetadata(
        officialReferenceSections, PRINT_ANNOUNCEMENT, MetadatumType.YEAR, "2020")
    assertSectionsHasMetadata(
        officialReferenceSections, PRINT_ANNOUNCEMENT, MetadatumType.PAGE, "1")
    assertSectionsHasMetadata(
        officialReferenceSections, DIGITAL_ANNOUNCEMENT, MetadatumType.ANNOUNCEMENT_MEDIUM, "bgbl")
    assertSectionsHasMetadata(
        officialReferenceSections, DIGITAL_ANNOUNCEMENT, MetadatumType.YEAR, "2020")
    assertSectionsHasMetadata(
        officialReferenceSections, DIGITAL_ANNOUNCEMENT, MetadatumType.EDITION, "1")
    val divergentSections =
        metadataSections
            .filter { it.name in listOf(DIVERGENT_ENTRY_INTO_FORCE, DIVERGENT_EXPIRATION) }
            .flatMap { it.sections ?: emptyList() }
    assertSectionsHasMetadata(
        divergentSections,
        DIVERGENT_ENTRY_INTO_FORCE_DEFINED,
        MetadatumType.DATE,
        LocalDate.of(2020, 10, 1))
    assertSectionsHasMetadata(
        divergentSections,
        DIVERGENT_EXPIRATION_UNDEFINED,
        MetadatumType.UNDEFINED_DATE,
        UndefinedDate.UNDEFINED_FUTURE)
    assertSectionsHasMetadata(
        metadataSections, CITATION_DATE, MetadatumType.DATE, LocalDate.of(2020, 10, 1))
    assertSectionsHasMetadata(
        metadataSections, AGE_INDICATION, MetadatumType.RANGE_START, "ageIndicationStart")
    assertSectionsHasMetadata(
        metadataSections, CATEGORIZED_REFERENCE, MetadatumType.TEXT, "category")
    assertSectionsHasMetadata(
        metadataSections, NORM_PROVIDER, MetadatumType.ENTITY, "providerEntity")
    assertSectionsHasMetadata(
        metadataSections, NORM_PROVIDER, MetadatumType.DECIDING_BODY, "providerBody")
    assertSectionsHasMetadata(
        metadataSections, NORM_PROVIDER, MetadatumType.RESOLUTION_MAJORITY, true)
    assertSectionsHasMetadata(
        metadataSections, ENTRY_INTO_FORCE, MetadatumType.DATE, LocalDate.of(2023, 6, 15))
    val documentStatusSections =
        metadataSections
            .filter { it.name == DOCUMENT_STATUS_SECTION }
            .flatMap { it.sections ?: emptyList() }
    assertSectionsHasMetadata(documentStatusSections, DOCUMENT_STATUS, MetadatumType.YEAR, "2021")
    assertSectionsHasMetadata(
        documentStatusSections, DOCUMENT_STATUS, MetadatumType.WORK_NOTE, "documentStatusWorkNote")
    assertSectionsHasMetadata(
        documentStatusSections,
        DOCUMENT_STATUS,
        MetadatumType.DESCRIPTION,
        "documentStatusDescription")
    assertSectionsHasMetadata(
        metadataSections,
        PRINCIPLE_ENTRY_INTO_FORCE,
        MetadatumType.DATE,
        LocalDate.of(2024, 10, 10))
    assertSectionHasNotMetadataType(metadataSections, EXPIRATION, MetadatumType.DATE)
    assertSectionsHasMetadata(
        metadataSections, PRINCIPLE_EXPIRATION, MetadatumType.DATE, LocalDate.of(2020, 10, 10))
    assertSectionHasNotMetadataType(
        metadataSections, PRINCIPLE_EXPIRATION, MetadatumType.UNDEFINED_DATE)
    val statusIndicationSections =
        metadataSections
            .filter { it.name == STATUS_INDICATION }
            .flatMap { it.sections ?: emptyList() }
    assertSectionsHasMetadata(statusIndicationSections, STATUS, MetadatumType.NOTE, "statusNote")
    assertSectionsHasMetadata(
        statusIndicationSections, STATUS, MetadatumType.DESCRIPTION, "statusDescription")
    assertSectionsHasMetadata(
        statusIndicationSections, STATUS, MetadatumType.DATE, LocalDate.of(2010, 2, 3))
    assertSectionsHasMetadata(
        statusIndicationSections, STATUS, MetadatumType.REFERENCE, "statusReference")
    assertSectionsHasMetadata(statusIndicationSections, REISSUE, MetadatumType.NOTE, "reissueNote")
    assertSectionsHasMetadata(
        statusIndicationSections, REISSUE, MetadatumType.ARTICLE, "reissueArticle")
    assertSectionsHasMetadata(
        statusIndicationSections, REISSUE, MetadatumType.DATE, LocalDate.of(2011, 12, 15))
    assertSectionsHasMetadata(
        statusIndicationSections, REISSUE, MetadatumType.REFERENCE, "reissueReference")
    assertSectionsHasMetadata(
        statusIndicationSections, REPEAL, MetadatumType.TEXT, "repealReferences")
    assertSectionsHasMetadata(
        statusIndicationSections, OTHER_STATUS, MetadatumType.NOTE, "otherStatusNote")

    val footnotesSections = metadataSections.filter { it.name == FOOTNOTES }
    assertThat(footnotesSections).hasSize(2)
    assertSectionHasMetadataWithCorrectOrder(
        footnotesSections[0], MetadatumType.FOOTNOTE_REFERENCE, "reference 1", 1)
    assertSectionHasMetadataWithCorrectOrder(
        footnotesSections[0], MetadatumType.FOOTNOTE_CHANGE, "footnoteChange A", 2)
    assertSectionHasMetadataWithCorrectOrder(
        footnotesSections[0], MetadatumType.FOOTNOTE_COMMENT, "footnoteComment A", 3)
    assertSectionHasMetadataWithCorrectOrder(
        footnotesSections[0], MetadatumType.FOOTNOTE_CHANGE, "footnoteChange B", 4)
    assertSectionHasMetadataWithCorrectOrder(
        footnotesSections[0], MetadatumType.FOOTNOTE_DECISION, "footnoteDecision B", 5)
    assertSectionHasMetadataWithCorrectOrder(
        footnotesSections[0], MetadatumType.FOOTNOTE_COMMENT, "footnoteComment B", 6)
    assertSectionHasMetadataWithCorrectOrder(
        footnotesSections[0], MetadatumType.FOOTNOTE_DECISION, "footnoteDecision A", 7)
    assertSectionHasMetadataWithCorrectOrder(
        footnotesSections[0], MetadatumType.FOOTNOTE_STATE_LAW, "footnoteStateLaw A", 8)
    assertSectionHasMetadataWithCorrectOrder(
        footnotesSections[0], MetadatumType.FOOTNOTE_STATE_LAW, "footnoteStateLaw B", 9)
    assertSectionHasMetadataWithCorrectOrder(
        footnotesSections[0], MetadatumType.FOOTNOTE_OTHER, "otherFootnote A", 10)
    assertSectionHasMetadataWithCorrectOrder(
        footnotesSections[0], MetadatumType.FOOTNOTE_EU_LAW, "footnoteEuLaw B", 11)
    assertSectionHasMetadataWithCorrectOrder(
        footnotesSections[0], MetadatumType.FOOTNOTE_OTHER, "otherFootnote B", 12)
    assertSectionHasMetadataWithCorrectOrder(
        footnotesSections[0], MetadatumType.FOOTNOTE_EU_LAW, "footnoteEuLaw A", 13)
    assertSectionHasMetadataWithCorrectOrder(
        footnotesSections[1], MetadatumType.FOOTNOTE_REFERENCE, "reference 2", 1)
    assertSectionHasMetadataWithCorrectOrder(
        footnotesSections[1], MetadatumType.FOOTNOTE_CHANGE, "another footnoteChange A", 2)
    assertSectionHasMetadataWithCorrectOrder(
        footnotesSections[1], MetadatumType.FOOTNOTE_CHANGE, "another footnoteChange B", 3)

    assertThat(metadataSections.filter { it.name == ANNOUNCEMENT_DATE }).hasSize(1)
    assertSectionsHasMetadata(
        metadataSections, ANNOUNCEMENT_DATE, MetadatumType.DATE, LocalDate.parse("2020-10-10"))
  }

  private fun assertSectionHasNotMetadataType(
      metadataSections: Collection<MetadataSection>,
      name: MetadataSectionName,
      type: MetadatumType,
  ) {
    assertThat(metadataSections.first { it.name == name }.metadata.find { it.type == type })
        .isNull()
  }

  private fun assertSectionsHasMetadata(
      metadataSections: Collection<MetadataSection>,
      name: MetadataSectionName,
      type: MetadatumType,
      value: Any?
  ) {
    assertThat(metadataSections.first { it.name == name }.metadata.first { it.type == type }.value)
        .isEqualTo(value)
  }

  private fun assertSectionHasMetadataWithCorrectOrder(
      metadataSection: MetadataSection,
      type: MetadatumType,
      value: Any?,
      order: Int
  ) {
    assertThat(metadataSection.metadata.find { it.type == type && it.order == order }?.value)
        .isEqualTo(value)
  }
}
