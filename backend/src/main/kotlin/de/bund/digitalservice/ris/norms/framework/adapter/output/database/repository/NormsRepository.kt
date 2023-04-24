package de.bund.digitalservice.ris.norms.framework.adapter.output.database.repository

import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.NormDto
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.util.*

@Repository
interface NormsRepository : ReactiveCrudRepository<NormDto, UUID> {

    fun findByGuid(norm: UUID): Mono<NormDto>

    @Query(
        "SELECT DISTINCT n.guid " +
            "FROM norms n " +
            "LEFT JOIN metadata_sections ms ON n.id = ms.norm_id " +
            "LEFT JOIN metadata m ON ms.id  = m.section_id " +
            "AND (ms.name = 'PRINT_ANNOUNCEMENT' AND m.type = 'ANNOUNCEMENT_GAZETTE' AND m.value = :gazette) " +
            "AND (ms.name = 'PRINT_ANNOUNCEMENT' AND m.type = 'PAGE' AND m.value = :page) " +
            "AND( " +
            "(EXTRACT(YEAR FROM n.announcement_date) = CAST(:year AS DOUBLE PRECISION)) " +
            "OR " +
            "(n.announcement_date IS NULL AND ms.name = 'CITATION_DATE' AND m.type = 'DATE' AND substring(m.value FROM 0 FOR 5) = :year) " +
            "OR " +
            "(n.announcement_date IS NULL AND ms.name = 'CITATION_DATE' AND m.type = 'YEAR' AND m.value = :year AND n.id NOT IN " +
            "(SELECT DISTINCT ms2.norm_id " +
            "FROM metadata_sections ms2 " +
            "JOIN metadata m2 ON ms2.id = m2.section_id " +
            "WHERE ms2.name = 'CITATION_DATE' " +
            "AND m2.type = 'DATE' " +
            "))) limit 1;",
    )
    fun findNormByEli(gazette: String, year: String, page: String): Mono<String>
}
