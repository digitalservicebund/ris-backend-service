package de.bund.digitalservice.ris.norms.framework.output

import de.bund.digitalservice.ris.norms.application.port.output.GetAllNormsOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.GetNormByGuidOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SaveNormOutputPort
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.value.Guid
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class NormsMemoryRepository : GetAllNormsOutputPort, GetNormByGuidOutputPort, SaveNormOutputPort {
    private val data = HashMap<Guid, Norm>()

    override fun saveNorm(norm: Norm): Mono<Boolean> {
        data[norm.guid] = norm
        return Mono.just(true)
    }

    override fun getNormByGuid(guid: Guid): Mono<Norm> =
        data[guid]?.let { Mono.just(it) } ?: Mono.empty()

    override fun getAllNorms(): Flux<Norm> =
        if (data.isEmpty()) Flux.fromIterable(data.values.toList()) else Flux.empty()
}
