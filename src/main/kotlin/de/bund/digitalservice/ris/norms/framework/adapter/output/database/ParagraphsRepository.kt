package de.bund.digitalservice.ris.norms.framework.adapter.output.database

import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.ParagraphDto
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import java.util.UUID

interface ParagraphsRepository : ReactiveCrudRepository<ParagraphDto, UUID>
