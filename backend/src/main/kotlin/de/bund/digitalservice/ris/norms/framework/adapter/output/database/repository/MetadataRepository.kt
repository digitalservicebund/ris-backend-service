package de.bund.digitalservice.ris.norms.framework.adapter.output.database.repository

import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.MetadatumDto
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface MetadataRepository : ReactiveCrudRepository<MetadatumDto, Int> {

    fun findBySectionId(normId: Int): Flux<MetadatumDto>

    fun deleteBySectionId(normId: Int): Mono<Void>

    fun findBySectionIdIn(sectionIds: List<Int>): Flux<MetadatumDto>
}
