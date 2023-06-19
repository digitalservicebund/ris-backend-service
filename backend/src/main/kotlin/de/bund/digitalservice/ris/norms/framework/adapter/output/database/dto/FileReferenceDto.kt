package de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table(name = "files")
data class FileReferenceDto(
    @Id
    val id: Int,
    val name: String,
    var hash: String,
    @Column("norm_id")
    val normId: Int,
    @Column("norm_guid")
    val normGuid: UUID,
    @Column("created_at")
    val createdAt: LocalDateTime,
)
