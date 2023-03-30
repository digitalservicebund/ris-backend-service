package de.bund.digitalservice.ris.norms.framework.adapter.output

import de.bund.digitalservice.ris.norms.application.port.output.GenerateNormFileOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.ParseJurisXmlOutputPort
import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.AGE_OF_MAJORITY_INDICATION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.DEFINITION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.DIVERGENT_DOCUMENT_NUMBER
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.KEYWORD
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.REFERENCE_NUMBER
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.RIS_ABBREVIATION_INTERNATIONAL_LAW
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.UNOFFICIAL_ABBREVIATION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.UNOFFICIAL_LONG_TITLE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.UNOFFICIAL_REFERENCE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.UNOFFICIAL_SHORT_TITLE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.VALIDITY_RULE
import de.bund.digitalservice.ris.norms.domain.value.UndefinedDate
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

            assertThat(fileCreatedAt).isEqualTo(LocalDateTime.now(ZoneId.of("Europe/Berlin")).hour)
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
                    risAbbreviationInternationalLaw = listOf("test ris abbreviation international law")
                    divergentDocumentNumber = "test document number"
                    documentCategory = "test document category"
                    providerEntity = "test provider entity"
                    providerDecidingBody = "test provider deciding body"
                    providerIsResolutionMajority = true
                    participationType = "test participation type"
                    participationInstitution = "test participation institution"
                    leadJurisdiction = "test lead jurisdiction"
                    leadUnit = "test lead unit"
                    subjectFna = "test subject FNA"
                    subjectGesta = "test subject Gesta"
                    officialShortTitle = "test official short title"
                    officialAbbreviation = "test official abbreviation"
                    unofficialLongTitle = listOf("test unofficial long title")
                    unofficialShortTitle = listOf("test unofficial short title")
                    unofficialAbbreviation = listOf("test unofficial abbreviation")
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
                    citationDate = "2022-01-08"
                    printAnnouncementGazette = "test print announcement gazette"
                    printAnnouncementYear = "test print announcement year"
                    printAnnouncementPage = "test print announcement page"
                    unofficialReference = listOf("test unofficial reference")
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
                    validityRule = listOf("test validity rule")
                    referenceNumber = listOf("test reference number")
                    celexNumber = "test celex number"
                    definition = listOf("test definition")
                    ageOfMajorityIndication = listOf("test age of majority indication")
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
            assertThat(norm?.participationType).isEqualTo("test participation type")
            assertThat(norm?.participationInstitution).isEqualTo("test participation institution")
            assertThat(norm?.leadJurisdiction).isEqualTo("test lead jurisdiction")
            assertThat(norm?.leadUnit).isEqualTo("test lead unit")
            assertThat(norm?.subjectFna).isEqualTo("test subject FNA")
            assertThat(norm?.subjectGesta).isEqualTo("test subject Gesta")
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
            assertThat(norm?.citationDate).isEqualTo(LocalDate.parse("2022-01-08"))
            assertThat(norm?.citationYear).isNull()
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

            assertThat(norm?.metadata).contains(Metadatum("test document number", DIVERGENT_DOCUMENT_NUMBER, 0))
            assertThat(norm?.metadata).contains(Metadatum("test ris abbreviation international law", RIS_ABBREVIATION_INTERNATIONAL_LAW, 0))
            assertThat(norm?.metadata).contains(Metadatum("test unofficial long title", UNOFFICIAL_LONG_TITLE, 0))
            assertThat(norm?.metadata).contains(Metadatum("test unofficial short title", UNOFFICIAL_SHORT_TITLE, 0))
            assertThat(norm?.metadata).contains(Metadatum("test unofficial abbreviation", UNOFFICIAL_ABBREVIATION, 0))
            assertThat(norm?.metadata).contains(Metadatum("test unofficial reference", UNOFFICIAL_REFERENCE, 0))
            assertThat(norm?.metadata).contains(Metadatum("test validity rule", VALIDITY_RULE, 0))
            assertThat(norm?.metadata).contains(Metadatum("test reference number", REFERENCE_NUMBER, 0))
            assertThat(norm?.metadata).contains(Metadatum("test definition", DEFINITION, 0))
            assertThat(norm?.metadata).contains(Metadatum("test age of majority indication", AGE_OF_MAJORITY_INDICATION, 0))
        }

        @Test
        fun `it correctly maps the parsed data to metdata using the index order`() {
            val data = NormData().apply { frameKeywords = listOf("foo", "bar") }
            val query = ParseJurisXmlOutputPort.Query(anyGuid, anyZipFile.readBytes(), anyZipFile.name)
            val converter = JurisConverter()

            every { extractData(any()) } returns data

            val norm = converter.parseJurisXml(query).block()

            assertThat(norm?.metadata).contains(Metadatum("foo", KEYWORD, 0))
            assertThat(norm?.metadata).contains(Metadatum("bar", KEYWORD, 1))
        }

        @Test
        fun `it correctly maps the citation date containing only year to the norm properties`() {
            val converter = JurisConverter()
            val data =
                NormData().apply {
                    officialLongTitle = "test official long title"
                    citationDate = "2022"
                }
            val query = ParseJurisXmlOutputPort.Query(anyGuid, anyZipFile.readBytes(), anyZipFile.name)
            every { extractData(any()) } returns data

            val norm = converter.parseJurisXml(query).block()

            assertThat(norm?.officialLongTitle).isEqualTo("test official long title")
            assertThat(norm?.citationDate).isNull()
            assertThat(norm?.citationYear).isEqualTo("2022")
        }

        @Test
        fun `it returns null citation date and year if date value invalid`() {
            val converter = JurisConverter()
            val data =
                NormData().apply {
                    officialLongTitle = "test official long title"
                    citationDate = "20112-2-1"
                }
            val query = ParseJurisXmlOutputPort.Query(anyGuid, anyZipFile.readBytes(), anyZipFile.name)
            every { extractData(any()) } returns data

            val norm = converter.parseJurisXml(query).block()

            assertThat(norm?.officialLongTitle).isEqualTo("test official long title")
            assertThat(norm?.citationDate).isNull()
            assertThat(norm?.citationYear).isNull()
        }

        @Test
        fun `it returns null citation date and year if date year with letters`() {
            val converter = JurisConverter()
            val data =
                NormData().apply {
                    officialLongTitle = "test official long title"
                    citationDate = "201c"
                }
            val query = ParseJurisXmlOutputPort.Query(anyGuid, anyZipFile.readBytes(), anyZipFile.name)
            every { extractData(any()) } returns data

            val norm = converter.parseJurisXml(query).block()

            assertThat(norm?.officialLongTitle).isEqualTo("test official long title")
            assertThat(norm?.citationDate).isNull()
            assertThat(norm?.citationYear).isNull()
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
        val norm = createRandomNorm().apply { citationDate = null }
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
            assertThat(norm.citationDate).isNull()
            assertThat(norm.citationYear).isNotNull
            assertThat(normData.citationDate).isNotNull
            assertThat(normData.citationDate).isEqualTo(norm.citationYear)
        }

        @Test
        fun `it correctly maps the norm metadata values including the order`() {
            val metadata = listOf(Metadatum("foo", KEYWORD, 1), Metadatum("bar", KEYWORD, 0))
            val normWithMetadata = norm.copy(metadata = metadata)
            val command = GenerateNormFileOutputPort.Command(normWithMetadata, generatedZipFile.readBytes())
            val converter = JurisConverter()

            converter.generateNormFile(command).block()

            verify(exactly = 1) {
                generateZip(
                    withArg {
                        assertThat(it.frameKeywords).isEqualTo(listOf("bar", "foo"))
                    },
                    any(),
                )
            }
        }
    }
}
