package de.bund.digitalservice.ris.norms.framework.adapter.output.xml.dto

data class ParagraphDto(
    val guid: String,
    val marker: String,
    val markerText: String? = null,
    val articleMarker: String,
    val text: String
)
