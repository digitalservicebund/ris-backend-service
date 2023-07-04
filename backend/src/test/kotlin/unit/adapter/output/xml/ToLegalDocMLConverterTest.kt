package de.bund.digitalservice.ris.norms.framework.adapter.output.xml

import de.bund.digitalservice.ris.norms.application.port.output.ConvertNormToXmlOutputPort
import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import de.bund.digitalservice.ris.norms.domain.value.NormCategory
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.decodeLocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.xml.sax.InputSource
import reactor.test.StepVerifier
import utils.createRandomNorm
import utils.createRandomNormWithCitationDateAndArticles
import java.io.File
import java.io.StringReader
import java.io.StringWriter
import java.util.*
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory
import kotlin.collections.ArrayList

class ToLegalDocMLConverterTest {
    @Test
    fun `it creates a Akoma Ntoso document with correct schema and version`() {
        val document = convertNormToLegalDocML(createRandomNormWithCitationDateAndArticles())
        assertThat(document.getElementsByTagName("akn:akomaNtoso").length).isEqualTo(1)
        assertThat(document.firstChild.nodeName).isEqualTo("akn:akomaNtoso")

        val attributes = document.firstChild.attributes

        assertThat(attributes.getNamedItem("xmlns:akn").nodeValue)
            .isEqualTo("http://Inhaltsdaten.LegalDocML.de/1.4/")
        assertThat(attributes.getNamedItem("xmlns:xsi").nodeValue)
            .isEqualTo("http://www.w3.org/2001/XMLSchema-instance")
    }

    @Test
    fun `it adds the correct main element with name`() {
        val document = convertNormToLegalDocML(createRandomNormWithCitationDateAndArticles())

        val mainElement = document.getElementsByTagName("akn:bill")

        assertThat(mainElement.item(0).parentNode.nodeName).isEqualTo("akn:akomaNtoso")
    }

    @Test
    fun `it creates the preface with the norm its official titles`() {
        val norm =
            createRandomNormWithCitationDateAndArticles()
                .copy(
                    metadataSections = listOf(
                        MetadataSection(
                            name = MetadataSectionName.NORM,
                            metadata = listOf(
                                Metadatum(
                                    type = MetadatumType.OFFICIAL_SHORT_TITLE,
                                    value = "test official short title",
                                ),
                                Metadatum(
                                    type = MetadatumType.OFFICIAL_LONG_TITLE,
                                    value = "test official long title",
                                ),
                            ),
                        ),
                    ),
                )
        val document = convertNormToLegalDocML(norm)

        val preface = document.getElementsByTagName("akn:preface").item(0)
        val longTitle = getFirstChildNodeWithTagName(preface, "akn:longTitle")
        val longTitleP = getFirstChildNodeWithTagName(longTitle, "akn:p")
        val shortTitle = getFirstChildNodeWithTagName(longTitleP, "akn:shortTitle")
        val docTitle = getFirstChildNodeWithTagName(longTitleP, "akn:docTitle")

        assertThat(docTitle.textContent.trim()).isEqualTo("test official long title")
        assertThat(shortTitle.textContent.trim()).isEqualTo("test official short title")
    }

