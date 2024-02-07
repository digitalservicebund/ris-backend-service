export type FieldType =
  | "all"
  | "case_law"
  | "norms"
  | "administrative_regulations"
  | "literature"

export type Field = {
  label: string
  id?: string
  aliases: string[]
  examples: string[]
  types: FieldType[]
}

export const availableFields: Field[] = [
  {
    label: "Abweichende Meinung",
    aliases: ["dissenting_opinion"],
    examples: [],
    types: ["case_law"],
  },
  {
    label: "Dokumentnummer",
    aliases: ["document_number"],
    examples: ["document_number:ABCD200501001"],
    types: ["case_law"],
  },
  {
    label: "Dokumenttyp",
    aliases: ["document_type"],
    examples: ["document_type:Beschluss"],
    types: ["case_law"],
  },
  {
    label: "ECLI",
    aliases: ["ecli"],
    examples: ['ecli:"ECLI:DE:BEISPIEL:2024:0001.000000.00.00"'],
    types: ["case_law"],
  },
  {
    label: "Entscheidungsdatum",
    aliases: ["decision_date"],
    examples: ["decision_date:2024-01-01"],
    types: ["case_law"],
  },
  {
    label: "Entscheidungsgründe",
    aliases: ["decision_grounds"],
    examples: [],
    types: ["case_law"],
  },
  {
    label: "Entscheidungsname",
    aliases: ["headline"],
    examples: [],
    types: ["case_law"],
  },
  {
    label: "Gründe",
    aliases: ["grounds"],
    examples: [],
    types: ["case_law"],
  },
  {
    label: "Leitsatz",
    aliases: ["guiding_principle"],
    examples: [],
    types: ["case_law"],
  },
  {
    label: "Orientierungssatz",
    aliases: ["headnote"],
    examples: [],
    types: ["case_law"],
  },
  {
    label: "Sonstiger Langtext",
    aliases: ["other_long_text"],
    examples: [],
    types: ["case_law"],
  },
  {
    label: "Sonstiger Orientierungssatz",
    aliases: ["other_headnote"],
    examples: [],
    types: ["case_law"],
  },
  {
    label: "Tatbestand",
    aliases: ["case_facts"],
    examples: [],
    types: ["case_law"],
  },
  {
    label: "Tenor",
    aliases: ["tenor"],
    examples: ['tenor:"Arbeitsverhältnis"'],
    types: ["case_law"],
  },
  {
    label: "Titelzeile",
    aliases: ["headline"],
    examples: [],
    types: ["case_law"],
  },
]

export const availableFeatures = [
  {
    id: "und",
    label: "UND-Verknüpfung",
    description:
      "Gefunden werden Dokumente, die <strong>alle</strong> der durch AND verknüpften Suchbegriffe enthalten.",
    examples: [
      `decision_date:[2000 TO 2010] AND guiding_principle:Arbeitsverhältnis`,
    ],
  },
  {
    id: "oder",
    label: "ODER-Verknüpfung",
    description:
      "Gefunden werden Dokumente, die <strong>mindestens einen</strong> der durch OR verknüpften Suchbegriffe enthalten.",
    examples: [
      `decision_date:[2000 TO 2010] OR guiding_principle:Arbeitsverhältnis`,
    ],
  },
]
