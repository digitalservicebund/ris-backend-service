package de.bund.digitalservice.ris.norms.application.service

import de.bund.digitalservice.ris.norms.application.port.input.EditNormFrameUseCase
import de.bund.digitalservice.ris.norms.application.port.output.EditNormOutputPort
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class EditNormFrameService(private val editNormOutputPort: EditNormOutputPort) :
    EditNormFrameUseCase {

    companion object {
        private val logger = LoggerFactory.getLogger(EditNormFrameService::class.java)
    }

    override fun editNormFrame(command: EditNormFrameUseCase.Command): Mono<Boolean> {
        val norm = getUpdatedNorm(command.guid, command.properties)
        val editNormCommand = EditNormOutputPort.Command(norm)
        return editNormOutputPort.editNorm(editNormCommand).doOnError {
                exception ->
            logger.error("Error occurred while updating the norm frame:", exception)
        }
    }
}

private fun getUpdatedNorm(guid: UUID, properties: EditNormFrameUseCase.NormFrameProperties) = Norm(
    guid = guid,
    articles = listOf(),
    metadataSections = properties.metadataSections,
)