    @Test
    fun `it creates the identification tag with proper data`() {
        val printAnnouncementSection = MetadataSection(
            MetadataSectionName.OFFICIAL_REFERENCE,
            listOf(),
            1,
            listOf(
                MetadataSection(
                    MetadataSectionName.PRINT_ANNOUNCEMENT,
                    listOf(
                        Metadatum("printAnnouncementGazette", MetadatumType.ANNOUNCEMENT_GAZETTE, 1),
                        Metadatum("1102", MetadatumType.PAGE, 1),
                    ),
                ),
            ),
        )

        val announcmentDateSection = MetadataSection(
            MetadataSectionName.ANNOUNCEMENT_DATE,
            listOf(Metadatum(decodeLocalDate("2001-01-01"), MetadatumType.DATE)),
            2,
            listOf(),

        )
        val guid = UUID.randomUUID()
        val norm =
            createRandomNormWithCitationDateAndArticles()
                .copy(
                    guid = guid,
                    metadataSections = listOf(printAnnouncementSection, announcmentDateSection),
                )
        val document = convertNormToLegalDocML(norm)

        val identification = document.getElementsByTagName("akn:identification").item(0)
        val work = getFirstChildNodeWithTagName(identification, "akn:FRBRWork")
        val manifestation = getFirstChildNodeWithTagName(identification, "akn:FRBRManifestation")
        val expression = getFirstChildNodeWithTagName(identification, "akn:FRBRExpression")
        // Work
        val workThis = getFirstChildNodeWithTagName(work, "akn:FRBRthis")
        val workUri = getFirstChildNodeWithTagName(work, "akn:FRBRuri")
        val workDate = getFirstChildNodeWithTagName(work, "akn:FRBRdate")
        val workAuthor = getFirstChildNodeWithTagName(work, "akn:FRBRauthor")
        val workAlias = getFirstChildNodeWithTagName(work, "akn:FRBRalias")
        val workCountry = getFirstChildNodeWithTagName(work, "akn:FRBRcountry")
        val workNumber = getFirstChildNodeWithTagName(work, "akn:FRBRnumber")
        val workName = getFirstChildNodeWithTagName(work, "akn:FRBRname")
        val workSubtype = getFirstChildNodeWithTagName(work, "akn:FRBRsubtype")
        // Manifestation
        val manifestationThis = getFirstChildNodeWithTagName(manifestation, "akn:FRBRthis")
        val manifestationUri = getFirstChildNodeWithTagName(manifestation, "akn:FRBRuri")
        val manifestationDate = getFirstChildNodeWithTagName(manifestation, "akn:FRBRdate")
        val manifestationAuthor = getFirstChildNodeWithTagName(manifestation, "akn:FRBRauthor")
        // Expression
        val expressionThis = getFirstChildNodeWithTagName(expression, "akn:FRBRthis")
        val expressionUri = getFirstChildNodeWithTagName(expression, "akn:FRBRuri")
        val expressionDate = getFirstChildNodeWithTagName(expression, "akn:FRBRdate")
        val expressionAuthor = getFirstChildNodeWithTagName(expression, "akn:FRBRauthor")

        // Assert Work
        assertThat(workThis.attributes.getNamedItem("value").nodeValue).isEqualTo("eli/printAnnouncementGazette/2001/s1102/regelungstext-1")
        assertThat(workUri.attributes.getNamedItem("value").nodeValue).isEqualTo("eli/printAnnouncementGazette/2001/s1102")
        assertThat(workAlias.attributes.getNamedItem("value").nodeValue).isEqualTo("$guid")
        assertThat(workDate.attributes.getNamedItem("date").nodeValue).isEqualTo("2001-01-01")
        assertThat(workAuthor.attributes.getNamedItem("href").nodeValue).isEqualTo("recht.bund.de/institution/bundesregierung")
        assertThat(workCountry.attributes.getNamedItem("value").nodeValue).isEqualTo("de")
        assertThat(workNumber.attributes.getNamedItem("value").nodeValue).isEqualTo("s1102")
        assertThat(workName.attributes.getNamedItem("value").nodeValue).isEqualTo("printAnnouncementGazette")
        assertThat(workSubtype.attributes.getNamedItem("value").nodeValue).isEqualTo("regelungstext-1")
        // AssertManifestation
        assertThat(manifestationThis.attributes.getNamedItem("value").nodeValue).isEqualTo("eli/printAnnouncementGazette/2001/s1102/2001-01-01/1/deu/regelungstext-1.xml")
        assertThat(manifestationUri.attributes.getNamedItem("value").nodeValue).isEqualTo("eli/printAnnouncementGazette/2001/s1102/2001-01-01/1/deu/regelungstext-1.xml")
        assertThat(manifestationDate.attributes.getNamedItem("date").nodeValue).isEqualTo("2001-01-01")
        assertThat(manifestationAuthor.attributes.getNamedItem("href").nodeValue).isEqualTo("recht.bund.de")
        // AssertExpression
        assertThat(expressionThis.attributes.getNamedItem("value").nodeValue).isEqualTo("eli/printAnnouncementGazette/2001/s1102/2001-01-01/1/deu/regelungstext-1")
        assertThat(expressionUri.attributes.getNamedItem("value").nodeValue).isEqualTo("eli/printAnnouncementGazette/2001/s1102/2001-01-01/1/deu")
        assertThat(expressionDate.attributes.getNamedItem("date").nodeValue).isEqualTo("2001-01-01")
        assertThat(expressionAuthor.attributes.getNamedItem("href").nodeValue).isEqualTo("recht.bund.de/institution/bundesregierung")
    }

