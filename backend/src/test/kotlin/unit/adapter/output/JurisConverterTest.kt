package de.bund.digitalservice.ris.norms.framework.adapter.output

import de.bund.digitalservice.ris.norms.application.port.output.GenerateNormFileOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.ParseJurisXmlOutputPort
import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.AGE_OF_MAJORITY_INDICATION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.DATE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.DEFINITION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.DIVERGENT_DOCUMENT_NUMBER
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.KEYWORD
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.LEAD_JURISDICTION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.LEAD_UNIT
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.PARTICIPATION_INSTITUTION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.PARTICIPATION_TYPE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.REFERENCE_NUMBER
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.RIS_ABBREVIATION_INTERNATIONAL_LAW
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.SUBJECT_FNA
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.SUBJECT_GESTA
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.UNOFFICIAL_ABBREVIATION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.UNOFFICIAL_LONG_TITLE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.UNOFFICIAL_REFERENCE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.UNOFFICIAL_SHORT_TITLE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.VALIDITY_RULE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.YEAR
import de.bund.digitalservice.ris.norms.domain.value.UndefinedDate
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.decodeLocalDate
import de.bund.digitalservice.ris.norms.juris.converter.extractor.extractData
import de.bund.digitalservice.ris.norms.juris.converter.generator.generateZip
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import utils.createRandomNorm
import utils.createSimpleSections
import java.io.File
import java.nio.ByteBuffer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID
import de.bund.digitalservice.ris.norms.juris.converter.model.Article as ArticleData
import de.bund.digitalservice.ris.norms.juris.converter.model.Norm as NormData

class JurisConverterTest {

