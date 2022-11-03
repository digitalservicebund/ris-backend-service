package de.bund.digitalservice.ris.norms.application

import de.bund.digitalservice.ris.norms.application.port.input.ListNormsUseCase
import de.bund.digitalservice.ris.norms.application.port.output.GetAllNormsOutputPort
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import reactor.core.publisher.Flux

// TODO: Enable once all adapters are available
// @Component
class ListNormsService(private val getAllNormsPort: GetAllNormsOutputPort) : ListNormsUseCase {

    override fun listNorms(): Flux<Norm> {
        return getAllNormsPort.getAllNorms()
    }
}
