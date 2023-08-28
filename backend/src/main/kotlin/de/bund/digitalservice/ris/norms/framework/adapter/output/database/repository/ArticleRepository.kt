package de.bund.digitalservice.ris.norms.framework.adapter.output.database.repository

import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.ArticleDto
import java.util.UUID
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface ArticleRepository : ReactiveCrudRepository<ArticleDto, UUID> {

  fun findByNormGuidAndDocumentSectionGuid(
      normGuid: UUID,
      documentSectionGuid: UUID?
  ): Flux<ArticleDto>
}
