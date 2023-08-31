package de.bund.digitalservice.ris.norms.framework.adapter.output.database.repository

import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.RecitalsDto
import java.util.UUID
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface RecitalsRepository : ReactiveCrudRepository<RecitalsDto, UUID> {

  fun findByGuid(recitalsGuid: UUID): Mono<RecitalsDto>
}
