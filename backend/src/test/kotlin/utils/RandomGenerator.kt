package utils

import de.bund.digitalservice.ris.norms.application.port.input.EditNormFrameUseCase
import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.DocumentSection
import de.bund.digitalservice.ris.norms.domain.entity.Documentation
import de.bund.digitalservice.ris.norms.domain.entity.FileReference
import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import de.bund.digitalservice.ris.norms.domain.value.DocumentSectionType.BOOK
import de.bund.digitalservice.ris.norms.domain.value.DocumentSectionType.PART
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import de.bund.digitalservice.ris.norms.domain.value.NormCategory
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller.EditNormFrameControllerTest
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller.ValidateNormFrameControllerTest
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.schema.MetadataSectionRequestSchema
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.schema.MetadatumRequestSchema
import java.time.LocalDate
import java.util.Random
import java.util.UUID
import org.apache.commons.lang3.RandomStringUtils
import org.jeasy.random.EasyRandom
import org.jeasy.random.EasyRandomParameters
import org.jeasy.random.FieldPredicates.inClass
import org.jeasy.random.FieldPredicates.named

fun createRandomNormFameProperties(): EditNormFrameUseCase.NormFrameProperties {
  val parameters: EasyRandomParameters =
      EasyRandomParameters().randomize(named("metadataSections")) { createSimpleMetadataSections() }
  return EasyRandom(parameters).nextObject(EditNormFrameUseCase.NormFrameProperties::class.java)
}

fun createRandomEditNormRequestTestSchema():
    EditNormFrameControllerTest.NormFramePropertiesTestRequestSchema {
  val parameters: EasyRandomParameters =
      EasyRandomParameters()
          .randomize(named(".+Date\$")) {
            // needed for string date fields
            createRandomLocalDateInString()
          }
          .randomize(named("metadataSections")) { createSimpleMetadataSections() }
          .randomize(named("documentNormCategory")) { NormCategory.values().random().name }
  return EasyRandom(parameters)
      .nextObject(EditNormFrameControllerTest.NormFramePropertiesTestRequestSchema::class.java)
}

fun createMetadatumRequestSchema(
    value: String,
    type: MetadatumType,
    order: Int = 1
): MetadatumRequestSchema {
  val metadatum = MetadatumRequestSchema()
  metadatum.value = value
  metadatum.type = type
  metadatum.order = order
  return metadatum
}

fun createMetadataSectionRequestSchema(
    name: MetadataSectionName,
    metadata: List<MetadatumRequestSchema>
): MetadataSectionRequestSchema {
  val section = MetadataSectionRequestSchema()
  section.name = name
  section.metadata = metadata
  return section
}

fun createValidValidateNormFrameTestRequestSchema():
    ValidateNormFrameControllerTest.ValidateNormFrameTestRequestSchema {

  val m11 = createMetadatumRequestSchema("official long title", MetadatumType.OFFICIAL_LONG_TITLE)
  val s1 = createMetadataSectionRequestSchema(MetadataSectionName.NORM, listOf(m11))

  val m21 = createMetadatumRequestSchema("type name", MetadatumType.TYPE_NAME)
  val m22 =
      createMetadatumRequestSchema(NormCategory.BASE_NORM.toString(), MetadatumType.NORM_CATEGORY)
  val s2 = createMetadataSectionRequestSchema(MetadataSectionName.DOCUMENT_TYPE, listOf(m21, m22))

  val m31 = createMetadatumRequestSchema("entity", MetadatumType.ENTITY)
  val m32 = createMetadatumRequestSchema("deciding body", MetadatumType.DECIDING_BODY)
  val s3 = createMetadataSectionRequestSchema(MetadataSectionName.NORM_PROVIDER, listOf(m31, m32))

  return ValidateNormFrameControllerTest.ValidateNormFrameTestRequestSchema(listOf(s1, s2, s3))
}

fun createRandomNorm(): Norm {
  val parameters: EasyRandomParameters =
      EasyRandomParameters()
          .randomize(named("citationYear")) {
            EasyRandom(EasyRandomParameters().stringLengthRange(4, 4))
                .nextObject(String::class.java)
          }
          .randomize(named("metadataSections")) { emptyList<MetadataSection>() }
          .randomize(named("files")) { emptyList<FileReference>() }
          .randomize(named("documentation")) { emptyList<Documentation>() }
          .excludeField(named("eGesetzgebung"))
  return EasyRandom(parameters).nextObject(Norm::class.java)
}

fun createRandomNormWithCitationDateAndArticles(): Norm {
  return createRandomNorm()
      .copy(
          metadataSections =
              listOf(
                  MetadataSection(
                      MetadataSectionName.CITATION_DATE,
                      listOf(Metadatum(LocalDate.parse("2002-02-02"), MetadatumType.DATE))),
              ),
          documentation =
              listOf(
                  createRandomArticle().copy(paragraphs = listOf(createRandomParagraph())),
                  createRandomArticle().copy(paragraphs = listOf(createRandomParagraph())),
              ),
      )
}

fun createRandomFileReference(): FileReference {
  return EasyRandom().nextObject(FileReference::class.java)
}

fun createRandomArticle(): Article {
  val parameters: EasyRandomParameters =
      EasyRandomParameters().randomize(named("marker").and(inClass(Article::class.java))) {
        "§ " + Random().nextInt(1, 50).toString()
      }
  return EasyRandom(parameters).nextObject(Article::class.java)
}

fun createRandomParagraph(): Paragraph {
  val parameters: EasyRandomParameters =
      EasyRandomParameters().randomize(named("marker").and(inClass(Paragraph::class.java))) {
        "(" + Random().nextInt(1, 50).toString() + ")"
      }
  return EasyRandom(parameters).nextObject(Paragraph::class.java)
}

private fun createRandomLocalDateInString(): String {
  return EasyRandom().nextObject(LocalDate::class.java).toString()
}

fun createSimpleMetadataSections(): List<MetadataSection> =
    listOf(
        MetadataSection(
            MetadataSectionName.NORM,
            listOf(
                Metadatum("foo", MetadatumType.KEYWORD, 0),
                Metadatum("bar", MetadatumType.KEYWORD, 1),
            ),
        ),
    )

fun createSimpleDocumentation(): List<Documentation> =
    listOf(
        DocumentSection(guid = UUID.randomUUID(), order = 1, type = BOOK),
        DocumentSection(
            guid = UUID.randomUUID(),
            order = 2,
            type = BOOK,
            marker = "2",
            heading = "Book 2",
            documentation =
                listOf(
                    DocumentSection(
                        guid = UUID.randomUUID(),
                        order = 1,
                        type = PART,
                        marker = "1",
                        heading = "Part 1",
                        documentation =
                            listOf(
                                Article(
                                    guid = UUID.randomUUID(),
                                    order = 1,
                                    marker = "§ 1",
                                    heading = "Article 1",
                                    paragraphs =
                                        listOf(
                                            Paragraph(
                                                guid = UUID.randomUUID(),
                                                marker = "(1)",
                                                text = "text",
                                            ),
                                        )),
                            )),
                )),
        DocumentSection(
            guid = UUID.randomUUID(), order = 3, type = BOOK, marker = "3", heading = "Book 3"))

fun randomString(length: Int = 10): String = RandomStringUtils.randomAlphabetic(length)
