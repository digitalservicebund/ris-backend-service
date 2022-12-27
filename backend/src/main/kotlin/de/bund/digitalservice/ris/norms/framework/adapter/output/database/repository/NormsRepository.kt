package de.bund.digitalservice.ris.norms.framework.adapter.output.database.repository

import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.NormDto
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.util.UUID

@Repository
interface NormsRepository : ReactiveCrudRepository<NormDto, UUID> {

    fun findByGuid(norm: UUID): Mono<NormDto>
}
