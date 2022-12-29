package de.bund.digitalservice.ris.norms.framework.adapter.output.xml

import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import norms.utils.createRandomNorm
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.xml.sax.InputSource
import reactor.test.StepVerifier
import java.io.File
import java.io.StringReader
import java.io.StringWriter
import java.time.LocalDate
import java.util.*
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory

class ToLegalDocMLConverterTest {
    @Test
    fun `it creates a Akoma Ntoso document with correct schema and version`() {
        val document = convertNormToLegalDocML()
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
        val document = convertNormToLegalDocML()

        val mainElement = document.getElementsByTagName("akn:bill")

        assertThat(mainElement.item(0).parentNode.nodeName).isEqualTo("akn:akomaNtoso")
    }

    @Test
    fun `it creates the preface with the norm its official titles`() {
        val norm =
            createRandomNorm()
                .copy(
                    officialLongTitle = "test official long title",
                    officialShortTitle = "test official short title"
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
        val guid = UUID.randomUUID()
        val norm =
            createRandomNorm()
                .copy(
                    guid = guid,
                    printAnnouncementGazette = "printAnnouncementGazette",
                    publicationDate = LocalDate.parse("2001-01-01"),
                    printAnnouncementPage = "1102"
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
        val document = convertNormToLegalDocML(createRandomNorm())

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
                    documentTypeName = "documentTypeName",
                    documentNormCategory = "documentNormCategory",
                    providerDecidingBody = "providerDecidingBody",
                    participationInstitution = "participationInstitution"
                )
        )

        val metadata = document.getElementsByTagName("meta:legalDocML.de_metadaten").item(0)
        val type = getFirstChildNodeWithTagName(metadata, "meta:typ")
        val category = getFirstChildNodeWithTagName(metadata, "meta:art")
        val initiant = getFirstChildNodeWithTagName(metadata, "meta:initiant")
        val participant = getFirstChildNodeWithTagName(metadata, "meta:bearbeitendeInstitution")

        assertThat(type.textContent.trim()).isEqualTo("gesetz")
        assertThat(category.textContent.trim()).isEqualTo("regelungstext")
        assertThat(initiant.textContent.trim()).isEqualTo("nicht-vorhanden")
        assertThat(participant.textContent.trim()).isEqualTo("nicht-vorhanden")
    }

    @Test
    fun `it creates the metadata tag with default values if null provided`() {
        val document = convertNormToLegalDocML(
            createRandomNorm()
                .copy(
                    documentTypeName = null,
                    documentNormCategory = null,
                    providerDecidingBody = null,
                    participationInstitution = null
                )
        )

        val metadata = document.getElementsByTagName("meta:legalDocML.de_metadaten").item(0)
        val type = getFirstChildNodeWithTagName(metadata, "meta:typ")
        val category = getFirstChildNodeWithTagName(metadata, "meta:art")
        val initiant = getFirstChildNodeWithTagName(metadata, "meta:initiant")
        val participant = getFirstChildNodeWithTagName(metadata, "meta:bearbeitendeInstitution")

        assertThat(type.textContent.trim()).isEqualTo("gesetz")
        assertThat(category.textContent.trim()).isEqualTo("regelungstext")
        assertThat(initiant.textContent.trim()).isEqualTo("nicht-vorhanden")
        assertThat(participant.textContent.trim()).isEqualTo("nicht-vorhanden")
    }

    @Test
    fun `it creates the metadata tag with mapped values if a match found`() {
        val document = convertNormToLegalDocML(
            createRandomNorm()
                .copy(
                    documentTypeName = "SO",
                    documentNormCategory = "Rechtsetzungsdokument",
                    providerDecidingBody = "Präsident des Deutschen Bundestages",
                    participationInstitution = "Bundeskanzleramt"
                )
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
                    providerDecidingBody = "Bundesministerinnen",
                    participationInstitution = "BMinisterium"
                )
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
                paragraphs = listOf(paragraph)
            )
        val norm = createRandomNorm().copy(articles = listOf(article))
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
    fun `it produces valid xml content according to xml schema definition`() {
        val norm = createRandomNorm()
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

private fun convertNormToLegalDocML(norm: Norm? = null): Document {
    val toConvertNorm = norm ?: createRandomNorm()
    val converter = ToLegalDocMLConverter()
    var xmlContent = ""

    converter
        .convertNormToXml(toConvertNorm)
        .`as`(StepVerifier::create)
        .consumeNextWith { xmlContent = it }
        .verifyComplete()

    val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()

    return builder.parse(InputSource(StringReader(xmlContent)))
}

private fun getFirstChildNodeWithTagName(node: Node, tagName: String): Node {
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