    @Test
    fun `it creates the constant metadata tags `() {
        val document = convertNormToLegalDocML(createRandomNormWithCitationDateAndArticles())

        val metadata = document.getElementsByTagName("meta:legalDocML.de_metadaten").item(0)
        val form = getFirstChildNodeWithTagName(metadata, "meta:form")
        val summary = getFirstChildNodeWithTagName(metadata, "meta:fassung")

        assertThat(form.textContent.trim()).isEqualTo("stammform")
        assertThat(summary.textContent.trim()).isEqualTo("verkuendungsfassung")
    }

    @Test
    fun `it creates the metadata tag with default values if no match found`() {
        val document = convertNormToLegalDocML(
            createRandomNorm()
                .copy(
                    metadataSections = listOf(
                        MetadataSection(MetadataSectionName.PARTICIPATION, listOf(Metadatum("participationInstitution", MetadatumType.PARTICIPATION_INSTITUTION))),
                        MetadataSection(MetadataSectionName.CITATION_DATE, listOf(Metadatum(decodeLocalDate("2002-02-02"), MetadatumType.DATE))),
                        MetadataSection(MetadataSectionName.NORM_PROVIDER, listOf(Metadatum("providerDecidingBody", MetadatumType.DECIDING_BODY))),
                        MetadataSection(
                            MetadataSectionName.DOCUMENT_TYPE,
                            listOf(
                                Metadatum("documentTypeName", MetadatumType.TYPE_NAME),
                                Metadatum(NormCategory.BASE_NORM, MetadatumType.NORM_CATEGORY),
                            ),
                        ),
                    ),
                ),
        )

        val metadata = document.getElementsByTagName("meta:legalDocML.de_metadaten").item(0)
        val type = getFirstChildNodeWithTagName(metadata, "meta:typ")
        val category = getFirstChildNodeWithTagName(metadata, "meta:art")
        val initiant = getFirstChildNodeWithTagName(metadata, "meta:initiant")
        val participant = getFirstChildNodeWithTagName(metadata, "meta:bearbeitendeInstitution")

        assertThat(type.textContent.trim()).isEqualTo("gesetz")
        assertThat(category.textContent.trim()).isEqualTo("rechtsetzungsdokument")
        assertThat(initiant.textContent.trim()).isEqualTo("nicht-vorhanden")
        assertThat(participant.textContent.trim()).isEqualTo("nicht-vorhanden")
    }

    @Test
    fun `it creates the metadata tag with default values if null provided`() {
        val document = convertNormToLegalDocML(createRandomNormWithCitationDateAndArticles())

        val metadata = document.getElementsByTagName("meta:legalDocML.de_metadaten").item(0)
        val type = getFirstChildNodeWithTagName(metadata, "meta:typ")
        val category = getFirstChildNodeWithTagName(metadata, "meta:art")
        val initiant = getFirstChildNodeWithTagName(metadata, "meta:initiant")
        val participant = getFirstChildNodeWithTagName(metadata, "meta:bearbeitendeInstitution")

        assertThat(type.textContent.trim()).isEqualTo("gesetz")
        assertThat(category.textContent.trim()).isEqualTo("rechtsetzungsdokument")
        assertThat(initiant.textContent.trim()).isEqualTo("nicht-vorhanden")
        assertThat(participant.textContent.trim()).isEqualTo("nicht-vorhanden")
    }

