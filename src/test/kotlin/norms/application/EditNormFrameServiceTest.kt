package de.bund.digitalservice.ris.norms.application

import de.bund.digitalservice.ris.norms.application.port.input.EditNormFrameUseCase
import de.bund.digitalservice.ris.norms.application.port.output.EditNormOutputPort
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDate
import java.util.UUID

class EditNormFrameServiceTest {
    @Test
    fun `it calls the output port to save the norm with changed title and without optional fields`() {
        val editNormOutputPort = mockk<EditNormOutputPort>()
        val service = EditNormFrameService(editNormOutputPort)
        val guid = UUID.randomUUID()
        val properties = EditNormFrameUseCase.NormFrameProperties("new title")
        val command = EditNormFrameUseCase.Command(guid, properties)

        every { editNormOutputPort.editNorm(any()) } returns Mono.just(true)

        StepVerifier.create(service.editNormFrame(command)).expectNextCount(1).verifyComplete()

        verify(exactly = 1) { editNormOutputPort.editNorm(any()) }
        verify {
            editNormOutputPort.editNorm(
                withArg {
                    assertTrue(it.longTitle == "new title")
                    assertTrue(it.officialShortTitle == null)
                    assertTrue(it.officialAbbreviation == null)
                    assertTrue(it.referenceNumber == null)
                    assertTrue(it.publicationDate == null)
                    assertTrue(it.announcementDate == null)
                    assertTrue(it.citationDate == null)
                    assertTrue(it.frameKeywords == null)
                    assertTrue(it.authorEntity == null)
                    assertTrue(it.authorDecidingBody == null)
                    assertTrue(it.authorIsResolutionMajority == null)
                    assertTrue(it.leadJurisdiction == null)
                    assertTrue(it.leadUnit == null)
                    assertTrue(it.participationType == null)
                    assertTrue(it.participationInstitution == null)
                    assertTrue(it.documentTypeName == null)
                    assertTrue(it.documentNormCategory == null)
                    assertTrue(it.documentTemplateName == null)
                    assertTrue(it.subjectFna == null)
                    assertTrue(it.subjectPreviousFna == null)
                    assertTrue(it.subjectGesta == null)
                    assertTrue(it.subjectBgb3 == null)
                }
            )
        }
    }

    @Test
    fun `it calls the output port to save the norm with changed title and optional fields`() {
        val editNormOutputPort = mockk<EditNormOutputPort>()
        val service = EditNormFrameService(editNormOutputPort)
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
                "author entity",
                "author deciding body",
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
                "subject bgb3",
                "unofficial title",
                "unofficial short title",
                "unofficial abbreviation",
                "ris abbreviation"
            )
        val command = EditNormFrameUseCase.Command(guid, properties)

        every { editNormOutputPort.editNorm(any()) } returns Mono.just(true)

        StepVerifier.create(service.editNormFrame(command)).expectNextCount(1).verifyComplete()

        verify(exactly = 1) { editNormOutputPort.editNorm(any()) }
        verify {
            editNormOutputPort.editNorm(
                withArg {
                    assertTrue(it.longTitle == "long title")
                    assertTrue(it.officialShortTitle == "official short title")
                    assertTrue(it.officialAbbreviation == "official abbreviation")
                    assertTrue(it.referenceNumber == "reference number")
                    assertTrue(it.publicationDate == LocalDate.parse("2020-10-27"))
                    assertTrue(it.announcementDate == LocalDate.parse("2020-10-28"))
                    assertTrue(it.citationDate == LocalDate.parse("2020-10-29"))
                    assertTrue(it.frameKeywords == "frame keywords")
                    assertTrue(it.authorEntity == "author entity")
                    assertTrue(it.authorDecidingBody == "author deciding body")
                    assertTrue(it.authorIsResolutionMajority == true)
                    assertTrue(it.leadJurisdiction == "lead jurisdiction")
                    assertTrue(it.leadUnit == "lead unit")
                    assertTrue(it.participationType == "participation type")
                    assertTrue(it.participationInstitution == "participation institution")
                    assertTrue(it.documentTypeName == "document type name")
                    assertTrue(it.documentNormCategory == "document norm category")
                    assertTrue(it.documentTemplateName == "document template name")
                    assertTrue(it.subjectFna == "subject fna")
                    assertTrue(it.subjectPreviousFna == "subject previous fna")
                    assertTrue(it.subjectGesta == "subject gesta")
                    assertTrue(it.unofficialTitle == "unofficial title")
                    assertTrue(it.unofficialShortTitle == "unofficial short title")
                    assertTrue(it.unofficialAbbreviation == "unofficial abbreviation")
                    assertTrue(it.risAbbreviation == "ris abbreviation")
                }
            )
        }
    }
}
