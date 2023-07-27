package de.bund.digitalservice.ris.norms.framework.adapter.output.database.repository

import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.ArticleDto
import java.util.UUID
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface ArticlesRepository : ReactiveCrudRepository<ArticleDto, UUID> {

  fun findByNormGuid(normGuid: UUID): Flux<ArticleDto>
}
