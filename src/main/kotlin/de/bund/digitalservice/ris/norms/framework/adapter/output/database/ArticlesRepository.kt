package de.bund.digitalservice.ris.norms.framework.adapter.output.database

import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.ArticleDto
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.util.UUID

@Repository
interface ArticlesRepository : ReactiveCrudRepository<ArticleDto, UUID> {

    fun findByNorm(norm: Int): Mono<ArticleDto>
}
