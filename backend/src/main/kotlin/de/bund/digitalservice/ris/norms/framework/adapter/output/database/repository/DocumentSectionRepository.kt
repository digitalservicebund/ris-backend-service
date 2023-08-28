package de.bund.digitalservice.ris.norms.framework.adapter.output.database.repository

import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.DocumentSectionDto
import java.util.UUID
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface DocumentSectionRepository : ReactiveCrudRepository<DocumentSectionDto, UUID> {

  fun findByNormGuidAndParentSectionGuid(
      normGuid: UUID,
      parentSectionGuid: UUID?
  ): Flux<DocumentSectionDto>
}
