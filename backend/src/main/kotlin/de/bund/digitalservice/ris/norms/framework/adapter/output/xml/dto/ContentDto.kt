package de.bund.digitalservice.ris.norms.framework.adapter.output.xml.dto

import java.util.UUID

data class ContentDto(
    val guid: String = UUID.randomUUID().toString(),
    var marker: String? = null,
    var order: Int = 1,
    var isText: Boolean = false,
    var isList: Boolean = false,
    var text: IdentifiedElement? = null,
    var intro: IdentifiedElement? = null,
    var points: MutableList<ContentDto> = mutableListOf()
)
