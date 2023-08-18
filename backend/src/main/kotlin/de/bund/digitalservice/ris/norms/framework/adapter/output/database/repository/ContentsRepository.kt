package de.bund.digitalservice.ris.norms.framework.adapter.output.database.repository

import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.ContentDto
import java.util.UUID
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface ContentsRepository : ReactiveCrudRepository<ContentDto, UUID> {

  fun findBySectionGuidIn(sectionGuids: List<UUID>): Flux<ContentDto>

  fun findByNormGuid(normGuid: UUID): Flux<ContentDto>
}
