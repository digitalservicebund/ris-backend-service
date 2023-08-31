package de.bund.digitalservice.ris.norms.framework.adapter.output.database.repository

import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.FormulaDto
import java.util.UUID
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface FormulaRepository : ReactiveCrudRepository<FormulaDto, UUID> {

  fun findByGuid(formulaGuid: UUID): Mono<FormulaDto>
}
