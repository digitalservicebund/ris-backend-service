package de.bund.digitalservice.ris.norms.framework.adapter.output.database.repository

import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.ParagraphDto
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import java.util.UUID

@Repository
interface ParagraphsRepository : ReactiveCrudRepository<ParagraphDto, UUID> {

    fun findByArticleGuid(articleGuid: UUID): Flux<ParagraphDto>
}
