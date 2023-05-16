package de.bund.digitalservice.ris.norms.domain.entity

import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import utils.createSimpleSections
import utils.factory.norm
import java.time.LocalDate
import java.util.UUID

class NormTest {

    @Test
    fun `can create a norm with only mandatory fields`() {
        val paragraph = Paragraph(UUID.randomUUID(), "marker", "text")
        val article = Article(UUID.randomUUID(), "title", "marker", listOf(paragraph))
        val guid = UUID.randomUUID()
        val norm = Norm(guid = guid, articles = listOf(article), officialLongTitle = "long title")

        assertThat(norm.guid).isEqualTo(guid)
        assertThat(norm.officialLongTitle).isEqualTo("long title")
        assertThat(norm.articles).isEqualTo(listOf(article))
        assertThat(norm.officialShortTitle).isNull()
        assertThat(norm.officialAbbreviation).isNull()
        assertThat(norm.risAbbreviation).isNull()
        assertThat(norm.documentStatusDescription).isNull()
        assertThat(norm.applicationScopeStartDate).isNull()
        assertThat(norm.categorizedReference).isNull()
        assertThat(norm.digitalEvidenceExternalDataNote).isNull()
        assertThat(norm.text).isNull()
    }

    @Test
    fun `can create a norm with a list of metadata`() {
        val guid = UUID.randomUUID()
        val sections = createSimpleSections()
        val norm =
            Norm(
                guid = guid,
                metadataSections = createSimpleSections(),
                officialLongTitle = "long title",
            )

        assertThat(norm.metadataSections.flatMap { it.metadata }).hasSize(2)
        assertThat(norm.metadataSections.flatMap { it.metadata }).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").containsAll(sections.first().metadata)
    }

    @Test
    fun `can create a norm with some optional string fields`() {
        val paragraph = Paragraph(UUID.randomUUID(), "marker", "text")
        val article = Article(UUID.randomUUID(), "title", "marker", listOf(paragraph))
        val guid = UUID.randomUUID()
        val norm =
            Norm(
                guid = guid,
                articles = listOf(article),
                officialLongTitle = "long title",
                officialShortTitle = "short title",
                officialAbbreviation = "ABC",
                risAbbreviation = "ABC",
            )

        assertThat(norm.guid).isEqualTo(guid)
        assertThat(norm.officialLongTitle).isEqualTo("long title")
        assertThat(norm.articles).isEqualTo(listOf(article))
        assertThat(norm.officialShortTitle).isEqualTo("short title")
        assertThat(norm.officialAbbreviation).isEqualTo("ABC")
        assertThat(norm.risAbbreviation).isEqualTo("ABC")
    }

    @Test
    fun `can create a norm with optional date and boolean fields`() {
        val publicationDate = LocalDate.of(2022, 11, 17)
        val announcementDate = LocalDate.of(2022, 11, 18)
        val paragraph = Paragraph(UUID.randomUUID(), "marker", "text")
        val article = Article(UUID.randomUUID(), "title", "marker", listOf(paragraph))
        val guid = UUID.randomUUID()

        val citationDate = Metadatum(LocalDate.of(2022, 11, 19), MetadatumType.DATE)
        val citationDateSection = MetadataSection(MetadataSectionName.CITATION_DATE, listOf(citationDate))

        val resolutionMajority = Metadatum(true, MetadatumType.RESOLUTION_MAJORITY)
        val normProviderSection = MetadataSection(MetadataSectionName.NORM_PROVIDER, listOf(resolutionMajority))

        val norm =
            Norm(
                guid = guid,
                articles = listOf(article),
                officialLongTitle = "long title",
                publicationDate = publicationDate,
                announcementDate = announcementDate,
                risAbbreviation = "ABC",
                documentStatusDescription = "document status description",
                applicationScopeStartDate = LocalDate.of(2022, 11, 18),
                categorizedReference = "categorized reference",
                digitalEvidenceExternalDataNote = "digital evidence external data note",
                text = "text",
                metadataSections = listOf(citationDateSection, normProviderSection),
            )

        assertThat(norm.guid).isEqualTo(guid)
        assertThat(norm.officialLongTitle).isEqualTo("long title")
        assertThat(norm.articles).isEqualTo(listOf(article))
        assertThat(norm.publicationDate).isEqualTo(publicationDate)
        assertThat(norm.announcementDate).isEqualTo(announcementDate)
        assertThat(norm.metadataSections.flatMap { it.metadata }).contains(citationDate)
        assertThat(norm.metadataSections.flatMap { it.metadata }).contains(resolutionMajority)
        assertThat(norm.risAbbreviation).isEqualTo("ABC")
        assertThat(norm.documentStatusDescription).isEqualTo("document status description")
        assertThat(norm.applicationScopeStartDate).isEqualTo(LocalDate.of(2022, 11, 18))
        assertThat(norm.categorizedReference).isEqualTo("categorized reference")
        assertThat(norm.digitalEvidenceExternalDataNote)
            .isEqualTo("digital evidence external data note")
        assertThat(norm.text).isEqualTo("text")
    }

