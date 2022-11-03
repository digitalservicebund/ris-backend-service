package de.bund.digitalservice.ris.norms.application

import de.bund.digitalservice.ris.norms.application.port.input.LoadNormUseCase
import de.bund.digitalservice.ris.norms.application.port.output.GetNormByGuidOutputPort
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import reactor.core.publisher.Mono

// TODO: Enable once all adapters are available
// @Component
class LoadNormService(private val getNormByGuidPort: GetNormByGuidOutputPort) : LoadNormUseCase {

    override fun loadNorm(query: LoadNormUseCase.Query): Mono<Norm> {
        return getNormByGuidPort.getNormByGuid(query.guid)
    }
}
