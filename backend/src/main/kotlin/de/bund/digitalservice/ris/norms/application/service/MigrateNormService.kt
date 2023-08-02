package de.bund.digitalservice.ris.norms.application.service

import de.bund.digitalservice.ris.norms.application.port.input.MigrateNormUseCase
import de.bund.digitalservice.ris.norms.application.port.output.ParseJurisArrayOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SaveNormOutputPort
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class MigrateNormService(
    private val parseJurisArrayAdapter: ParseJurisArrayOutputPort,
    private val saveNormAdapter: SaveNormOutputPort,
) : MigrateNormUseCase {

  companion object {
    var logger: Logger = LoggerFactory.getLogger(MigrateNormService::class.java)
  }

  override fun migrateNorm(command: MigrateNormUseCase.Command): Mono<Boolean> {
    val parseQuery = ParseJurisArrayOutputPort.Query(command.norms.norms)

    return parseJurisArrayAdapter
        .parseJurisArray(parseQuery)
        .flatMap { norm -> saveNormAdapter.saveNorm(SaveNormOutputPort.Command(norm)) }
        .then(Mono.just(true))
        .doOnError { exception ->
          logger.error("Error occurred while saving the file to bucket:", exception)
        }
  }
}
