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
            "LEFT JOIN metadata_sections ms2 ON n.id = ms2.norm_id " +
            "LEFT JOIN metadata m1 ON ms.id  = m1.section_id " +
            "LEFT JOIN metadata m2 ON ms2.id  = m2.section_id " +
            "LEFT JOIN metadata m3 ON ms2.id  = m3.section_id " +
            "WHERE (ms2.name = 'PRINT_ANNOUNCEMENT' AND m2.type = 'ANNOUNCEMENT_GAZETTE' AND m2.value = :gazette) " +
            "AND (ms2.name = 'PRINT_ANNOUNCEMENT' AND m3.type = 'PAGE' AND m3.value = :page) " +
            "AND( " +
            "(EXTRACT(YEAR FROM n.announcement_date) = CAST(:year AS DOUBLE PRECISION)) " +
            "OR " +
            "(n.announcement_date IS NULL AND ms.name = 'CITATION_DATE' AND m1.type = 'DATE' AND substring(m1.value FROM 0 FOR 5) = :year) " +
            "OR " +
            "(n.announcement_date IS NULL AND ms.name = 'CITATION_DATE' AND m1.type = 'YEAR' AND m1.value = :year AND n.id NOT IN " +
            "(SELECT DISTINCT ms3.norm_id " +
            "FROM metadata_sections ms3 " +
            "JOIN metadata m4 ON ms3.id = m4.section_id " +
            "WHERE ms3.name = 'CITATION_DATE' " +
            "AND m4.type = 'DATE' " +
            "))) limit 1;",
    )
    fun findNormByEli(gazette: String, year: String, page: String): Mono<String>
}
