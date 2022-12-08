package norms.utils

import de.bund.digitalservice.ris.norms.application.port.input.EditNormFrameUseCase
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.EditNormFrameController
import decodeLocalDate
import org.junit.jupiter.api.Assertions.assertTrue
import java.time.LocalDate
import kotlin.reflect.full.memberProperties

fun assertEditNormFrameProperties(commandProperties: EditNormFrameUseCase.NormFrameProperties, normFrameProperties: EditNormFrameUseCase.NormFrameProperties) {
    EditNormFrameUseCase.NormFrameProperties::class.memberProperties.forEach {
        assertTrue(it.get(commandProperties) == it.get(normFrameProperties))
    }
}

fun assertEditNormFramePropertiesAndEditNormFrameRequestSchema(normFrameProperties: EditNormFrameUseCase.NormFrameProperties, normFrameRequestSchema: EditNormFrameController.NormFramePropertiesRequestSchema) {
    val normFrameRequestSchemaMembers = EditNormFrameController.NormFramePropertiesRequestSchema::class.memberProperties
    val normFramePropertiesMembers = EditNormFrameUseCase.NormFrameProperties::class.memberProperties
    normFramePropertiesMembers.forEach { normFramePropertiesMember ->
        val found = normFrameRequestSchemaMembers.find { normFrameRequestSchemaMember ->
            normFramePropertiesMember.name == normFrameRequestSchemaMember.name
        }
        if (normFramePropertiesMember.get(normFrameProperties) is LocalDate) {
            assertTrue(normFramePropertiesMember.get(normFrameProperties) == decodeLocalDate(found?.get(normFrameRequestSchema).toString()))
        } else {
            assertTrue(normFramePropertiesMember.get(normFrameProperties) == found?.get(normFrameRequestSchema))
        }
    }
}

fun assertNormAndEditNormFrameProperties(norm: Norm, normFrameProperties: EditNormFrameUseCase.NormFrameProperties) {
    val normMembers = Norm::class.memberProperties
    val normFramePropertiesMembers = EditNormFrameUseCase.NormFrameProperties::class.memberProperties
    normFramePropertiesMembers.forEach { normFramePropertiesMember ->
        val found = normMembers.find { normMember ->
            normFramePropertiesMember.name == normMember.name
        }
        assertTrue(normFramePropertiesMember.get(normFrameProperties) == found?.get(norm))
    }
}
