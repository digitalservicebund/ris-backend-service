package de.bund.digitalservice.ris.norms.framework.adapter.output.database

import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.ArticleDto
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import java.util.UUID

interface ArticlesRepository : ReactiveCrudRepository<ArticleDto, UUID>
