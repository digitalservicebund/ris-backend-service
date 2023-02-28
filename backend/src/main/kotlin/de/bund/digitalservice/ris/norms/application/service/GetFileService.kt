package de.bund.digitalservice.ris.norms.application.service

import de.bund.digitalservice.ris.norms.application.port.input.GetFileUseCase
import de.bund.digitalservice.ris.norms.application.port.output.GetFileOutputPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class GetFileService(
    private val getFileOutputPort: GetFileOutputPort,
) : GetFileUseCase {

    companion object {
        private val logger = LoggerFactory.getLogger(GetFileService::class.java)
    }
    override fun getFile(command: GetFileUseCase.Command): Mono<ByteArray> {
        val queryGetFile = GetFileOutputPort.Query(command.hash)
        return getFileOutputPort.getFile(queryGetFile)
            .doOnError {
                    exception ->
                logger.error("Error occurred while retrieving file:", exception)
            }
    }
}
