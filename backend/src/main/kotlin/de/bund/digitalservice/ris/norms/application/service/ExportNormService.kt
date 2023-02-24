package de.bund.digitalservice.ris.norms.application.service

import de.bund.digitalservice.ris.norms.application.port.input.ExportNormUseCase
import de.bund.digitalservice.ris.norms.application.port.output.GetFileOutputPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class ExportNormService(
    private val getFileOutputPort: GetFileOutputPort,
) : ExportNormUseCase {

    companion object {
        private val logger = LoggerFactory.getLogger(ExportNormService::class.java)
    }
    override fun exportNorm(command: ExportNormUseCase.Command): Mono<ByteArray> {
        val queryGetFile = GetFileOutputPort.Query(command.hash)
        return getFileOutputPort.getFile(queryGetFile)
            .doOnError {
                    exception ->
                logger.error("Error occurred while retrieving file:", exception)
            }
    }
}