    @Test
    fun `it can create a proper eli from the respective sections`() {
        val printAnnouncementSection = MetadataSection(
            MetadataSectionName.OFFICIAL_REFERENCE,
            listOf(),
            1,
            listOf(
                MetadataSection(
                    MetadataSectionName.PRINT_ANNOUNCEMENT,
                    listOf(
                        Metadatum("BGBl I", MetadatumType.ANNOUNCEMENT_GAZETTE, 1),
                        Metadatum("BGBL II", MetadatumType.ANNOUNCEMENT_GAZETTE, 2),
                        Metadatum("1102", MetadatumType.PAGE, 1),
                        Metadatum("1102", MetadatumType.PAGE, 2),
                    ),
                ),
            ),
        )
        val citationDateSection = MetadataSection(
            MetadataSectionName.CITATION_DATE,
            listOf(
                Metadatum(LocalDate.of(2022, 11, 19), MetadatumType.DATE),
            ),
        )
        val announcementDate = LocalDate.of(2022, 11, 18)
        val guid = UUID.randomUUID()

        val norm =
            Norm(
                guid = guid,
                officialLongTitle = "long title",
                announcementDate = announcementDate,
                metadataSections = listOf(printAnnouncementSection, citationDateSection),
            )

        assertThat(norm.eli.gazette).isEqualTo("bgbl-1")
        assertThat(norm.eli.printAnnouncementGazette).isEqualTo("BGBl I")
        assertThat(norm.eli.citationDate).isEqualTo(LocalDate.of(2022, 11, 19))
        assertThat(norm.eli.announcementDate).isEqualTo(LocalDate.of(2022, 11, 18))
        assertThat(norm.eli.toString()).isEqualTo("eli/bgbl-1/2022/s1102")
    }

    @Test
    fun `can create a norm using type safe builders`() {
        val norm = norm {
            officialLongTitle = "officialLongTitle"
            articles {
                article {
                    title = "Title"
                    paragraphs {
                        paragraph {
                            text = "Paragraph"
                        }
                    }
                }
            }
            metadataSections {
                metadataSection {
                    name = MetadataSectionName.OFFICIAL_REFERENCE
                    sections {
                        metadataSection {
                            name = MetadataSectionName.PRINT_ANNOUNCEMENT
                            metadata {
                                metadatum {
                                    value = "PrintAnnouncementGazette"
                                    type = MetadatumType.ANNOUNCEMENT_GAZETTE
                                }
                            }
                        }
                    }
                }
            }
            files {
                file {
                    name = "file.zip"
                }
            }
        }
        assertThat(norm.officialLongTitle).isEqualTo("officialLongTitle")
        assertThat(norm.articles.first().title).isEqualTo("Title")
        assertThat(norm.articles.first().paragraphs.first().text).isEqualTo("Paragraph")
        assertThat(norm.metadataSections.first().name).isEqualTo(MetadataSectionName.OFFICIAL_REFERENCE)
        assertThat(norm.metadataSections.first().metadata).isEmpty()
        assertThat(norm.metadataSections.first().sections?.first()?.metadata?.first()?.type).isEqualTo(MetadatumType.ANNOUNCEMENT_GAZETTE)
        assertThat(norm.metadataSections.first().sections?.first()?.metadata?.first()?.value).isEqualTo("PrintAnnouncementGazette")
        assertThat(norm.files.first().name).isEqualTo("file.zip")
    }

    @Test
    fun `it can create retrieve first metadata in a flat sections list`() {
        val leadSection = MetadataSection(
            MetadataSectionName.LEAD,
            listOf(
                Metadatum("jurisdiction1", MetadatumType.LEAD_JURISDICTION, 1),
                Metadatum("jurisdiction2", MetadatumType.LEAD_JURISDICTION, 2),
                Metadatum("lead1", MetadatumType.LEAD_UNIT, 1),
                Metadatum("lead2", MetadatumType.LEAD_UNIT, 2),
            ),
        )
        val citationDateSection = MetadataSection(
            MetadataSectionName.CITATION_DATE,
            listOf(
                Metadatum(LocalDate.of(2022, 11, 19), MetadatumType.DATE),
            ),
        )

        val norm =
            Norm(
                guid = UUID.randomUUID(),
                officialLongTitle = "long title",
                metadataSections = listOf(leadSection, citationDateSection),
            )

        assertThat(norm.getFirstMetadatum(MetadataSectionName.LEAD, MetadatumType.LEAD_JURISDICTION)?.value).isEqualTo("jurisdiction1")
        assertThat(norm.getFirstMetadatum(MetadataSectionName.LEAD, MetadatumType.LEAD_UNIT)?.value).isEqualTo("lead1")
    }

    @Test
    fun `it can retrieve first metadata in a tree of sections with two levels`() {
        val printAnnouncementSection1 = MetadataSection(
            MetadataSectionName.PRINT_ANNOUNCEMENT,
            listOf(
                Metadatum("gazette1", MetadatumType.ANNOUNCEMENT_GAZETTE, 1),
                Metadatum("gazette2", MetadatumType.ANNOUNCEMENT_GAZETTE, 2),
            ),
            1,
        )
        val printAnnouncementSection2 = MetadataSection(
            MetadataSectionName.PRINT_ANNOUNCEMENT,
            listOf(
                Metadatum("gazette3", MetadatumType.ANNOUNCEMENT_GAZETTE, 1),
                Metadatum("gazette4", MetadatumType.ANNOUNCEMENT_GAZETTE, 2),
            ),
            2,
        )
        val referenceSection1 = MetadataSection(MetadataSectionName.OFFICIAL_REFERENCE, listOf(), 1, listOf(printAnnouncementSection1))
        val referenceSection2 = MetadataSection(MetadataSectionName.OFFICIAL_REFERENCE, listOf(), 2, listOf(printAnnouncementSection2))

        val norm =
            Norm(
                guid = UUID.randomUUID(),
                officialLongTitle = "long title",
                metadataSections = listOf(referenceSection1, referenceSection2),
            )

        assertThat(norm.getFirstMetadatum(MetadataSectionName.PRINT_ANNOUNCEMENT, MetadatumType.ANNOUNCEMENT_GAZETTE, MetadataSectionName.OFFICIAL_REFERENCE)?.value).isEqualTo("gazette1")
    }
}
