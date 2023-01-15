package de.bund.digitalservice.ris.norms.framework.adapter.output.xml.dto

import java.util.*

data class ContentDto(
    val guid: String = UUID.randomUUID().toString(),
    var marker: String? = null,
    var paragraphMarker: String? = null,
    var order: Int = 1,
    var isText: Boolean = false,
    var isList: Boolean = false,
    var text: IdentifiedElement? = null,
    var intro: IdentifiedElement? = null,
    var points: MutableList<ContentDto> = mutableListOf(),
    var listMarkerGrandparent: String? = null,
    var listMarkerParent: String? = null
) {
    var markerClean: String? = null
        get() {
            return if (field == null) {
                marker?.replace(Regex("[^a-zA-Z0-9]"), "")
            } else {
                field
            }
        }
}
