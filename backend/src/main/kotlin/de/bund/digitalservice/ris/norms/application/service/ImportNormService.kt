package de.bund.digitalservice.ris.norms.application.service

import de.bund.digitalservice.ris.norms.application.port.input.ImportNormUseCase
import de.bund.digitalservice.ris.norms.application.port.output.ParseJurisXmlOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SaveNormOutputPort
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class ImportNormService(
    private val parseJurisXmlAdapter: ParseJurisXmlOutputPort,
    private val saveNormAdapter: SaveNormOutputPort,
) : ImportNormUseCase {

    override fun importNorm(command: ImportNormUseCase.Command): Mono<UUID> {
        val guid = UUID.randomUUID()
        val parseQuery = ParseJurisXmlOutputPort.Query(guid, command.zipFile)

        return parseJurisXmlAdapter
            .parseJurisXml(parseQuery)
            .map { parsedNorm -> SaveNormOutputPort.Command(parsedNorm) }
            .flatMap { saveCommand -> saveNormAdapter.saveNorm(saveCommand) }
            .map { guid }
    }
}
