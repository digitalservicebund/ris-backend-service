package de.bund.digitalservice.ris.norms.framework.adapter.output.database.repository

import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.MetadataSectionDto
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Repository
interface MetadataSectionsRepository : ReactiveCrudRepository<MetadataSectionDto, UUID> {

    fun findByNormGuid(normGuid: UUID): Flux<MetadataSectionDto>

    fun deleteByNormGuid(normGuid: UUID): Mono<Void>
}
