package utils

import de.bund.digitalservice.ris.norms.application.port.input.EditNormFrameUseCase
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller.EditNormFrameController
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.decodeLocalDate
import org.assertj.core.api.Assertions.assertThat
import java.time.LocalDate
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

fun assertNormsAreEqual(norm1: Norm, norm2: Norm) {
    val members = Norm::class.memberProperties
    val supportedMembers = members.filter { it.name != "files" }

    supportedMembers.forEach { member ->
        val norm1Value = member(norm1)
        val norm2Value = member(norm2)

        when (norm1Value) {
            is Iterable<*> ->
                assertThat(norm1Value.toSet()).isEqualTo((norm2Value as Iterable<*>).toSet())

            is LocalDate ->
                assertThat(norm1Value).isEqualTo(decodeLocalDate(norm2Value.toString()))

            else ->
                assertThat(norm1Value).isEqualTo(norm2Value)
        }
    }
}
