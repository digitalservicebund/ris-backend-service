package de.bund.digitalservice.ris.norms.framework.adapter.output.database.repository

import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.MetadataSectionDto
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface MetadataSectionsRepository : ReactiveCrudRepository<MetadataSectionDto, Int> {

    fun findByNormId(normId: Int): Flux<MetadataSectionDto>

    fun deleteByNormId(normId: Int): Mono<Void>
}
