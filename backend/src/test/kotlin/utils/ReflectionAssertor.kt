package utils

import de.bund.digitalservice.ris.norms.application.port.input.EditNormFrameUseCase
import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller.EditNormFrameControllerTest
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.decodeLocalDate
import org.assertj.core.api.Assertions.assertThat
import java.time.LocalDate
import kotlin.reflect.full.memberProperties

fun assertEditNormFramePropertiesAndEditNormRequestSchema(
    normFrameProperties: EditNormFrameUseCase.NormFrameProperties,
    normFrameRequestSchema: EditNormFrameControllerTest.NormFramePropertiesTestRequestSchema,
) {
    val normFrameRequestSchemaMembers =
        EditNormFrameControllerTest.NormFramePropertiesTestRequestSchema::class.memberProperties
    val normFramePropertiesMembers = EditNormFrameUseCase.NormFrameProperties::class.memberProperties
    normFramePropertiesMembers.forEach { normFramePropertiesMember ->
        if (normFramePropertiesMember.name == "metadataSections") {
            assertMetadataSections(normFrameProperties.metadataSections.filter { it.name != MetadataSectionName.DOCUMENT_TYPE }, normFrameRequestSchema.metadataSections)

            return@forEach
        }

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

private fun assertMetadataSections(propertiesSections: List<MetadataSection>, schemaSections: List<*>) {
    val guidRegex = Regex("guid=[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}")
    assertThat(propertiesSections.toString().replace(guidRegex, ""))
        .isEqualTo(schemaSections.toString().replace(guidRegex, ""))
}
