package de.bund.digitalservice.ris.norms.framework.adapter.output.database.repository

import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.MetadatumDto
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Repository
interface MetadataRepository : ReactiveCrudRepository<MetadatumDto, UUID> {

    fun deleteBySectionGuid(normGuid: UUID): Mono<Void>

    fun findBySectionGuidIn(sectionGuids: List<UUID>): Flux<MetadatumDto>
}