    @Test
    fun `it creates the metadata tag with mapped values if a match found`() {
        val document = convertNormToLegalDocML(
            createRandomNorm()
                .copy(
                    metadataSections = listOf(
                        MetadataSection(MetadataSectionName.PARTICIPATION, listOf(Metadatum("Bundeskanzleramt", MetadatumType.PARTICIPATION_INSTITUTION))),
                        MetadataSection(MetadataSectionName.CITATION_DATE, listOf(Metadatum(decodeLocalDate("2002-02-02"), MetadatumType.DATE))),
                        MetadataSection(MetadataSectionName.NORM_PROVIDER, listOf(Metadatum("Präsident des Deutschen Bundestages", MetadatumType.DECIDING_BODY))),
                        MetadataSection(
                            MetadataSectionName.DOCUMENT_TYPE,
                            listOf(
                                Metadatum("SO", MetadatumType.TYPE_NAME),
                                Metadatum(NormCategory.AMENDMENT_NORM, MetadatumType.NORM_CATEGORY),
                            ),
                        ),
                    ),
                ),

        )

        val metadata = document.getElementsByTagName("meta:legalDocML.de_metadaten").item(0)
        val type = getFirstChildNodeWithTagName(metadata, "meta:typ")
        val category = getFirstChildNodeWithTagName(metadata, "meta:art")
        val initiant = getFirstChildNodeWithTagName(metadata, "meta:initiant")
        val participant = getFirstChildNodeWithTagName(metadata, "meta:bearbeitendeInstitution")

        assertThat(type.textContent.trim()).isEqualTo("sonstige-bekanntmachung")
        assertThat(category.textContent.trim()).isEqualTo("rechtsetzungsdokument")
        assertThat(initiant.textContent.trim()).isEqualTo("bundestag")
        assertThat(participant.textContent.trim()).isEqualTo("bundesregierung")
    }

    @Test
    fun `it creates the metadata tag with mapped values if a partial match found`() {
        val document = convertNormToLegalDocML(
            createRandomNorm()
                .copy(
                    metadataSections = listOf(
                        MetadataSection(MetadataSectionName.PARTICIPATION, listOf(Metadatum("BMinisterium", MetadatumType.PARTICIPATION_INSTITUTION))),
                        MetadataSection(MetadataSectionName.CITATION_DATE, listOf(Metadatum(decodeLocalDate("2002-02-02"), MetadatumType.DATE))),
                        MetadataSection(MetadataSectionName.NORM_PROVIDER, listOf(Metadatum("Bundesministerinnen", MetadatumType.DECIDING_BODY))),
                    ),
                ),
        )

        val metadata = document.getElementsByTagName("meta:legalDocML.de_metadaten").item(0)
        val initiant = getFirstChildNodeWithTagName(metadata, "meta:initiant")
        val participant = getFirstChildNodeWithTagName(metadata, "meta:bearbeitendeInstitution")

        assertThat(initiant.textContent.trim()).isEqualTo("bundesregierung")
        assertThat(participant.textContent.trim()).isEqualTo("bundesregierung")
    }

