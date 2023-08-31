package de.bund.digitalservice.ris.norms.framework.adapter.output.database.repository

import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.ConclusionDto
import java.util.UUID
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface ConclusionRepository : ReactiveCrudRepository<ConclusionDto, UUID> {

  fun findByGuid(conclusionGuid: UUID): Mono<ConclusionDto>
}
