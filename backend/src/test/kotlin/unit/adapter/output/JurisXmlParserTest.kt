package de.bund.digitalservice.ris.norms.framework.adapter.output

import de.bund.digitalservice.ris.norms.application.port.output.ParseJurisXmlOutputPort
import de.bund.digitalservice.ris.norms.domain.value.UndefinedDate
import de.bund.digitalservice.ris.norms.juris.importer.importData
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.ByteBuffer
import java.time.LocalDate
import java.util.UUID
import de.bund.digitalservice.ris.norms.juris.importer.model.Norm as NormData

class JurisXmlParserTest {
    val anyGuid = UUID.randomUUID()
    val anyZipFile = ByteBuffer.allocate(0)

    @BeforeEach
    internal fun beforeEach() {
        val data = NormData()
        mockkStatic(::importData)
        every { importData(any()) } returns data
    }

    @AfterEach
    internal fun afterEach() {
        clearAllMocks()
    }

    @Test
    fun `it uses the given GUID for the norm`() {
        val parser = JurisXmlParser()
        val guid = UUID.randomUUID()
        val command = ParseJurisXmlOutputPort.Command(guid, anyZipFile)

        val norm = parser.parseJurisXml(command).block()

        assertThat(norm?.guid).isEqualTo(guid)
    }

    @Test
    fun `it calls the external library to parse the given ZIP file`() {
        val parser = JurisXmlParser()
        val zipFile = ByteBuffer.allocate(0)
        val command = ParseJurisXmlOutputPort.Command(anyGuid, anyZipFile)

        parser.parseJurisXml(command).block()

        verify(exactly = 1) { importData(zipFile) }
    }

    @Test
    fun `it correctly maps the parsed data to the norm properties`() {
        val parser = JurisXmlParser()
        val data =
            NormData().apply {
                officialLongTitle = "test official long title"
                risAbbreviation = "test ris abbreviation"
                risAbbreviationInternationalLaw = "test ris abbreviation international law"
                documentNumber = "test document number"
                documentCategory = "test document category"
                frameKeywords = "test frame keywords"
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
                unofficialLongTitle = "test unofficial long title"
                unofficialShortTitle = "test unofficial short title"
                unofficialAbbreviation = "test unofficial abbreviation"
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
                unofficialReference = "test unofficial reference"
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
                validityRule = "test validity rule"
                referenceNumber = "test reference number"
                celexNumber = "test celex number"
                definition = "test definition"
                ageOfMajorityIndication = "test age of majority indication"
                text = "test text"
            }
        val command = ParseJurisXmlOutputPort.Command(anyGuid, anyZipFile)
        every { importData(any()) } returns data

        val norm = parser.parseJurisXml(command).block()

        assertThat(norm?.officialLongTitle).isEqualTo("test official long title")
        assertThat(norm?.risAbbreviation).isEqualTo("test ris abbreviation")
        assertThat(norm?.risAbbreviationInternationalLaw)
            .isEqualTo("test ris abbreviation international law")
        assertThat(norm?.documentNumber).isEqualTo("test document number")
        assertThat(norm?.documentCategory).isEqualTo("test document category")
        assertThat(norm?.frameKeywords).isEqualTo("test frame keywords")
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
        assertThat(norm?.unofficialLongTitle).isEqualTo("test unofficial long title")
        assertThat(norm?.unofficialShortTitle).isEqualTo("test unofficial short title")
        assertThat(norm?.unofficialAbbreviation).isEqualTo("test unofficial abbreviation")
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
        assertThat(norm?.printAnnouncementGazette).isEqualTo("test print announcement gazette")
        assertThat(norm?.printAnnouncementYear).isEqualTo("test print announcement year")
        assertThat(norm?.printAnnouncementPage).isEqualTo("test print announcement page")
        assertThat(norm?.unofficialReference).isEqualTo("test unofficial reference")
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
        assertThat(norm?.validityRule).isEqualTo("test validity rule")
        assertThat(norm?.referenceNumber).isEqualTo("test reference number")
        assertThat(norm?.celexNumber).isEqualTo("test celex number")
        assertThat(norm?.definition).isEqualTo("test definition")
        assertThat(norm?.ageOfMajorityIndication).isEqualTo("test age of majority indication")
        assertThat(norm?.text).isEqualTo("test text")
    }
}
