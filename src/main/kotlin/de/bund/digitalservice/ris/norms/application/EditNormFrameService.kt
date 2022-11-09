package de.bund.digitalservice.ris.norms.application

import de.bund.digitalservice.ris.norms.application.port.input.EditNormFrameUseCase
import de.bund.digitalservice.ris.norms.application.port.output.GetNormByGuidOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SaveNormOutputPort
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class EditNormFrameService(
    private val getNormByGuidPort: GetNormByGuidOutputPort,
    private val saveNormOutputPort: SaveNormOutputPort
) : EditNormFrameUseCase {

    override fun editNormFrame(command: EditNormFrameUseCase.Command): Mono<Boolean> {
        return getNormByGuidPort
            .getNormByGuid(command.guid)
            .map({ norm -> norm.apply { longTitle = command.longTitle } })
            .flatMap({ norm -> saveNormOutputPort.saveNorm(norm) })
    }
}
