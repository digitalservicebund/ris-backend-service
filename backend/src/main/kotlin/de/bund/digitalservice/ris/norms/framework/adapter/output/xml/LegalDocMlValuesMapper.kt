package de.bund.digitalservice.ris.norms.framework.adapter.output.xml

data class MappedValue(
    val newValue: String,
    val oldValues: List<String>,
)

data class MappedValues(
    val property: Property,
    val defaultValue: String,
    val values: List<MappedValue>,
)

enum class Property {
    DOCUMENT_TYPE_NAME,
    DOCUMENT_NORM_CATEGORY,
    PROVIDER_DECIDING_BODY,
    PARTICIPATION_INSTITUTION,
}

val legalDocMlValuesMap: List<MappedValues> = listOf(
    MappedValues(
        Property.DOCUMENT_TYPE_NAME,
        "gesetz",
        listOf(
            MappedValue("verordnung", listOf("RV")),
            MappedValue("verwaltungsvorschrift", listOf("VV")),
            MappedValue("sonstige-bekanntmachung", listOf("SB", "OQ", "SO")),
        ),
    ),
    MappedValues(
        Property.DOCUMENT_NORM_CATEGORY,
        "rechtsetzungsdokument",
        listOf(),
    ),
    MappedValues(
        Property.PROVIDER_DECIDING_BODY,
        "nicht-vorhanden",
        listOf(
            MappedValue("bundeskanzler", listOf("Bundeskanzlerin", "Bundeskanzler")),
            MappedValue("bundespräsident", listOf("Bundespräsident")),
            MappedValue("bundesrat", listOf("Präsident des Bundesrates", "BR", "Rat")),
            MappedValue(
                "bundestag",
                listOf(
                    "BT",
                    "Präsident des Deutschen Bundestages",
                    "Präsidentin des Deutschen Bundestages",
                    "Direktor beim Deutschen Bundestag",
                ),
            ),
            MappedValue(
                "bundesregierung",
                listOf(
                    "BMin.*",
                    "Bundesminister.*",
                    "BMVBS",
                    "BMU",
                    "BMV",
                    "BMI",
                    "BReg",
                    "BMF",
                    "BMP",
                    "BMA",
                    "AA",
                    "BMJFG",
                    "BMPT",
                    "BMWi",
                    "BMJ",
                    "BMVg",
                    "BMWiE",
                    "BMBau",
                    "BMJFFG",
                    "BMWF",
                    "BMBW",
                    "BMELV",
                    "BMFuS",
                    "BMVBW",
                    "BMBF",
                    "BMAS",
                    "BMFSFJ",
                    "BMJV",
                    "Bundeskanzleramt",
                    "BMGS",
                    "BMZ",
                    "BMWA",
                    "BMIBH",
                    "BMFJ",
                ),
            ),
        ),
    ),
    MappedValues(
        Property.PARTICIPATION_INSTITUTION,
        "nicht-vorhanden",
        listOf(
            MappedValue("bundeskanzler", listOf("Bundeskanzlerin", "Bundeskanzler")),
            MappedValue("bundesrat", listOf("Direktor des Bundesrates", "BR")),
            MappedValue(
                "bundestag",
                listOf(
                    "Rechte des Deutschen Bundestages",
                    "BT",
                    "Beschluss des Bundestages",
                    "Rechte des Bundestages",
                    "Bundestag",
                    "Direktor beim Deutschen Bundestag",
                ),
            ),
            MappedValue(
                "bundesregierung",
                listOf(
                    "BMin.*",
                    "BMAS",
                    "BMA",
                    "BMP",
                    "BMF",
                    "BMI",
                    "BMWi",
                    "BMJ",
                    "BMPT",
                    "Bundesminister.*",
                    "BMWF",
                    "BML",
                    "BMV",
                    "BMBW",
                    "AA",
                    "BMU",
                    "BMVg",
                    "BMinIBH",
                    "Bundeskanzleramt",
                    "BMJFG",
                    "BReg",
                    "BMBau",
                    "BMJFFG",
                    "BMFuS",
                    "Chef des Bundeskanzleramtes",
                    "BMZE",
                ),
            ),
        ),
    ),
)

fun getMappedValue(property: Property, value: String): String? {
    val mappedValues = legalDocMlValuesMap.firstOrNull {
        it.property === property
    }

    return mappedValues?.values?.firstOrNull {
        it.oldValues.any { oldValue -> value.contains(Regex("^$oldValue$")) }
    }?.newValue ?: mappedValues?.defaultValue
}
