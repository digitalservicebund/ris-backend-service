package de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto

import java.util.UUID
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table

@Table(name = "norms")
data class Norm(
    @Id
    @GeneratedValue
    val guid: UUID,
    @Column(name = "long_title")
    val longTitle: String
) {
    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    lateinit var articles: List<Article>
}
