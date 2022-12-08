package norms.utils

import de.bund.digitalservice.ris.norms.application.port.input.EditNormFrameUseCase
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.reflect.full.memberProperties

fun assertEditNormFrameProperties(commandProperties: EditNormFrameUseCase.NormFrameProperties, normFrameProperties: EditNormFrameUseCase.NormFrameProperties) {
    EditNormFrameUseCase.NormFrameProperties::class.memberProperties.forEach {
        assertTrue(it.get(commandProperties) == it.get(normFrameProperties))
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
