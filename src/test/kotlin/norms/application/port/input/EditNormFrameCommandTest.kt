package de.bund.digitalservice.ris.norms.application.port.input

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class EditNormFrameCommandTest {
    @Test
    fun `can create command with GUID and new long title`() {
        val guid = UUID.randomUUID()
        val properties = EditNormFrameUseCase.NormFrameProperties("new title")
        val command = EditNormFrameUseCase.Command(guid, properties)

        assertTrue(command.guid == guid)
        assertTrue(command.properties.officialLongTitle == "new title")
        assertTrue(command.properties.officialShortTitle == null)
        assertTrue(command.properties.officialAbbreviation == null)
        assertTrue(command.properties.referenceNumber == null)
        assertTrue(command.properties.publicationDate == null)
        assertTrue(command.properties.announcementDate == null)
        assertTrue(command.properties.citationDate == null)
        assertTrue(command.properties.frameKeywords == null)
        assertTrue(command.properties.providerEntity == null)
        assertTrue(command.properties.providerDecidingBody == null)
        assertTrue(command.properties.providerIsResolutionMajority == null)
        assertTrue(command.properties.leadJurisdiction == null)
        assertTrue(command.properties.leadUnit == null)
        assertTrue(command.properties.participationType == null)
        assertTrue(command.properties.participationInstitution == null)
        assertTrue(command.properties.documentTypeName == null)
        assertTrue(command.properties.documentNormCategory == null)
        assertTrue(command.properties.documentTemplateName == null)
        assertTrue(command.properties.subjectFna == null)
        assertTrue(command.properties.subjectPreviousFna == null)
        assertTrue(command.properties.subjectGesta == null)
        assertTrue(command.properties.subjectBgb3 == null)
    }

    @Test
    fun `can create command with optional fields`() {
        val guid = UUID.randomUUID()
        val properties =
            EditNormFrameUseCase.NormFrameProperties(
                "long title",
                "official short title",
                "official abbreviation",
                "reference number",
                LocalDate.parse("2020-10-27"),
                LocalDate.parse("2020-10-28"),
                LocalDate.parse("2020-10-29"),
                "frame keywords",
                "provider entity",
                "provider deciding body",
                true,
                "lead jurisdiction",
                "lead unit",
                "participation type",
                "participation institution",
                "document type name",
                "document norm category",
                "document template name",
                "subject fna",
                "subject previous fna",
                "subject gesta",
                "subject bgb3"
            )
        val command = EditNormFrameUseCase.Command(guid, properties)

        assertTrue(command.guid == guid)
        assertTrue(command.properties.officialLongTitle == "long title")
        assertTrue(command.properties.officialShortTitle == "official short title")
        assertTrue(command.properties.officialAbbreviation == "official abbreviation")
        assertTrue(command.properties.referenceNumber == "reference number")
        assertTrue(command.properties.publicationDate == LocalDate.parse("2020-10-27"))
        assertTrue(command.properties.announcementDate == LocalDate.parse("2020-10-28"))
        assertTrue(command.properties.citationDate == LocalDate.parse("2020-10-29"))
        assertTrue(command.properties.frameKeywords == "frame keywords")
        assertTrue(command.properties.providerEntity == "provider entity")
        assertTrue(command.properties.providerDecidingBody == "provider deciding body")
        assertTrue(command.properties.providerIsResolutionMajority == true)
        assertTrue(command.properties.leadJurisdiction == "lead jurisdiction")
        assertTrue(command.properties.leadUnit == "lead unit")
        assertTrue(command.properties.participationType == "participation type")
        assertTrue(command.properties.participationInstitution == "participation institution")
        assertTrue(command.properties.documentTypeName == "document type name")
        assertTrue(command.properties.documentNormCategory == "document norm category")
        assertTrue(command.properties.documentTemplateName == "document template name")
        assertTrue(command.properties.subjectFna == "subject fna")
        assertTrue(command.properties.subjectPreviousFna == "subject previous fna")
        assertTrue(command.properties.subjectGesta == "subject gesta")
        assertTrue(command.properties.subjectBgb3 == "subject bgb3")
    }
}
