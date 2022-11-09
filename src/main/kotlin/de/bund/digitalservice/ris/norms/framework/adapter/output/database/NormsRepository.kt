package de.bund.digitalservice.ris.norms.framework.adapter.output.database

import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.NormDto
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono
import java.util.UUID

interface NormsRepository : ReactiveCrudRepository<NormDto, UUID> {
    @Query(
        """
        SELECT * FROM norms
        INNER JOIN articles on articles.norm_id = norms.id
        INNER JOIN paragraphs on paragraphs.article_id = articles.id
        WHERE norms.guid = $1
        """
    )
    override fun findById(id: UUID): Mono<NormDto>
}
