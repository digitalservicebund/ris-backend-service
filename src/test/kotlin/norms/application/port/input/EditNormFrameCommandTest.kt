package de.bund.digitalservice.ris.norms.application.port.input

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class EditNormFrameCommandTest {
    @Test
    fun `can create command with GUID and new long title`() {
        val guid = UUID.randomUUID()
        val command = EditNormFrameUseCase.Command(guid, "new title")

        assertTrue(command.guid == guid)
        assertTrue(command.longTitle == "new title")
        assertTrue(command.officialShortTitle == null)
        assertTrue(command.officialAbbreviation == null)
        assertTrue(command.referenceNumber == null)
        assertTrue(command.publicationDate == null)
        assertTrue(command.announcementDate == null)
        assertTrue(command.citationDate == null)
        assertTrue(command.frameKeywords == null)
        assertTrue(command.authorEntity == null)
        assertTrue(command.authorDecidingBody == null)
        assertTrue(command.authorIsResolutionMajority == null)
        assertTrue(command.leadJurisdiction == null)
        assertTrue(command.leadUnit == null)
        assertTrue(command.participationType == null)
        assertTrue(command.participationInstitution == null)
        assertTrue(command.documentTypeName == null)
        assertTrue(command.documentNormCategory == null)
        assertTrue(command.documentTemplateName == null)
        assertTrue(command.subjectFna == null)
        assertTrue(command.subjectPreviousFna == null)
        assertTrue(command.subjectGesta == null)
        assertTrue(command.subjectBgb3 == null)
    }

    @Test
    fun `can create command with optional fields`() {
        val guid = UUID.randomUUID()
        val command = EditNormFrameUseCase.Command(
            guid, "long title", "official short title", "official abbreviation",
            "reference number", LocalDate.parse("2020-10-27"), LocalDate.parse("2020-10-28"), LocalDate.parse("2020-10-29"),
            "frame keywords", "author entity", "author deciding body",
            true, "lead jurisdiction", "lead unit", "participation type",
            "participation institution", "document type name", "document norm category",
            "document template name", "subject fna", "subject previous fna",
            "subject gesta", "subject bgb3"
        )
        assertTrue(command.guid == guid)
        assertTrue(command.longTitle == "long title")
        assertTrue(command.officialShortTitle == "official short title")
        assertTrue(command.officialAbbreviation == "official abbreviation")
        assertTrue(command.referenceNumber == "reference number")
        assertTrue(command.publicationDate == LocalDate.parse("2020-10-27"))
        assertTrue(command.announcementDate == LocalDate.parse("2020-10-28"))
        assertTrue(command.citationDate == LocalDate.parse("2020-10-29"))
        assertTrue(command.frameKeywords == "frame keywords")
        assertTrue(command.authorEntity == "author entity")
        assertTrue(command.authorDecidingBody == "author deciding body")
        assertTrue(command.authorIsResolutionMajority == true)
        assertTrue(command.leadJurisdiction == "lead jurisdiction")
        assertTrue(command.leadUnit == "lead unit")
        assertTrue(command.participationType == "participation type")
        assertTrue(command.participationInstitution == "participation institution")
        assertTrue(command.documentTypeName == "document type name")
        assertTrue(command.documentNormCategory == "document norm category")
        assertTrue(command.documentTemplateName == "document template name")
        assertTrue(command.subjectFna == "subject fna")
        assertTrue(command.subjectPreviousFna == "subject previous fna")
        assertTrue(command.subjectGesta == "subject gesta")
        assertTrue(command.subjectBgb3 == "subject bgb3")
    }
}
