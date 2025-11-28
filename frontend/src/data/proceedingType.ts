import { ProceedingType } from "@/domain/objectValue"

export const proceedingType = [
  {
    label: "Verfassungsbeschwerde",
    value: ProceedingType.VERFASSUNGSBESCHWERDE,
  },
  { label: "Organstreitverfahren", value: ProceedingType.ORGANSTREITVERFAHREN },
  {
    label: "Konkrete Normenkontrolle",
    value: ProceedingType.KONKRETE_NORMENKONTROLLE,
  },
  {
    label: "Abstrakte Normenkontrolle",
    value: ProceedingType.ABSTRAKTE_NORMENKONTROLLE,
  },
  {
    label: "Bund-Länder-Streitigkeit",
    value: ProceedingType.BUND_LAENDER_STREITIGKEIT,
  },
  {
    label: "Wahlprüfungsverfahren",
    value: ProceedingType.WAHLPRUEFUNGSVERFAHREN,
  },
  {
    label: "Einstweiliger Rechtsschutz",
    value: ProceedingType.EINSTWEILIGER_RECHTSSCHUTZ,
  },
  {
    label: "Sonstige Verfahren",
    value: ProceedingType.SONSTIGE_VERFAHREN,
  },
]
