package de.bund.digitalservice.ris.norms.framework.adapter.output

import de.bund.digitalservice.ris.norms.application.port.output.ConvertNormToXmlOutputPort
import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import org.springframework.stereotype.Component
import org.w3c.dom.Document
import org.w3c.dom.Element
import reactor.core.publisher.Mono
import java.io.StringWriter
import java.util.UUID
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

private const val XML_NAMESPACE_AKN_URL = "http://Inhaltsdaten.LegalDocML.de/1.4"
private const val XML_NAMESPACE_XSI_URL = "http://www.w3.org/2001/XMLSchema-instance"

@Component
class ToLegalDocMLConverter : ConvertNormToXmlOutputPort {
    override fun convertNormToXml(norm: Norm): Mono<String> {
        val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val document = documentBuilder.newDocument()

        val body = createBody(document)

        norm.articles.forEach({
            val article = createArticle(document, it)
            body.appendChild(article)
        })

        val metadata = createMetadata(document, norm)
        val preface = createPreface(document, norm)

        val main = createMain(document)
        main.appendChild(metadata)
        main.appendChild(preface)
        main.appendChild(body)

        val root = createAkomaNtosoRoot(document)
        root.appendChild(main)

        document.appendChild(root)

        val xmlContent = convertXmlDocumentToString(document)
        return Mono.just(xmlContent)
    }
}

private fun createAkomaNtosoRoot(document: Document): Element {
    val root = createAknElement(document, "akomaNtoso")
    root.setAttribute("xmlns:akn", XML_NAMESPACE_AKN_URL)
    root.setAttribute("xmlns:xsi", XML_NAMESPACE_XSI_URL)
    return root
}

private fun createMain(document: Document): Element {
    val main = createAknElement(document, "documentCollection")
    main.setAttribute("name", "null")
    return main
}

private fun createMetadata(document: Document, norm: Norm): Element {
    val date = createAknElement(document, "FRBRdate")
    date.setAttribute("date", norm.entryIntoForceDate.toString())

    val author = createAknElement(document, "FRBRauhtor")
    author.setAttribute("date", norm.providerEntity.toString())

    val manifestation = createAknElement(document, "FRBRManifestation")
    manifestation.appendChild(date)
    manifestation.appendChild(author)

    val identification = createAknElement(document, "identification")
    identification.setAttribute("source", "null")
    identification.appendChild(manifestation)

    val metadata = createAknElement(document, "meta")
    metadata.appendChild(identification)

    return metadata
}

private fun createPreface(document: Document, norm: Norm): Element {
    val longTitleP = createIdentifiedAknElement(document, "p")
    longTitleP.setTextContent(norm.officialLongTitle)

    val longTitle = createIdentifiedAknElement(document, "longTitle")
    longTitle.appendChild(longTitleP)

    val shortTitleP = createIdentifiedAknElement(document, "p")
    shortTitleP.setTextContent(norm.officialShortTitle)

    val shortTitle = createIdentifiedAknElement(document, "shortTitle")
    shortTitle.appendChild(shortTitleP)

    val preface = createAknElement(document, "preface")
    preface.appendChild(longTitle)
    preface.appendChild(shortTitle)

    return preface
}

private fun createBody(document: Document): Element {
    val body = createAknElement(document, "body")
    body.setAttribute("refersTo", "null")
    return body
}

private fun createArticle(document: Document, article: Article): Element {
    val heading = createIdentifiedAknElement(document, "heading")
    heading.setTextContent(article.title)

    val number = createIdentifiedAknElement(document, "num")
    number.setTextContent(article.marker)

    val articleElement = createIdentifiedAknElement(document, "article", article.guid)
    articleElement.appendChild(heading)
    articleElement.appendChild(number)

    article.paragraphs.forEach({
        val paragraph = createParagraph(document, it)
        articleElement.appendChild(paragraph)
    })

    return articleElement
}

private fun createParagraph(document: Document, paragraph: Paragraph): Element {
    val number = createIdentifiedAknElement(document, "num")
    number.setTextContent(paragraph.marker)

    val p = createIdentifiedAknElement(document, "p")
    p.setTextContent(paragraph.text)

    val content = createIdentifiedAknElement(document, "content")
    content.appendChild(p)

    val paragraphElement = createIdentifiedAknElement(document, "paragraph", paragraph.guid)
    paragraphElement.appendChild(number)
    paragraphElement.appendChild(content)

    return paragraphElement
}

private fun createAknElement(document: Document, tagName: String) =
    document.createElement("akn:$tagName")

private fun createIdentifiedAknElement(
    document: Document,
    tagName: String,
    guid: UUID? = null,
    eId: String? = ""
): Element {
    val element = createAknElement(document, tagName)
    element.setAttribute("eId", eId.toString())
    element.setAttribute("GUID", guid.toString())
    return element
}

private fun convertXmlDocumentToString(document: Document): String {
    val writer = StringWriter()
    val result = StreamResult(writer)
    val transformer = TransformerFactory.newInstance().newTransformer()
    val source = DOMSource(document)

    transformer.transform(source, result)

    return writer.toString()
}
