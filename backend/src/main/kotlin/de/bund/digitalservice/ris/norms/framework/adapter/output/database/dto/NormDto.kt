package de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table(name = "norms")
data class NormDto(
    @Id
    val guid: UUID,

) : Persistable<UUID> {

    @Transient
    var newEntry: Boolean = true

    override fun getId(): UUID = guid

    override fun isNew(): Boolean = newEntry
}
