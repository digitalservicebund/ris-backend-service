package norms.utils

import de.bund.digitalservice.ris.norms.application.port.input.EditNormFrameUseCase
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.EditNormFrameController
import org.jeasy.random.EasyRandom
import org.jeasy.random.EasyRandomParameters

fun createRandomNormFameProperties(): EditNormFrameUseCase.NormFrameProperties {
    return EasyRandom().nextObject(EditNormFrameUseCase.NormFrameProperties::class.java)
}

fun createRandomNormFramePropertiesRequestSchema(): EditNormFrameController.NormFramePropertiesRequestSchema {
    val parameters: EasyRandomParameters = EasyRandomParameters().randomize(String::class.java) { "2020-10-21" } // needed for string date fields
    val easyRandom = EasyRandom(parameters)
    return easyRandom.nextObject(EditNormFrameController.NormFramePropertiesRequestSchema::class.java)
}

fun createRandomNorm(): Norm {
    val parameters: EasyRandomParameters = EasyRandomParameters().stringLengthRange(5, 20) // needed for marker fields
    val easyRandom = EasyRandom(parameters)
    return easyRandom.nextObject(Norm::class.java)
}