    @Test
    fun `it creates and article element with its paragraph and content`() {
        val paragraph =
            Paragraph(UUID.randomUUID(), marker = "§ 1.1", text = "test paragraph text")
        val article =
            Article(
                UUID.randomUUID(),
                title = "test article title",
                marker = "§ 9a",
                paragraphs = listOf(paragraph),
            )
        val norm = createRandomNormWithCitationDateAndArticles().copy(
            articles = listOf(article),
        )
        val document = convertNormToLegalDocML(norm)

        val body = document.getElementsByTagName("akn:body").item(0)
        val articleElement = getFirstChildNodeWithTagName(body, "akn:article")
        val articleHeading = getFirstChildNodeWithTagName(articleElement, "akn:heading")
        val articleNumber = getFirstChildNodeWithTagName(articleElement, "akn:num")
        val articleMarker = getFirstChildNodeWithTagName(articleNumber, "akn:marker")
        val paragraphElement = getFirstChildNodeWithTagName(articleElement, "akn:paragraph")
        val paragraphNumber = getFirstChildNodeWithTagName(paragraphElement, "akn:num")
        val paragraphMarker = getFirstChildNodeWithTagName(paragraphNumber, "akn:marker")
        val paragraphContent = getFirstChildNodeWithTagName(paragraphElement, "akn:content")
        val paragraphContentP = getFirstChildNodeWithTagName(paragraphContent, "akn:p")

        assertThat(articleElement.attributes.getNamedItem("eId").nodeValue).isEqualTo("para-9a")
        assertThat(articleNumber.attributes.getNamedItem("eId").nodeValue).isEqualTo("para-9a_bezeichnung-1")
        assertThat(articleNumber.textContent.trim()).isEqualTo("§ 9a")
        assertThat(articleMarker.attributes.getNamedItem("name").nodeValue).isEqualTo("9a")
        assertThat(articleHeading.attributes.getNamedItem("eId").nodeValue).isEqualTo("para-9a_überschrift-1")
        assertThat(articleHeading.textContent.trim()).isEqualTo("test article title")
        assertThat(paragraphElement.attributes.getNamedItem("eId").nodeValue).isEqualTo("para-9a_abs-1.1")
        assertThat(paragraphNumber.attributes.getNamedItem("eId").nodeValue).isEqualTo("para-9a_abs-1.1_bezeichnung-1")
        assertThat(paragraphNumber.textContent.trim()).isEqualTo("§ 1.1")
        assertThat(paragraphMarker.attributes.getNamedItem("name").nodeValue).isEqualTo("1.1")
        assertThat(paragraphContent.attributes.getNamedItem("eId").nodeValue).isEqualTo("para-9a_abs-1.1_inhalt-1")
        assertThat(paragraphContentP.attributes.getNamedItem("eId").nodeValue).isEqualTo("para-9a_abs-1.1_inhalt-1_text-1")
        assertThat(paragraphContentP.textContent.trim()).isEqualTo("test paragraph text")
    }