    @Nested
    inner class ParseJurisXml {
        val anyGuid = UUID.randomUUID()
        val anyZipFile = File.createTempFile("Temp", ".zip")

        @BeforeEach
        internal fun beforeEach() {
            val data = NormData()
            mockkStatic(::extractData)
            every { extractData(any()) } returns data
        }

        @AfterEach
        internal fun afterEach() {
            clearAllMocks()
        }

        @Test
        fun `it saves the date correctly with the right timezone`() {
            System.setProperty("user.timezone", "Australia/Sydney")
            val converter = JurisConverter()
            val guid = UUID.randomUUID()
            val query = ParseJurisXmlOutputPort.Query(guid, anyZipFile.readBytes(), anyZipFile.name)

            val norm = converter.parseJurisXml(query).block()
            val fileCreatedAt = norm?.files?.first()?.createdAt?.hour

            assertThat(fileCreatedAt).isEqualTo(LocalDateTime.now(ZoneId.of("UTC")).hour)
        }

        @Test
        fun `it uses the given GUID for the norm`() {
            val converter = JurisConverter()
            val guid = UUID.randomUUID()
            val query = ParseJurisXmlOutputPort.Query(guid, anyZipFile.readBytes(), anyZipFile.name)

            val norm = converter.parseJurisXml(query).block()

            assertThat(norm?.guid).isEqualTo(guid)
        }

        @Test
        fun `it calls the external library to parse the given ZIP file`() {
            val converter = JurisConverter()
            val zipFile = File.createTempFile("Temp", ".zip")
            val query = ParseJurisXmlOutputPort.Query(anyGuid, anyZipFile.readBytes(), anyZipFile.name)

            converter.parseJurisXml(query).block()

            verify(exactly = 1) { extractData(ByteBuffer.wrap(zipFile.readBytes())) }
        }

        @Test
        fun `it correctly maps the parsed data to the norm properties`() {
            val converter = JurisConverter()
            val data =
                NormData().apply {
                    officialLongTitle = "test official long title"
                    risAbbreviation = "test ris abbreviation"
                    risAbbreviationInternationalLawList = listOf("test ris abbreviation international law")
                    divergentDocumentNumber = "test document number"
                    documentCategory = "test document category"
                    providerEntity = "test provider entity"
                    providerDecidingBody = "test provider deciding body"
                    providerIsResolutionMajority = true
                    participationTypeList = listOf("test participation type")
                    participationInstitutionList = listOf("test participation institution")
                    leadJurisdictionList = listOf("test lead jurisdiction")
                    leadUnitList = listOf("test lead unit")
                    subjectFnaList = listOf("test subject FNA")
                    subjectGestaList = listOf("test subject Gesta")
                    officialShortTitle = "test official short title"
                    officialAbbreviation = "test official abbreviation"
                    unofficialLongTitleList = listOf("test unofficial long title")
                    unofficialShortTitleList = listOf("test unofficial short title")
                    unofficialAbbreviationList = listOf("test unofficial abbreviation")
                    entryIntoForceDate = "2022-01-01"
                    entryIntoForceDateState = "UNDEFINED_UNKNOWN"
                    principleEntryIntoForceDate = "2022-01-02"
                    principleEntryIntoForceDateState = "UNDEFINED_FUTURE"
                    divergentEntryIntoForceDate = "2022-01-03"
                    divergentEntryIntoForceDateState = "UNDEFINED_NOT_PRESENT"
                    entryIntoForceNormCategory = "test entry into force norm category"
                    expirationDate = "2022-01-04"
                    expirationDateState = "UNDEFINED_UNKNOWN"
                    principleExpirationDate = "2022-01-05"
                    principleExpirationDateState = "UNDEFINED_UNKNOWN"
                    divergentExpirationDate = "2022-01-06"
                    divergentExpirationDateState = "UNDEFINED_UNKNOWN"
                    expirationNormCategory = "test expiration norm category"
                    announcementDate = "2022-01-07"
                    citationDateList = listOf("2022-01-08")
                    printAnnouncementGazette = "test print announcement gazette"
                    printAnnouncementYear = "test print announcement year"
                    printAnnouncementPage = "test print announcement page"
                    unofficialReferenceList = listOf("test unofficial reference")
                    statusNote = "test status note"
                    statusDescription = "test status description"
                    statusDate = "2022-01-09"
                    statusReference = "test status reference"
                    repealNote = "test repeal note"
                    repealArticle = "test repeal article"
                    repealDate = "2022-01-10"
                    repealReferences = "test repeal references"
                    reissueNote = "test reissue note"
                    reissueArticle = "test reissue article"
                    reissueDate = "2022-01-11"
                    reissueReference = "test reissue reference"
                    otherStatusNote = "test other status note"
                    documentStatusWorkNote = "test document status work note"
                    documentStatusDescription = "test document status description"
                    documentStatusDate = "2022-01-12"
                    applicationScopeArea = "test application scope area"
                    applicationScopeStartDate = "2022-01-13"
                    applicationScopeEndDate = "2022-01-14"
                    categorizedReference = "test categorized reference"
                    otherFootnote = "test other footnote"
                    validityRuleList = listOf("test validity rule")
                    referenceNumberList = listOf("test reference number")
                    celexNumber = "test celex number"
                    definitionList = listOf("test definition")
                    ageOfMajorityIndicationList = listOf("test age of majority indication")
                    text = "test text"
                }
            val query = ParseJurisXmlOutputPort.Query(anyGuid, anyZipFile.readBytes(), anyZipFile.name)
            every { extractData(any()) } returns data

            val norm = converter.parseJurisXml(query).block()

            assertThat(norm?.officialLongTitle).isEqualTo("test official long title")
            assertThat(norm?.risAbbreviation).isEqualTo("test ris abbreviation")
            assertThat(norm?.documentCategory).isEqualTo("test document category")
            assertThat(norm?.providerEntity).isEqualTo("test provider entity")
            assertThat(norm?.providerDecidingBody).isEqualTo("test provider deciding body")
            assertThat(norm?.providerIsResolutionMajority).isEqualTo(true)
            assertThat(norm?.officialShortTitle).isEqualTo("test official short title")
            assertThat(norm?.officialAbbreviation).isEqualTo("test official abbreviation")
            assertThat(norm?.entryIntoForceDate).isEqualTo(LocalDate.parse("2022-01-01"))
            assertThat(norm?.entryIntoForceDateState).isEqualTo(UndefinedDate.UNDEFINED_UNKNOWN)
            assertThat(norm?.principleEntryIntoForceDate).isEqualTo(LocalDate.parse("2022-01-02"))
            assertThat(norm?.principleEntryIntoForceDateState).isEqualTo(UndefinedDate.UNDEFINED_FUTURE)
            assertThat(norm?.divergentEntryIntoForceDate).isEqualTo(LocalDate.parse("2022-01-03"))
            assertThat(norm?.divergentEntryIntoForceDateState).isEqualTo(UndefinedDate.UNDEFINED_NOT_PRESENT)
            assertThat(norm?.entryIntoForceNormCategory).isEqualTo("test entry into force norm category")
            assertThat(norm?.expirationDate).isEqualTo(LocalDate.parse("2022-01-04"))
            assertThat(norm?.expirationDateState).isEqualTo(UndefinedDate.UNDEFINED_UNKNOWN)
            assertThat(norm?.principleExpirationDate).isEqualTo(LocalDate.parse("2022-01-05"))
            assertThat(norm?.principleExpirationDateState).isEqualTo(UndefinedDate.UNDEFINED_UNKNOWN)
            assertThat(norm?.divergentExpirationDate).isEqualTo(LocalDate.parse("2022-01-06"))
            assertThat(norm?.divergentExpirationDateState).isEqualTo(UndefinedDate.UNDEFINED_UNKNOWN)
            assertThat(norm?.expirationNormCategory).isEqualTo("test expiration norm category")
            assertThat(norm?.announcementDate).isEqualTo(LocalDate.parse("2022-01-07"))
            assertThat(norm?.printAnnouncementGazette).isEqualTo("test print announcement gazette")
            assertThat(norm?.printAnnouncementYear).isEqualTo("test print announcement year")
            assertThat(norm?.printAnnouncementPage).isEqualTo("test print announcement page")
            assertThat(norm?.statusNote).isEqualTo("test status note")
            assertThat(norm?.statusDescription).isEqualTo("test status description")
            assertThat(norm?.statusDate).isEqualTo(LocalDate.parse("2022-01-09"))
            assertThat(norm?.statusReference).isEqualTo("test status reference")
            assertThat(norm?.repealNote).isEqualTo("test repeal note")
            assertThat(norm?.repealArticle).isEqualTo("test repeal article")
            assertThat(norm?.repealDate).isEqualTo(LocalDate.parse("2022-01-10"))
            assertThat(norm?.repealReferences).isEqualTo("test repeal references")
            assertThat(norm?.reissueNote).isEqualTo("test reissue note")
            assertThat(norm?.reissueArticle).isEqualTo("test reissue article")
            assertThat(norm?.reissueDate).isEqualTo(LocalDate.parse("2022-01-11"))
            assertThat(norm?.reissueReference).isEqualTo("test reissue reference")
            assertThat(norm?.otherStatusNote).isEqualTo("test other status note")
            assertThat(norm?.documentStatusWorkNote).isEqualTo("test document status work note")
            assertThat(norm?.documentStatusDescription).isEqualTo("test document status description")
            assertThat(norm?.documentStatusDate).isEqualTo(LocalDate.parse("2022-01-12"))
            assertThat(norm?.applicationScopeArea).isEqualTo("test application scope area")
            assertThat(norm?.applicationScopeStartDate).isEqualTo(LocalDate.parse("2022-01-13"))
            assertThat(norm?.applicationScopeEndDate).isEqualTo(LocalDate.parse("2022-01-14"))
            assertThat(norm?.categorizedReference).isEqualTo("test categorized reference")
            assertThat(norm?.otherFootnote).isEqualTo("test other footnote")
            assertThat(norm?.celexNumber).isEqualTo("test celex number")
            assertThat(norm?.text).isEqualTo("test text")
            val metadata = norm?.metadataSections?.flatMap { it.metadata }
            assertThat(metadata).contains(Metadatum("test document number", DIVERGENT_DOCUMENT_NUMBER, 1))
            assertThat(metadata).contains(Metadatum("test ris abbreviation international law", RIS_ABBREVIATION_INTERNATIONAL_LAW, 1))
            assertThat(metadata).contains(Metadatum("test unofficial long title", UNOFFICIAL_LONG_TITLE, 1))
            assertThat(metadata).contains(Metadatum("test unofficial short title", UNOFFICIAL_SHORT_TITLE, 1))
            assertThat(metadata).contains(Metadatum("test unofficial abbreviation", UNOFFICIAL_ABBREVIATION, 1))
            assertThat(metadata).contains(Metadatum("test unofficial reference", UNOFFICIAL_REFERENCE, 1))
            assertThat(metadata).contains(Metadatum("test validity rule", VALIDITY_RULE, 1))
            assertThat(metadata).contains(Metadatum("test reference number", REFERENCE_NUMBER, 1))
            assertThat(metadata).contains(Metadatum("test definition", DEFINITION, 1))
            assertThat(metadata).contains(Metadatum("test age of majority indication", AGE_OF_MAJORITY_INDICATION, 1))
            assertThat(metadata).contains(Metadatum("test participation type", PARTICIPATION_TYPE, 1))
            assertThat(metadata).contains(Metadatum("test participation institution", PARTICIPATION_INSTITUTION, 1))
            assertThat(metadata).contains(Metadatum("test lead jurisdiction", LEAD_JURISDICTION, 1))
            assertThat(metadata).contains(Metadatum("test lead unit", LEAD_UNIT, 1))
            assertThat(metadata).contains(Metadatum("test subject FNA", SUBJECT_FNA, 1))
            assertThat(metadata).contains(Metadatum("test subject Gesta", SUBJECT_GESTA, 1))
            assertThat(metadata).contains(Metadatum(decodeLocalDate("2022-01-08"), DATE, 1))
        }

        @Test
        fun `it correctly maps the parsed data to metdata using the index order`() {
            val data = NormData().apply { frameKeywordList = listOf("foo", "bar") }
            val query = ParseJurisXmlOutputPort.Query(anyGuid, anyZipFile.readBytes(), anyZipFile.name)
            val converter = JurisConverter()

            every { extractData(any()) } returns data

            val norm = converter.parseJurisXml(query).block()
            val metadata = norm?.metadataSections?.flatMap { it.metadata }
            assertThat(metadata).contains(Metadatum("foo", KEYWORD, 1))
            assertThat(metadata).contains(Metadatum("bar", KEYWORD, 2))
        }

        @Test
        fun `it correctly maps the citation date containing only year to the norm properties`() {
            val converter = JurisConverter()
            val data =
                NormData().apply {
                    officialLongTitle = "test official long title"
                    citationDateList = listOf("2022")
                }
            val query = ParseJurisXmlOutputPort.Query(anyGuid, anyZipFile.readBytes(), anyZipFile.name)
            every { extractData(any()) } returns data

            val norm = converter.parseJurisXml(query).block()

            assertThat(norm?.officialLongTitle).isEqualTo("test official long title")
            assertThat(norm?.metadataSections?.flatMap { it.metadata }).contains(Metadatum("2022", YEAR, 1))
            assertThat(norm?.metadataSections?.flatMap { it.metadata }?.filter { it.type == DATE }).isEmpty()
        }

        @Test
        fun `it returns null citation date and year if date value invalid`() {
            val converter = JurisConverter()
            val data =
                NormData().apply {
                    officialLongTitle = "test official long title"
                    citationDateList = listOf("20112-2-1")
                }
            val query = ParseJurisXmlOutputPort.Query(anyGuid, anyZipFile.readBytes(), anyZipFile.name)
            every { extractData(any()) } returns data

            val norm = converter.parseJurisXml(query).block()

            assertThat(norm?.officialLongTitle).isEqualTo("test official long title")
            assertThat(norm?.metadataSections?.flatMap { it.metadata }?.filter { it.type == DATE }).isEmpty()
            assertThat(norm?.metadataSections?.flatMap { it.metadata }?.filter { it.type == YEAR }).isEmpty()
        }

        @Test
        fun `it returns null citation date and year if date year with letters`() {
            val converter = JurisConverter()
            val data =
                NormData().apply {
                    officialLongTitle = "test official long title"
                    citationDateList = listOf("201c")
                }
            val query = ParseJurisXmlOutputPort.Query(anyGuid, anyZipFile.readBytes(), anyZipFile.name)
            every { extractData(any()) } returns data

            val norm = converter.parseJurisXml(query).block()

            assertThat(norm?.officialLongTitle).isEqualTo("test official long title")
            assertThat(norm?.metadataSections?.flatMap { it.metadata }?.filter { it.type == DATE }).isEmpty()
            assertThat(norm?.metadataSections?.flatMap { it.metadata }?.filter { it.type == YEAR }).isEmpty()
        }

        @Test
        fun `it parses articles`() {
            val converter = JurisConverter()
            val guid = UUID.randomUUID()
            val query = ParseJurisXmlOutputPort.Query(guid, anyZipFile.readBytes(), anyZipFile.name)
            val articleData = ArticleData("title", "marker")
            val data = NormData().apply { articles = listOf(articleData) }

            every { extractData(any()) } returns data

            val norm = converter.parseJurisXml(query).block()

            assertThat(norm?.articles).hasSize(1)
        }
    }

