package de.bund.digitalservice.ris.norms.application.port.input

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class EditNormFrameCommandTest {
    @Test
    fun `can create command with GUID and new long title`() {
        val guid = UUID.randomUUID()
        val command = EditNormFrameUseCase.Command(guid, "new title")

        assertThat(command.guid).isEqualTo(guid)
        assertThat(command.longTitle).isEqualTo("new title")
        assertThat(command.officialShortTitle).isEqualTo(null)
        assertThat(command.officialAbbreviation).isEqualTo(null)
        assertThat(command.referenceNumber).isEqualTo(null)
        assertThat(command.publicationDate).isEqualTo(null)
        assertThat(command.announcementDate).isEqualTo(null)
        assertThat(command.citationDate).isEqualTo(null)
        assertThat(command.frameKeywords).isEqualTo(null)
        assertThat(command.authorEntity).isEqualTo(null)
        assertThat(command.authorDecidingBody).isEqualTo(null)
        assertThat(command.authorIsResolutionMajority).isEqualTo(null)
        assertThat(command.leadJurisdiction).isEqualTo(null)
        assertThat(command.leadUnit).isEqualTo(null)
        assertThat(command.participationType).isEqualTo(null)
        assertThat(command.participationInstitution).isEqualTo(null)
        assertThat(command.documentTypeName).isEqualTo(null)
        assertThat(command.documentNormCategory).isEqualTo(null)
        assertThat(command.documentTemplateName).isEqualTo(null)
        assertThat(command.subjectFna).isEqualTo(null)
        assertThat(command.subjectPreviousFna).isEqualTo(null)
        assertThat(command.subjectGesta).isEqualTo(null)
        assertThat(command.subjectBgb3).isEqualTo(null)
    }

    @Test
    fun `can create command with optional fields`() {
        val guid = UUID.randomUUID()
        val command = EditNormFrameUseCase.Command(
            guid, "long title", "official short title", "official abbreviation",
            "reference number", "2020-10-27", "2020-10-28", "2020-10-29",
            "frame keywords", "author entity", "author deciding body",
            true, "lead jurisdiction", "lead unit", "participation type",
            "participation institution", "document type name", "document norm category",
            "document template name", "subject fna", "subject previous fna",
            "subject gesta", "subject bgb3"
        )
        assertThat(command.guid).isEqualTo(guid)
        assertThat(command.longTitle).isEqualTo("long title")
        assertThat(command.officialShortTitle).isEqualTo("official short title")
        assertThat(command.officialAbbreviation).isEqualTo("official abbreviation")
        assertThat(command.referenceNumber).isEqualTo("reference number")
        assertThat(command.publicationDate).isEqualTo("2020-10-27")
        assertThat(command.announcementDate).isEqualTo("2020-10-28")
        assertThat(command.citationDate).isEqualTo("2020-10-29")
        assertThat(command.frameKeywords).isEqualTo("frame keywords")
        assertThat(command.authorEntity).isEqualTo("author entity")
        assertThat(command.authorDecidingBody).isEqualTo("author deciding body")
        assertThat(command.authorIsResolutionMajority).isEqualTo(true)
        assertThat(command.leadJurisdiction).isEqualTo("lead jurisdiction")
        assertThat(command.leadUnit).isEqualTo("lead unit")
        assertThat(command.participationType).isEqualTo("participation type")
        assertThat(command.participationInstitution).isEqualTo("participation institution")
        assertThat(command.documentTypeName).isEqualTo("document type name")
        assertThat(command.documentNormCategory).isEqualTo("document norm category")
        assertThat(command.documentTemplateName).isEqualTo("document template name")
        assertThat(command.subjectFna).isEqualTo("subject fna")
        assertThat(command.subjectPreviousFna).isEqualTo("subject previous fna")
        assertThat(command.subjectGesta).isEqualTo("subject gesta")
        assertThat(command.subjectBgb3).isEqualTo("subject bgb3")
    }
}