    @Test
    fun `it creates and article element with its paragraph and content containing a not nested list`() {
        val paragraph =
            Paragraph(
                UUID.randomUUID(),
                marker = "(1)",
                text = "test list intro text:\n" +
                    "                    <DL Type=\"arabic\">\n" +
                    "                            <DT>1.</DT>\n" +
                    "                        <DD Font=\"normal\">\n" +
                    "                            <LA>test first point\n" +
                    "                            </LA>\n" +
                    "                        </DD>\n" +
                    "                        <DT>2.</DT>\n" +
                    "                        <DD Font=\"normal\">\n" +
                    "                            <LA>test second point\n" +
                    "                            </LA>\n" +
                    "                        </DD>\n" +
                    "                    </DL>\n" +
                    "                ",
            )
        val article =
            Article(
                UUID.randomUUID(),
                title = "test article title",
                marker = "§ 1",
                paragraphs = listOf(paragraph),
            )
        val norm = createRandomNormWithCitationDateAndArticles().copy(
            articles = listOf(article),
        )
        val document = convertNormToLegalDocML(norm)

        val body = document.getElementsByTagName("akn:body").item(0)
        val articleElement = getFirstChildNodeWithTagName(body, "akn:article")
        val paragraphElement = getFirstChildNodeWithTagName(articleElement, "akn:paragraph")
        val paragraphList = getFirstChildNodeWithTagName(paragraphElement, "akn:list")
        val paragraphListIntro = getFirstChildNodeWithTagName(paragraphList, "akn:intro")
        val paragraphListIntroP = getFirstChildNodeWithTagName(paragraphListIntro, "akn:p")
        val paragraphListPoints = getChildrenWithTagName(paragraphList, "akn:point")

        val listFirstPointNum = getFirstChildNodeWithTagName(paragraphListPoints[0], "akn:num")
        val listFirstPointMarker = getFirstChildNodeWithTagName(listFirstPointNum, "akn:marker")
        val listFirstPointContent = getFirstChildNodeWithTagName(paragraphListPoints[0], "akn:content")
        val listFirstPointContentP = getFirstChildNodeWithTagName(listFirstPointContent, "akn:p")

        val listSecondPointNum = getFirstChildNodeWithTagName(paragraphListPoints[1], "akn:num")
        val listSecondPointMarker = getFirstChildNodeWithTagName(listSecondPointNum, "akn:marker")
        val listSecondPointContent = getFirstChildNodeWithTagName(paragraphListPoints[1], "akn:content")
        val listSecondPointContentP = getFirstChildNodeWithTagName(listSecondPointContent, "akn:p")

        assertThat(paragraphListIntroP.textContent.trim()).isEqualTo("test list intro text:")
        assertThat(listFirstPointNum.textContent.trim()).isEqualTo("1.")
        assertThat(listFirstPointMarker.attributes.getNamedItem("name").nodeValue).isEqualTo("1")
        assertThat(listFirstPointContentP.textContent.trim()).isEqualTo("test first point")
        assertThat(listSecondPointNum.textContent.trim()).isEqualTo("2.")
        assertThat(listSecondPointMarker.attributes.getNamedItem("name").nodeValue).isEqualTo("2")
        assertThat(listSecondPointContentP.textContent.trim()).isEqualTo("test second point")
    }