    @Nested
    inner class GenerateNormFile {
        val norm = createRandomNorm().copy(
            metadataSections = listOf(
                MetadataSection(MetadataSectionName.CITATION_DATE, listOf(Metadatum("2002", YEAR))),
            ),
        )
        val normData = mapDomainToData(norm)
        val generatedZipFile = File.createTempFile("Temp", ".zip")

        @BeforeEach
        internal fun beforeEach() {
            mockkStatic(::generateZip)
            mockkStatic(::mapDomainToData)
            every { generateZip(any(), any()) } returns generatedZipFile.readBytes()
        }

        @AfterEach
        internal fun afterEach() {
            clearAllMocks()
        }

        @Test
        fun `it calls the external library to generate the new ZIP file with the correct parameters`() {
            val converter = JurisConverter()
            val zipFile = File.createTempFile("Temp", ".zip")
            val command = GenerateNormFileOutputPort.Command(norm, zipFile.readBytes())

            converter.generateNormFile(command).block()

            verify(exactly = 1) { generateZip(normData, ByteBuffer.wrap(zipFile.readBytes())) }
        }

        @Test
        fun `it return the newly generated zip file`() {
            val converter = JurisConverter()
            val previousZipFile = File.createTempFile("Temp", ".zip")
            val command = GenerateNormFileOutputPort.Command(norm, previousZipFile.readBytes())

            val newFile = converter.generateNormFile(command).block()

            assertThat(newFile).isEqualTo(generatedZipFile.readBytes())
        }

        @Test
        fun `it takes the value of the citation year for the citation date in the external norm entity`() {
            assertThat(norm.metadataSections.flatMap { it.metadata }.filter { it.type == DATE }).isEmpty()

            assertThat(norm.metadataSections.flatMap { it.metadata }.filter { it.type == YEAR }).isNotNull()
            assertThat(normData.citationDateList).isNotNull

            val citationDateYear = norm.metadataSections.flatMap { it.metadata }.filter { it.type == YEAR }.first().value.toString()
            assertThat(normData.citationDateList).contains(citationDateYear)
        }

        @Test
        fun `it correctly maps the norm metadata values including the order`() {
            val normWithMetadata = norm.copy(metadataSections = createSimpleSections())
            val command = GenerateNormFileOutputPort.Command(normWithMetadata, generatedZipFile.readBytes())
            val converter = JurisConverter()

            converter.generateNormFile(command).block()

            verify(exactly = 1) {
                generateZip(
                    withArg {
                        assertThat(it.frameKeywordList).isEqualTo(listOf("foo", "bar"))
                    },
                    any(),
                )
            }
        }
    }
}
