package utils

import de.bund.digitalservice.ris.norms.application.port.input.EditNormFrameUseCase
import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.FileReference
import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller.EditNormFrameController
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.decodeLocalDate
import org.assertj.core.api.Assertions.assertThat
import java.time.LocalDate
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
    normFrameRequestSchema: EditNormFrameController.NormFramePropertiesRequestSchema,
) {
    val normFrameRequestSchemaMembers =
        EditNormFrameController.NormFramePropertiesRequestSchema::class.memberProperties
    val normFramePropertiesMembers = EditNormFrameUseCase.NormFrameProperties::class.memberProperties
    normFramePropertiesMembers.forEach { normFramePropertiesMember ->
        val found =
            normFrameRequestSchemaMembers.find { normFrameRequestSchemaMember ->
                normFramePropertiesMember.name == normFrameRequestSchemaMember.name
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

/**
 * ATTENTION: File references are excluded atm., as they do not work properly,
 * yet. We NEED to fix this!
 */
fun assertNormsAreEqual(norm1: Norm, norm2: Norm) {
    val sortedNorm1 = getNormWithSortedListProperties(norm1).copy(files = emptyList())
    val sortedNorm2 = getNormWithSortedListProperties(norm2).copy(files = emptyList())
    assertThat(sortedNorm1.articles).isEqualTo(sortedNorm2.articles)
}

fun getNormWithSortedListProperties(norm: Norm): Norm {
    val sections = norm.metadataSections.map {
        MetadataSection(it.name, it.metadata.toMutableList().sortedWith(metadatumComparator), it.sections)
    }
    val fileReferences = norm.files.toMutableList().sortedWith(fileReferenceComparator)
    var articles = norm.articles.toMutableList().sortedWith(articleComparator)

    articles = articles.map {
        it.copy(paragraphs = it.paragraphs.toMutableList().sortedWith(paragraphComparator))
    }

    return norm.copy(metadataSections = sections, files = fileReferences, articles = articles)
}

// As there is n natural sorting for our domain classes, the comparators try to
// be exhaustive on the properties as possible. Else it could happen to easily
// with random/stupid test data that the sorting is not definitely.
private val metadatumComparator = compareBy<Metadatum<*>>({ it.type }, { it.order })
private val fileReferenceComparator = compareBy<FileReference>({ it.hash }, { it.name }, { it.createdAt })
private val articleComparator = compareBy<Article>({ it.guid }, { it.marker }, { it.title })
private val paragraphComparator = compareBy<Paragraph>({ it.guid }, { it.marker }, { it.text })
