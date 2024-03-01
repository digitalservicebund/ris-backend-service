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
    aliases: ["ABWMEIN"],
    examples: [],
    types: ["case_law"],
  },
  {
    label: "Dokumentnummer",
    aliases: ["DOKUMENTNUMMER", "NR"],
    examples: ["NR:ABCD200501001"],
    types: ["case_law"],
  },
  {
    label: "Dokumenttyp",
    aliases: ["DOKUMENTTYP", "TYP"],
    examples: ["TYP:Beschluss"],
    types: ["case_law"],
  },
  {
    label: "ECLI",
    aliases: ["ECLI", "ecli"],
    examples: ['ECLI:"ECLI:DE:BEISPIEL:2024:0001.000000.00.00"'],
    types: ["case_law"],
  },
  {
    label: "Entscheidungsdatum",
    aliases: ["DATUM", "DAT"],
    examples: ["DAT:2024-01-01"],
    types: ["case_law"],
  },
  {
    label: "Entscheidungsgründe",
    aliases: ["ENTSCHEIDUNGSGRUENDE", "EGR"],
    examples: [],
    types: ["case_law"],
  },
  {
    label: "Entscheidungsname",
    aliases: ["ENTSCHEIDUNGSNAME", "ENAME"],
    examples: [],
    types: ["case_law"],
  },
  {
    label: "Gründe",
    aliases: ["GRUENDE", "GR"],
    examples: [],
    types: ["case_law"],
  },
  {
    label: "Leitsatz",
    aliases: ["LEITSATZ", "LS"],
    examples: [],
    types: ["case_law"],
  },
  {
    label: "Orientierungssatz",
    aliases: ["ORIENTIERUNGSSATZ", "OSATZ"],
    examples: [],
    types: ["case_law"],
  },
  {
    label: "Sonstiger Langtext",
    aliases: ["SLANGTEXT", "STEXT"],
    examples: [],
    types: ["case_law"],
  },
  {
    label: "Sonstiger Orientierungssatz",
    aliases: ["SORIENTIERUNGSSATZ", "SOSATZ"],
    examples: [],
    types: ["case_law"],
  },
  {
    label: "Tatbestand",
    aliases: ["TATBESTAND", "TB"],
    examples: [],
    types: ["case_law"],
  },
  {
    label: "Tenor",
    aliases: ["TENOR"],
    examples: ['TENOR:"Arbeitsverhältnis"'],
    types: ["case_law"],
  },
  {
    label: "Titelzeile",
    aliases: ["TITELZEILE", "TTZE"],
    examples: [],
    types: ["case_law"],
  },
]

export const availableFeatures = [
  {
    id: "und",
    label: "UND-Verknüpfung",
    description:
      "Gefunden werden Dokumente, die alle der durch AND verknüpften Suchbegriffe enthalten..",
    examples: [`DATUM:[2000 TO 2010] AND LEITSATZ:Arbeitsverhältnis`],
  },
  {
    id: "oder",
    label: "ODER-Verknüpfung",
    description:
      "Gefunden werden Dokumente, die mindestens einen der durch OR verknüpften Suchbegriffe enthalten.",
    examples: [`DATUM:[2000 TO 2010] OR LEITSATZ:Arbeitsverhältnis`],
  },
  {
    id: "Exact phrasing",
    label: "Genauer Ausdruck",
    description:
      "Gefunden werden Dokumente, die den genauen Ausdruck enthalten.",
    examples: [`ECLI:"ECLI:DE:LAGRLP:2001:0809.6SA135.01.0A"`],
  },
  {
    id: "Wildcards",
    label: "Platzhalter-Suche für mehrere Zeichen",
    description:
      "Platzhalter-Suchen können für einzelne Begriffe ausgeführt werden, wobei * null oder mehr Zeichen ersetzt. “Arbeits*“ findet Dokumente, die den Ausdruck “Arbeits” enthalten, wie z.B. Arbeitsgericht, Arbeitsverhältnis. Gefunden werden somit auch Dokumente, die etwa die Formulierung “Arbeits- und Beschäftigungsfragen” nutzen.",
    examples: [`TENOR:"Arbeits*"`],
  },
  {
    id: "Wildcards2",
    label: "Platzhalter-Suche für ein Zeichen",
    description:
      "Platzhalter-Suchen können für einzelne Begriffe ausgeführt werden, wobei ? ein einzelnes Zeichen ersetzt. So können bei der Suche nach dem EGMR-Urteil “Akkoç/Türkei”, mehrere Schreibweisen berücksichtigt werden.",
    examples: [`ENTSCHEIDUNGSNAME:Akko?`],
  },
  {
    id: "Fuzzyness",
    label: "Unschärfe",
    description:
      "Gefunden werden Dokumente, die nicht exakt mit dem Suchbegriff übereinstimmen, sondern eine gewisse Ähnlichkeit aufweisen (Schifffahrt, Schiffahrt). Berücksichtigt werden standardmäßig Begriffe, die maximal zwei Bearbeitungsschritte benötigen. Soll lediglich ein Zeichenunterschied berücksichtigt werden, kann eine Zahl ergänzt werden (Schifffahrt~1).",
    examples: [`ENTSCHEIDUNGSNAME:Schifffahrt~`],
  },
  {
    id: "Proximity searches",
    label: "Nahe Suche",
    description:
      "Während eine Suchanfrage zu einem genauen Ausdruck “Corona Impfung“ nur die exakte Schreibweise und Reihenfolge berücksichtigt, werden bei folgendem Beispiel auch Leitsätze wie Corona-Schutz-Impfung oder Impfung in der Trefferliste angezeigt. Durch den Wert der Zahl kann die maximale Bearbeitungsdistanz festgelegt werden.",
    examples: [`LEITSATZ:"Corona-Impfung"~5`],
  },
  {
    id: "ranges",
    label: "Bereiche",
    description:
      "Bereiche können für Datum-, numerische oder Zeichenfelder angegeben werden. Inklusive Bereiche werden mit eckigen Klammern ([]) und exklusive Bereiche mit geschweiften Klammern ({}) angegeben. Für Bereiche mit einer unbeschränkten Seite können das Größer-als-Zeichen (>) oder das Kleiner-als-Zeichen (<) verwendet werden.",
    examples: [
      `DATUM:[2024-01-01 TO 2024-01-10]`,
      `DATUM:{2024-01-01 TO 2024-01-10}`,
      `DATUM:>2024-01-15`,
    ],
  },
  {
    id: "boosting",
    label: "Boost-Funktion",
    description:
      "Die Boost-Funktion wird genutzt, um einen Begriff relevanter zu mache als einen anderen. Sollen also z.B. Dokumente zu den Begriffen “Corona” und “Impfung” gefunden werden, aber besonderes Interesse an Dokumenten zu “Corona” besteht.",
    examples: [
      `LEITSATZ:Corona^2 Impfung`,
      `"Landessozialgericht"^2 (COVID-19 Corona)^4`,
    ],
  },
  {
    id: "grouping",
    label: "Gruppieren",
    description:
      "Mehrere Begriffe oder Ausdrücke können mit Klammern gruppiert werden.",
    examples: [`GRUENDE:(Hund OR Katze) AND Haustier`],
  },
]