    @Test
    fun `it creates and article element with its paragraph and content containing a nested list`() {
        val paragraph =
            Paragraph(
                UUID.randomUUID(),
                marker = "(1)",
                text =
                "test list intro text:\n" +
                    "                    <DL Font=\"normal\" Type=\"arabic\">\n" +
                    "                        <DT>1.</DT>\n" +
                    "                        <DD Font=\"normal\">\n" +
                    "                            <LA Size=\"normal\">test 1. point text\n" +
                    "                                <DL Font=\"normal\" Type=\"alpha\">\n" +
                    "                                    <DT>a)</DT>\n" +
                    "                                    <DD Font=\"normal\">\n" +
                    "                                        <LA Size=\"normal\">test a) point text\n" +
                    "                                        </LA>\n" +
                    "                                    </DD>\n" +
                    "                                    <DT>b)</DT>\n" +
                    "                                    <DD Font=\"normal\">\n" +
                    "                                        <LA Size=\"normal\">test b) point text\n" +
                    "                                            <DL Font=\"normal\" Type=\"a-alpha\">\n" +
                    "                                                <DT>aa)</DT>\n" +
                    "                                                <DD Font=\"normal\">\n" +
                    "                                                    <LA Size=\"normal\">test aa) point text\n" +
                    "                                                    </LA>\n" +
                    "                                                </DD>\n" +
                    "                                                <DT>bb)</DT>\n" +
                    "                                                <DD Font=\"normal\">\n" +
                    "                                                    <LA Size=\"normal\">test bb) point text\n" +
                    "                                                    </LA>\n" +
                    "                                                </DD>\n" +
                    "                                            </DL>\n" +
                    "                                        </LA>\n" +
                    "                                    </DD>\n" +
                    "                                </DL>\n" +
                    "                            </LA>\n" +
                    "                        </DD>\n" +
                    "                    </DL>",
            )
        val article =
            Article(
                UUID.randomUUID(),
                title = "test article title",
                marker = "§ 1",
                paragraphs = listOf(paragraph),
            )
        val norm = createRandomNormWithCitationDateAndArticles().copy(
            articles = listOf(article),
        )
        val document = convertNormToLegalDocML(norm)

        val body = document.getElementsByTagName("akn:body").item(0)
        val articleElement = getFirstChildNodeWithTagName(body, "akn:article")
        val paragraphElement = getFirstChildNodeWithTagName(articleElement, "akn:paragraph")

        // 1. Nest level
        val paragraphList = getFirstChildNodeWithTagName(paragraphElement, "akn:list")
        val paragraphListIntro = getFirstChildNodeWithTagName(paragraphList, "akn:intro")
        val paragraphListIntroP = getFirstChildNodeWithTagName(paragraphListIntro, "akn:p")
        val paragraphListPoints = getChildrenWithTagName(paragraphList, "akn:point")
        val firstPointNum = getFirstChildNodeWithTagName(paragraphListPoints[0], "akn:num")
        val firstPointMarker = getFirstChildNodeWithTagName(firstPointNum, "akn:marker")

        assertThat(paragraphListIntroP.textContent.trim()).isEqualTo("test list intro text:")
        assertThat(paragraphListPoints.size).isEqualTo(1)
        assertThat(firstPointNum.textContent.trim()).isEqualTo("1.")
        assertThat(firstPointMarker.attributes.getNamedItem("name").nodeValue).isEqualTo("1")

        // 2. Nest level
        val firstPointList = getFirstChildNodeWithTagName(paragraphListPoints[0], "akn:list")
        val firstPointListIntro = getFirstChildNodeWithTagName(firstPointList, "akn:intro")
        val firstPointListIntroP = getFirstChildNodeWithTagName(firstPointListIntro, "akn:p")
        val firstPointListPoints = getChildrenWithTagName(firstPointList, "akn:point")
        val firstPoint2ndLevelNum = getFirstChildNodeWithTagName(firstPointListPoints[0], "akn:num")
        val firstPoint2ndLevelMarker = getFirstChildNodeWithTagName(firstPoint2ndLevelNum, "akn:marker")
        val firstPoint2ndLevelContent = getFirstChildNodeWithTagName(firstPointListPoints[0], "akn:content")
        val firstPoint2ndLevelContentP = getFirstChildNodeWithTagName(firstPoint2ndLevelContent, "akn:p")
        val secondPoint2ndLevelNum = getFirstChildNodeWithTagName(firstPointListPoints[1], "akn:num")
        val secondPoint2ndLevelMarker = getFirstChildNodeWithTagName(secondPoint2ndLevelNum, "akn:marker")

        assertThat(firstPointListIntroP.textContent.trim()).isEqualTo("test 1. point text")
        assertThat(firstPointListPoints.size).isEqualTo(2)
        assertThat(firstPoint2ndLevelNum.textContent.trim()).isEqualTo("a)")
        assertThat(firstPoint2ndLevelMarker.attributes.getNamedItem("name").nodeValue).isEqualTo("a")
        assertThat(firstPoint2ndLevelContentP.textContent.trim()).isEqualTo("test a) point text")
        assertThat(secondPoint2ndLevelNum.textContent.trim()).isEqualTo("b)")
        assertThat(secondPoint2ndLevelMarker.attributes.getNamedItem("name").nodeValue).isEqualTo("b")

        // 3. Nest level
        val secondPoint2ndLevelList = getFirstChildNodeWithTagName(firstPointListPoints[1], "akn:list")
        val secondPoint2ndLevelListIntro = getFirstChildNodeWithTagName(secondPoint2ndLevelList, "akn:intro")
        val secondPoint2ndLevelListIntroP = getFirstChildNodeWithTagName(secondPoint2ndLevelListIntro, "akn:p")
        val secondPoint2ndLevelPoints = getChildrenWithTagName(secondPoint2ndLevelList, "akn:point")
        val firstPoint3ndLevelNum = getFirstChildNodeWithTagName(secondPoint2ndLevelPoints[0], "akn:num")
        val firstPoint3ndLevelMarker = getFirstChildNodeWithTagName(firstPoint3ndLevelNum, "akn:marker")
        val firstPoint3ndLevelContent = getFirstChildNodeWithTagName(secondPoint2ndLevelPoints[0], "akn:content")
        val firstPoint3ndLevelContentP = getFirstChildNodeWithTagName(firstPoint3ndLevelContent, "akn:p")
        val secondPoint3ndLevelNum = getFirstChildNodeWithTagName(secondPoint2ndLevelPoints[1], "akn:num")
        val secondPoint3ndLevelMarker = getFirstChildNodeWithTagName(secondPoint3ndLevelNum, "akn:marker")
        val secondPoint3ndLevelContent = getFirstChildNodeWithTagName(secondPoint2ndLevelPoints[1], "akn:content")
        val secondPoint3ndLevelContentP = getFirstChildNodeWithTagName(secondPoint3ndLevelContent, "akn:p")

        assertThat(secondPoint2ndLevelListIntroP.textContent.trim()).isEqualTo("test b) point text")
        assertThat(secondPoint2ndLevelPoints.size).isEqualTo(2)
        assertThat(firstPoint3ndLevelNum.textContent.trim()).isEqualTo("aa)")
        assertThat(firstPoint3ndLevelMarker.attributes.getNamedItem("name").nodeValue).isEqualTo("aa")
        assertThat(firstPoint3ndLevelContentP.textContent.trim()).isEqualTo("test aa) point text")
        assertThat(secondPoint3ndLevelNum.textContent.trim()).isEqualTo("bb)")
        assertThat(secondPoint3ndLevelMarker.attributes.getNamedItem("name").nodeValue).isEqualTo("bb")
        assertThat(secondPoint3ndLevelContentP.textContent.trim()).isEqualTo("test bb) point text")
    }

