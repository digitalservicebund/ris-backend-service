package unit.adapter.output.juris

import de.bund.digitalservice.ris.norms.application.port.output.GenerateNormFileOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.ParseJurisXmlOutputPort
import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.AGE_OF_MAJORITY_INDICATION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.ANNOUNCEMENT_GAZETTE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.ARTICLE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.CELEX_NUMBER
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.DATE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.DECIDING_BODY
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.DEFINITION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.DESCRIPTION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.DIVERGENT_DOCUMENT_NUMBER
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.DOCUMENT_CATEGORY
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.ENTITY
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.KEYWORD
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.LEAD_JURISDICTION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.LEAD_UNIT
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.NORM_CATEGORY
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.NOTE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.OFFICIAL_ABBREVIATION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.OFFICIAL_LONG_TITLE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.OFFICIAL_SHORT_TITLE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.PAGE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.PARTICIPATION_INSTITUTION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.PARTICIPATION_TYPE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.RANGE_START
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.REFERENCE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.REFERENCE_NUMBER
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.RESOLUTION_MAJORITY
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.RIS_ABBREVIATION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.RIS_ABBREVIATION_INTERNATIONAL_LAW
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.SUBJECT_FNA
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.SUBJECT_GESTA
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.TEMPLATE_NAME
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.TEXT
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.TYPE_NAME
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.UNDEFINED_DATE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.UNOFFICIAL_ABBREVIATION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.UNOFFICIAL_LONG_TITLE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.UNOFFICIAL_REFERENCE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.UNOFFICIAL_SHORT_TITLE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.VALIDITY_RULE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.WORK_NOTE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.YEAR
import de.bund.digitalservice.ris.norms.domain.value.NormCategory
import de.bund.digitalservice.ris.norms.domain.value.UndefinedDate
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.decodeLocalDate
import de.bund.digitalservice.ris.norms.framework.adapter.output.juris.JurisConverter
import de.bund.digitalservice.ris.norms.framework.adapter.output.juris.mapDomainToData
import de.bund.digitalservice.ris.norms.juris.converter.extractor.extractData
import de.bund.digitalservice.ris.norms.juris.converter.generator.generateZip
import de.bund.digitalservice.ris.norms.juris.converter.model.CategorizedReference
import de.bund.digitalservice.ris.norms.juris.converter.model.DivergentEntryIntoForce
import de.bund.digitalservice.ris.norms.juris.converter.model.DivergentExpiration
import de.bund.digitalservice.ris.norms.juris.converter.model.DocumentStatus
import de.bund.digitalservice.ris.norms.juris.converter.model.DocumentType
import de.bund.digitalservice.ris.norms.juris.converter.model.Lead
import de.bund.digitalservice.ris.norms.juris.converter.model.NormProvider
import de.bund.digitalservice.ris.norms.juris.converter.model.Participation
import de.bund.digitalservice.ris.norms.juris.converter.model.PrintAnnouncement
import de.bund.digitalservice.ris.norms.juris.converter.model.Reissue
import de.bund.digitalservice.ris.norms.juris.converter.model.Status
import de.bund.digitalservice.ris.norms.juris.converter.model.SubjectArea
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
                    normProviderList = listOf(
                        NormProvider("test provider entity", "test provider deciding body", true),
                        NormProvider("DEU", "BT", false),
                    )
                    participationList = listOf(Participation("test participation type", "test participation institution"))
                    leadList = listOf(Lead("test lead jurisdiction", "test lead unit"))
                    subjectAreaList = listOf(SubjectArea("test subject FNA", "test subject Gesta"))
                    documentType = DocumentType("RV", listOf("documentTemplateName"), listOf("ÜN"))
                    officialShortTitle = "test official short title"
                    officialAbbreviation = "test official abbreviation"
                    unofficialLongTitleList = listOf("test unofficial long title")
                    unofficialShortTitleList = listOf("test unofficial short title")
                    unofficialAbbreviationList = listOf("test unofficial abbreviation")
                    entryIntoForceDate = "2022-01-01"
                    entryIntoForceDateState = null
                    principleEntryIntoForceDate = null
                    principleEntryIntoForceDateState = "UNDEFINED_FUTURE"
                    divergentEntryIntoForceList = listOf(DivergentEntryIntoForce("2022-01-03", null, "SN"), DivergentEntryIntoForce(null, "UNDEFINED_NOT_PRESENT", "ÜN"))
                    expirationDate = null
                    expirationDateState = "UNDEFINED_UNKNOWN"
                    principleExpirationDate = "2022-01-05"
                    principleExpirationDateState = null
                    divergentExpirationsList = listOf(DivergentExpiration("2022-01-06", null, "ÄN"), DivergentExpiration(null, "UNDEFINED_UNKNOWN", "SN"))
                    announcementDate = "2022-01-07"
                    citationDateList = listOf("2022-01-08")
                    printAnnouncementList = listOf(PrintAnnouncement("test print announcement year", "test print announcement page", "test print announcement gazette"))
                    unofficialReferenceList = listOf("test unofficial reference")
                    statusList = listOf(Status("test status note", "test status description", "2022-01-09", "test status reference"))
                    reissueList = listOf(Reissue("test reissue note", "test reissue article", "2022-01-11", "test reissue reference"))
                    repealList = listOf("test repeal references 1", "test repeal references 2")
                    otherStatusList = listOf("test other status note")
                    documentStatus = listOf(DocumentStatus(listOf("test document status work note"), "test document status description", "2022"))
                    validityRuleList = listOf("test validity rule")
                    referenceNumberList = listOf("test reference number")
                    celexNumber = "test celex number"
                    definitionList = listOf("test definition")
                    ageOfMajorityIndicationList = listOf("test age of majority indication")
                    text = "test text"
                    ageIndicationStartList = listOf("Lebensjahr 10", "Monate 11")
                    categorizedReferences = listOf(CategorizedReference("test categorized reference 1"), CategorizedReference("test categorized reference 2"))
                }
            val query = ParseJurisXmlOutputPort.Query(anyGuid, anyZipFile.readBytes(), anyZipFile.name)
            every { extractData(any()) } returns data

            val norm = converter.parseJurisXml(query).block()

            val metadata = norm?.metadataSections?.flatMap { it.metadata }

            assertThat(metadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("test official long title", OFFICIAL_LONG_TITLE))
            assertThat(metadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("test ris abbreviation", RIS_ABBREVIATION))
            assertThat(metadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("test document category", DOCUMENT_CATEGORY))
            assertThat(metadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("test official short title", OFFICIAL_SHORT_TITLE))
            assertThat(metadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("test official abbreviation", OFFICIAL_ABBREVIATION))
            assertThat(metadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("test celex number", CELEX_NUMBER))
            assertThat(metadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("test text", TEXT))
            assertThat(metadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("test document number", DIVERGENT_DOCUMENT_NUMBER, 1))
            assertThat(metadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("test ris abbreviation international law", RIS_ABBREVIATION_INTERNATIONAL_LAW, 1))
            assertThat(metadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("test unofficial long title", UNOFFICIAL_LONG_TITLE, 1))
            assertThat(metadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("test unofficial short title", UNOFFICIAL_SHORT_TITLE, 1))
            assertThat(metadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("test unofficial abbreviation", UNOFFICIAL_ABBREVIATION, 1))
            assertThat(metadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("test unofficial reference", UNOFFICIAL_REFERENCE, 1))
            assertThat(metadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("test validity rule", VALIDITY_RULE, 1))
            assertThat(metadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("test reference number", REFERENCE_NUMBER, 1))
            assertThat(metadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("test definition", DEFINITION, 1))
            assertThat(metadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("test age of majority indication", AGE_OF_MAJORITY_INDICATION, 1))
            assertThat(metadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("test participation type", PARTICIPATION_TYPE, 1))
            assertThat(metadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("test participation institution", PARTICIPATION_INSTITUTION, 1))
            assertThat(metadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("test lead jurisdiction", LEAD_JURISDICTION, 1))
            assertThat(metadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("test lead unit", LEAD_UNIT, 1))
            assertThat(metadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("test subject FNA", SUBJECT_FNA, 1))
            assertThat(metadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("test subject Gesta", SUBJECT_GESTA, 1))
            assertThat(metadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum(decodeLocalDate("2022-01-08"), DATE, 1))
            assertThat(metadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("test provider entity", ENTITY, 1))
            assertThat(metadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("test provider deciding body", DECIDING_BODY, 1))
            assertThat(metadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum(true, RESOLUTION_MAJORITY, 1))
            assertThat(metadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("DEU", ENTITY, 1))
            assertThat(metadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("BT", DECIDING_BODY, 1))
            assertThat(metadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum(false, RESOLUTION_MAJORITY, 1))
            assertThat(metadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("Lebensjahr 10", RANGE_START, 1))
            assertThat(metadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("Monate 11", RANGE_START, 1))

            val printAnnouncementMetadata = norm?.metadataSections?.flatMap { it.sections ?: listOf() }?.flatMap { it.metadata }
            assertThat(printAnnouncementMetadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("test print announcement gazette", ANNOUNCEMENT_GAZETTE, 1))
            assertThat(printAnnouncementMetadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("test print announcement year", YEAR, 1))
            assertThat(printAnnouncementMetadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("test print announcement page", PAGE, 1))

            val documentTypeSections = norm?.metadataSections?.filter { it.name == MetadataSectionName.DOCUMENT_TYPE }
            assertThat(documentTypeSections).hasSize(1)
            val documentTypeMetadata = documentTypeSections?.flatMap { it.metadata }
            assertThat(documentTypeMetadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("RV", TYPE_NAME, 1))
            assertThat(documentTypeMetadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum(NormCategory.TRANSITIONAL_NORM, NORM_CATEGORY, 1))
            assertThat(documentTypeMetadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("documentTemplateName", TEMPLATE_NAME, 1))

            val divergentEntryIntoForceParentSections = norm?.metadataSections?.filter { it.name == MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE }
            assertThat(divergentEntryIntoForceParentSections).hasSize(2)
            val divergentEntryIntoForceChildrenSections = divergentEntryIntoForceParentSections?.mapNotNull { it.sections }?.flatten()
            assertThat(divergentEntryIntoForceChildrenSections).hasSize(2)
            val divergentEntryIntoForceDefinedMetadata = divergentEntryIntoForceChildrenSections?.filter { it.name == MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_DEFINED }?.flatMap { it.metadata }
            assertThat(divergentEntryIntoForceDefinedMetadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum(LocalDate.parse("2022-01-03"), DATE, 1))
            assertThat(divergentEntryIntoForceDefinedMetadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum(NormCategory.BASE_NORM, NORM_CATEGORY, 1))
            val divergentEntryIntoForceUndefinedMetadata = divergentEntryIntoForceChildrenSections?.filter { it.name == MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED }?.flatMap { it.metadata }
            assertThat(divergentEntryIntoForceUndefinedMetadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum(UndefinedDate.UNDEFINED_NOT_PRESENT, UNDEFINED_DATE, 1))
            assertThat(divergentEntryIntoForceUndefinedMetadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum(NormCategory.TRANSITIONAL_NORM, NORM_CATEGORY, 1))

            val divergentexpirationParentSections = norm?.metadataSections?.filter { it.name == MetadataSectionName.DIVERGENT_EXPIRATION }
            assertThat(divergentexpirationParentSections).hasSize(2)
            val divergentexpirationChildrenSections = divergentexpirationParentSections?.mapNotNull { it.sections }?.flatten()
            assertThat(divergentexpirationChildrenSections).hasSize(2)
            val divergentexpirationDefinedMetadata = divergentexpirationChildrenSections?.filter { it.name == MetadataSectionName.DIVERGENT_EXPIRATION_DEFINED }?.flatMap { it.metadata }
            assertThat(divergentexpirationDefinedMetadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum(LocalDate.parse("2022-01-06"), DATE, 1))
            assertThat(divergentexpirationDefinedMetadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum(NormCategory.AMENDMENT_NORM, NORM_CATEGORY, 1))
            val divergentexpirationUndefinedMetadata = divergentexpirationChildrenSections?.filter { it.name == MetadataSectionName.DIVERGENT_EXPIRATION_UNDEFINED }?.flatMap { it.metadata }
            assertThat(divergentexpirationUndefinedMetadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum(UndefinedDate.UNDEFINED_UNKNOWN, UNDEFINED_DATE, 1))
            assertThat(divergentexpirationUndefinedMetadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum(NormCategory.BASE_NORM, NORM_CATEGORY, 1))

            val categorizedReferenceSections = norm?.metadataSections?.filter { it.name == MetadataSectionName.CATEGORIZED_REFERENCE }
            assertThat(categorizedReferenceSections).hasSize(2)
            assertThat(categorizedReferenceSections?.get(0)?.metadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("test categorized reference 1", TEXT, 1))
            assertThat(categorizedReferenceSections?.get(1)?.metadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("test categorized reference 2", TEXT, 1))

            val entryIntoForceSection = norm?.metadataSections?.filter { it.name == MetadataSectionName.ENTRY_INTO_FORCE }
            assertThat(entryIntoForceSection).hasSize(1)
            val entryIntoForceMetadata = entryIntoForceSection?.flatMap { it.metadata }
            assertThat(entryIntoForceMetadata).hasSize(1)
            assertThat(entryIntoForceMetadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum(decodeLocalDate(data.entryIntoForceDate), DATE, 1))

            val principleEntryIntoForceSection = norm?.metadataSections?.filter { it.name == MetadataSectionName.PRINCIPLE_ENTRY_INTO_FORCE }
            assertThat(principleEntryIntoForceSection).hasSize(1)
            val principleEntryIntoForceMetadata = principleEntryIntoForceSection?.flatMap { it.metadata }
            assertThat(principleEntryIntoForceMetadata).hasSize(1)
            assertThat(principleEntryIntoForceMetadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum(UndefinedDate.UNDEFINED_FUTURE, UNDEFINED_DATE, 1))

            val expirationSection = norm?.metadataSections?.filter { it.name == MetadataSectionName.EXPIRATION }
            assertThat(expirationSection).hasSize(1)
            val expirationMetadata = expirationSection?.flatMap { it.metadata }
            assertThat(expirationMetadata).hasSize(1)
            assertThat(expirationMetadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum(UndefinedDate.UNDEFINED_UNKNOWN, UNDEFINED_DATE, 1))

            val principleExpirationSection = norm?.metadataSections?.filter { it.name == MetadataSectionName.PRINCIPLE_EXPIRATION }
            assertThat(principleExpirationSection).hasSize(1)
            val principleExpirationMetadata = principleExpirationSection?.flatMap { it.metadata }
            assertThat(principleExpirationMetadata).hasSize(1)
            assertThat(principleExpirationMetadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum(decodeLocalDate(data.principleExpirationDate), DATE, 1))

            val statusIndications = norm?.metadataSections?.filter { it.name == MetadataSectionName.STATUS_INDICATION }
            assertThat(statusIndications).hasSize(5)
            val statusIndicationsChildren = statusIndications?.mapNotNull { it.sections }?.flatten()
            val statusTypeSections = statusIndicationsChildren?.filter { it.name == MetadataSectionName.STATUS }
            assertThat(statusTypeSections).hasSize(1)
            val statusTypeMetadata = statusTypeSections?.get(0)?.metadata
            assertThat(statusTypeMetadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("test status note", NOTE))
            assertThat(statusTypeMetadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("test status description", DESCRIPTION))
            assertThat(statusTypeMetadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum(LocalDate.parse("2022-01-09"), DATE))
            assertThat(statusTypeMetadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("test status reference", REFERENCE))

            val reissueTypeSections = statusIndicationsChildren?.filter { it.name == MetadataSectionName.REISSUE }
            assertThat(reissueTypeSections).hasSize(1)
            val reissueTypeMetadata = reissueTypeSections?.get(0)?.metadata
            assertThat(reissueTypeMetadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("test reissue note", NOTE))
            assertThat(reissueTypeMetadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("test reissue article", ARTICLE))
            assertThat(reissueTypeMetadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum(LocalDate.parse("2022-01-11"), DATE))
            assertThat(reissueTypeMetadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("test reissue reference", REFERENCE))

            val repealTypeSections = statusIndicationsChildren?.filter { it.name == MetadataSectionName.REPEAL }
            assertThat(repealTypeSections).hasSize(2)
            val repealTypeMetadata = repealTypeSections?.map { it.metadata }?.flatten()
            assertThat(repealTypeMetadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("test repeal references 1", TEXT))
            assertThat(repealTypeMetadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("test repeal references 2", TEXT))

            val otherTypeSections = statusIndicationsChildren?.filter { it.name == MetadataSectionName.OTHER_STATUS }
            assertThat(otherTypeSections).hasSize(1)
            val otherTypeMetadata = otherTypeSections?.get(0)?.metadata
            assertThat(otherTypeMetadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("test other status note", NOTE))

            val documentStatusParentSections = norm?.metadataSections?.filter { it.name == MetadataSectionName.DOCUMENT_STATUS_SECTION }
            assertThat(documentStatusParentSections).hasSize(1)
            val documentStatusChildren = documentStatusParentSections?.mapNotNull { it.sections }?.flatten()
            val documentStatusSections = documentStatusChildren?.filter { it.name == MetadataSectionName.DOCUMENT_STATUS }
            assertThat(documentStatusSections).hasSize(1)
            val documentStatusMetadata = documentStatusSections?.get(0)?.metadata
            assertThat(documentStatusMetadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("test document status work note", WORK_NOTE))
            assertThat(documentStatusMetadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("test document status description", DESCRIPTION))
            assertThat(documentStatusMetadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("2022", YEAR))

            val announcementDateSections = norm?.metadataSections?.filter { it.name == MetadataSectionName.ANNOUNCEMENT_DATE }
            assertThat(announcementDateSections).hasSize(1)
            val announcementDateMetadata = announcementDateSections?.get(0)?.metadata
            assertThat(announcementDateMetadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum(LocalDate.parse("2022-01-07"), DATE))
        }

        @Test
        fun `it correctly maps the parsed data to metdata using the index order`() {
            val data = NormData().apply { frameKeywordList = listOf("foo", "bar") }
            val query = ParseJurisXmlOutputPort.Query(anyGuid, anyZipFile.readBytes(), anyZipFile.name)
            val converter = JurisConverter()

            every { extractData(any()) } returns data

            val norm = converter.parseJurisXml(query).block()
            val metadata = norm?.metadataSections?.flatMap { it.metadata }
            assertThat(metadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("foo", KEYWORD, 1))
            assertThat(metadata).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("bar", KEYWORD, 2))
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

            assertThat(norm?.metadataSections?.flatMap { it.metadata }?.first { it.type == OFFICIAL_LONG_TITLE }?.value.toString()).isEqualTo("test official long title")
            assertThat(norm?.metadataSections?.flatMap { it.metadata }).usingRecursiveFieldByFieldElementComparatorIgnoringFields("guid").contains(Metadatum("2022", YEAR, 1))
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

            assertThat(norm?.metadataSections?.flatMap { it.metadata }?.filter { it.type == DATE }).isEmpty()
            assertThat(norm?.metadataSections?.flatMap { it.metadata }?.filter { it.type == YEAR }).isEmpty()
            assertThat(norm?.metadataSections?.flatMap { it.metadata }?.first { it.type == OFFICIAL_LONG_TITLE }?.value.toString()).isEqualTo("test official long title")
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

            assertThat(norm?.metadataSections?.flatMap { it.metadata }?.first { it.type == OFFICIAL_LONG_TITLE }?.value.toString()).isEqualTo("test official long title")
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
