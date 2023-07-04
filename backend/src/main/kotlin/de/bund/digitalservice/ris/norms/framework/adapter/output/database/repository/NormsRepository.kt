package de.bund.digitalservice.ris.norms.framework.adapter.output.database.repository

import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.NormDto
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.util.*

@Repository
interface NormsRepository : ReactiveCrudRepository<NormDto, UUID> {

    fun deleteByGuid(normGuid: UUID): Mono<Void>
    fun findByGuid(norm: UUID): Mono<NormDto>

    @Query(
        """
        SELECT DISTINCT n.guid
        FROM norms n
        LEFT JOIN metadata_sections ms ON n.guid = ms.norm_guid
        LEFT JOIN metadata m1 ON ms.guid = m1.section_guid AND ms.name = 'CITATION_DATE'
        LEFT JOIN metadata_sections ms2 ON n.guid = ms2.norm_guid AND ms2.name = 'PRINT_ANNOUNCEMENT'
        LEFT JOIN metadata m2 ON ms2.guid = m2.section_guid AND m2.type = 'ANNOUNCEMENT_GAZETTE' AND m2.value = :gazette
        LEFT JOIN metadata m3 ON ms2.guid = m3.section_guid AND m3.type = 'PAGE' AND m3.value = :page
        LEFT JOIN metadata_sections ms3 ON n.guid = ms3.norm_guid AND ms3.name = 'DIGITAL_ANNOUNCEMENT'
        LEFT JOIN metadata m4 ON ms3.guid = m4.section_guid AND m4.type = 'ANNOUNCEMENT_MEDIUM' AND m4.value = :gazette
        LEFT JOIN metadata m5 ON ms3.guid = m5.section_guid AND ((m5.type = 'EDITION' AND m5.value = :page)
                                                            OR (m5.type = 'PAGE' AND m5.value = :page))
       LEFT JOIN metadata_sections ms4 ON n.guid = ms4.norm_guid
       LEFT JOIN metadata m6 ON ms4.guid = m6.section_guid AND ms4.name = 'ANNOUNCEMENT_DATE'
        WHERE (
                ((ms2.name = 'PRINT_ANNOUNCEMENT' AND m2.type = 'ANNOUNCEMENT_GAZETTE' AND m2.value = :gazette)
                AND (ms2.name = 'PRINT_ANNOUNCEMENT' AND m3.type = 'PAGE' AND m3.value = :page))
                OR (
                    n.guid NOT IN (SELECT ms5.norm_guid
                                 FROM metadata_sections ms5
                                 JOIN metadata m7 ON ms5.guid = m7.section_guid
                                 JOIN metadata m8 ON ms5.guid = m8.section_guid
                                 WHERE ms5.name = 'PRINT_ANNOUNCEMENT'
                                 AND m7.type = 'ANNOUNCEMENT_GAZETTE'
                                 AND m8.type = 'PAGE')
                    AND (ms3.name = 'DIGITAL_ANNOUNCEMENT' AND m4.type = 'ANNOUNCEMENT_MEDIUM' AND m4.value = :gazette)
                    AND (ms3.name = 'DIGITAL_ANNOUNCEMENT' AND ((m5.type = 'EDITION' AND m5.value = :page)
                                                                OR (m5.type = 'PAGE' AND m5.value = :page
                                                                    AND n.guid NOT IN (SELECT ms6.norm_guid
                                                                                     FROM metadata_sections ms6
                                                                                     JOIN metadata m9 ON ms6.guid = m9.section_guid
                                                                                     WHERE ms6.name = 'DIGITAL_ANNOUNCEMENT'
                                                                                     AND m9.type = 'EDITION')
                                                                    )
                                                                )
                        )
                    )
                )
            AND (
                ((m6.type = 'DATE' AND SUBSTRING(m6.value, 1, 4) = :year) or (m6.type = 'YEAR' AND m6.value = :year))
                OR ((n.guid NOT IN (SELECT ms7.norm_guid
                                 FROM metadata_sections ms7
                                 WHERE ms7.name = 'ANNOUNCEMENT_DATE')) AND m1.type = 'DATE' AND SUBSTRING(m1.value, 1, 4) = :year)
                OR ((n.guid NOT IN (SELECT ms8.norm_guid
                                 FROM metadata_sections ms8
                                 WHERE ms8.name = 'ANNOUNCEMENT_DATE')) AND m1.type = 'YEAR' AND m1.value = :year AND n.guid NOT IN (SELECT DISTINCT ms9.norm_guid
                                                                                                           FROM metadata_sections ms9
                                                                                                           JOIN metadata m10 ON ms9.guid = m10.section_guid
                                                                                                           WHERE ms9.name = 'CITATION_DATE'
                                                                                                           AND m10.type = 'DATE')
                    )
                )
        LIMIT 1
    """,
    )
    fun findNormByEli(gazette: String, year: String, page: String): Mono<String>
}