    @Test
    fun `it produces valid xml content according to xml schema definition`() {
        val norm = createRandomNormWithCitationDateAndArticles()
        val document = convertNormToLegalDocML(norm)
        val domSource = DOMSource(document)
        val writer = StringWriter()
        val result = StreamResult(writer)
        val tf = TransformerFactory.newInstance()
        val transformer: Transformer = tf.newTransformer()
        transformer.transform(domSource, result)
        val schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
        val schema =
            schemaFactory.newSchema(File("src/main/resources/legaldocml/schemas/legalDocML.de-regelungstextentwurfsfassung.xsd"))
        val validator = schema.newValidator()
        val source = StreamSource(StringReader(writer.toString()))

        val isValid = try {
            validator.validate(source)
            true
        } catch (e: Exception) {
            false
        }

        assertThat(isValid).isTrue
    }
}

fun convertNormToLegalDocML(norm: Norm? = null): Document {
    val toConvertNorm = norm ?: createRandomNorm()
    val converter = ToLegalDocMLConverter()
    val command = ConvertNormToXmlOutputPort.Command(toConvertNorm)
    var xmlContent = ""

    converter
        .convertNormToXml(command)
        .`as`(StepVerifier::create)
        .consumeNextWith { xmlContent = it }
        .verifyComplete()

    val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()

    return builder.parse(InputSource(StringReader(xmlContent)))
}

fun getFirstChildNodeWithTagName(node: Node, tagName: String): Node {
    if (!node.hasChildNodes()) {
        throw Exception("Node has no children!")
    }

    val childNodes = node.childNodes

    for (i in 0 until childNodes.length) {
        val child = childNodes.item(i)

        if (child.nodeName == tagName) {
            return child
        }
    }

    throw Exception("No child node found for $tagName!")
}

private fun getChildrenWithTagName(node: Node, tagName: String): ArrayList<Node> {
    if (!node.hasChildNodes()) {
        throw Exception("Node has no children!")
    }

    val childNodes = node.childNodes
    val nodeList = ArrayList<Node>()
    for (i in 0 until childNodes.length) {
        val child = childNodes.item(i)

        if (child.nodeName == tagName) {
            nodeList.add(child)
        }
    }
    if (nodeList.size > 0) {
        return nodeList
    }
    throw Exception("No child nodes found for $tagName!")
}
