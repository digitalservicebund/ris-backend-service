package de.bund.digitalservice.ris.norms.application.service

import de.bund.digitalservice.ris.norms.application.port.input.ImportTestDataUseCase
import de.bund.digitalservice.ris.norms.application.port.output.SaveNormOutputPort
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class ImportTestDataService(val saveNormAdapter: SaveNormOutputPort) : ImportTestDataUseCase {
  override fun importTestData(command: ImportTestDataUseCase.Command): Mono<Boolean> {
    val saveCommand = SaveNormOutputPort.Command(command.norm)
    return saveNormAdapter.saveNorm(saveCommand)
  }
}
