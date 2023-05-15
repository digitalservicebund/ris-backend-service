package utils

import de.bund.digitalservice.ris.norms.application.port.input.EditNormFrameUseCase
import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.FileReference
import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller.EditNormFrameControllerTest
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.decodeLocalDate
import org.assertj.core.api.Assertions.assertThat
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.comparisons.compareBy
import kotlin.reflect.full.memberProperties

fun assertEditNormFrameProperties(
    commandProperties: EditNormFrameUseCase.NormFrameProperties,
    normFrameProperties: EditNormFrameUseCase.NormFrameProperties,
) {
    EditNormFrameUseCase.NormFrameProperties::class.memberProperties.forEach {
        assertThat(it.get(commandProperties)).isEqualTo(it.get(normFrameProperties))
    }
}

fun assertEditNormFramePropertiesAndEditNormRequestSchema(
    normFrameProperties: EditNormFrameUseCase.NormFrameProperties,
    normFrameRequestSchema: EditNormFrameControllerTest.NormFramePropertiesTestRequestSchema,
) {
    val normFrameRequestSchemaMembers =
        EditNormFrameControllerTest.NormFramePropertiesTestRequestSchema::class.memberProperties
    val normFramePropertiesMembers = EditNormFrameUseCase.NormFrameProperties::class.memberProperties
    normFramePropertiesMembers.forEach { normFramePropertiesMember ->
        val found =
            normFrameRequestSchemaMembers.find { normFrameRequestSchemaMember ->
                normFramePropertiesMember.name == normFrameRequestSchemaMember.name
            }

        if (normFramePropertiesMember.name == "metadataSections") {
            val guidRegex = Regex("guid=[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}")
            assertThat(normFrameProperties.metadataSections.toString().replace(guidRegex, ""))
                .isEqualTo(normFrameRequestSchema.metadataSections.toString().replace(guidRegex, ""))

            return@forEach
        }

        when (val normFramePropertiesMemberValue = normFramePropertiesMember.get(normFrameProperties)) {
            is LocalDate ->
                assertThat(normFramePropertiesMemberValue)
                    .isEqualTo(
                        decodeLocalDate(found?.get(normFrameRequestSchema).toString()),
                    )
            else -> {
                assertThat(normFramePropertiesMemberValue).isEqualTo(found?.get(normFrameRequestSchema))
            }
        }
    }
}

fun assertNormAndEditNormFrameProperties(
    norm: Norm,
    normFrameProperties: EditNormFrameUseCase.NormFrameProperties,
) {
    val normMembers = Norm::class.memberProperties
    val normFramePropertiesMembers = EditNormFrameUseCase.NormFrameProperties::class.memberProperties
    normFramePropertiesMembers.forEach { normFramePropertiesMember ->
        val found = normMembers.find { normMember -> normFramePropertiesMember.name == normMember.name }
        assertThat(normFramePropertiesMember.get(normFrameProperties)).isEqualTo(found?.get(norm))
    }
}

fun assertNormsAreEqual(norm1: Norm, norm2: Norm) {
    val sortedNorm1 = getNormWithSortedListProperties(norm1)
    val sortedNorm2 = getNormWithSortedListProperties(norm2)
    assertThat(sortedNorm1).isEqualTo(sortedNorm2)
}

/**
 * Comparison of FileReference, we need to reduce to milliseconds (don't care about micro or nano seconds) since
 * postgres can only save up to 6 digits (meaning micro seconds) but is automatically rounding up if needed.
 */
fun getNormWithSortedListProperties(norm: Norm): Norm {
    var sections = norm.metadataSections.toMutableList().sortedWith(metadataSectionComparator)
    sections = sections.map {
        it.copy(
            metadata = it.metadata.toMutableList().sortedWith(metadatumComparator),
            sections = it.sections?.toMutableList()?.sortedWith(metadataSectionComparator),
        )
    }
    val fileReferences = norm.files.toMutableList().map { it.copy(createdAt = it.createdAt.truncatedTo(ChronoUnit.MILLIS)) }
        .sortedWith(fileReferenceComparator)

    var articles = norm.articles.toMutableList().sortedWith(articleComparator)
    articles = articles.map {
        it.copy(paragraphs = it.paragraphs.toMutableList().sortedWith(paragraphComparator))
    }

    return norm.copy(metadataSections = sections, files = fileReferences, articles = articles)
}

// As there is n natural sorting for our domain classes, the comparators try to
// be exhaustive on the properties as possible. Else it could happen to easily
// with random/stupid test data that the sorting is not definitely.
private val metadataSectionComparator = compareBy<MetadataSection>({ it.name }, { it.order })
private val metadatumComparator = compareBy<Metadatum<*>>({ it.type }, { it.order })
private val fileReferenceComparator = compareBy<FileReference>({ it.hash }, { it.name }, { it.createdAt })
private val articleComparator = compareBy<Article>({ it.guid }, { it.marker }, { it.title })
private val paragraphComparator = compareBy<Paragraph>({ it.guid }, { it.marker }, { it.text })
