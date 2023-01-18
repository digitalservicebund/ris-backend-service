package de.bund.digitalservice.ris.norms.framework.adapter.output

import de.bund.digitalservice.ris.norms.application.port.output.GetAllNormsOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.GetNormByGuidOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SaveNormOutputPort
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID
import kotlin.collections.HashMap

@Component
class NormsMemoryRepository : GetAllNormsOutputPort, GetNormByGuidOutputPort, SaveNormOutputPort {
    private val data = HashMap<UUID, Norm>()

    override fun saveNorm(norm: Norm): Mono<Boolean> {
        data[norm.guid] = norm
        return Mono.just(true)
    }

    override fun getNormByGuid(query: GetNormByGuidOutputPort.Query): Mono<Norm> =
        data[query.guid]?.let { Mono.just(it) } ?: Mono.empty()

    override fun getAllNorms(): Flux<Norm> =
        if (data.isNotEmpty()) Flux.fromIterable(data.values.toList()) else Flux.empty()
}
