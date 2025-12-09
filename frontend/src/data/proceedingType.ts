import { ProceedingType, ProceedingTypeLabels } from "@/domain/objectValue"

export const proceedingType = [
  {
    label: ProceedingTypeLabels.VERFASSUNGSBESCHWERDE,
    value: ProceedingType.VERFASSUNGSBESCHWERDE,
  },
  {
    label: ProceedingTypeLabels.ORGANSTREITVERFAHREN,
    value: ProceedingType.ORGANSTREITVERFAHREN,
  },
  {
    label: ProceedingTypeLabels.KONKRETE_NORMENKONTROLLE,
    value: ProceedingType.KONKRETE_NORMENKONTROLLE,
  },
  {
    label: ProceedingTypeLabels.ABSTRAKTE_NORMENKONTROLLE,
    value: ProceedingType.ABSTRAKTE_NORMENKONTROLLE,
  },
  {
    label: ProceedingTypeLabels.BUND_LAENDER_STREITIGKEIT,
    value: ProceedingType.BUND_LAENDER_STREITIGKEIT,
  },
  {
    label: ProceedingTypeLabels.WAHLPRUEFUNGSVERFAHREN,
    value: ProceedingType.WAHLPRUEFUNGSVERFAHREN,
  },
  {
    label: ProceedingTypeLabels.EINSTWEILIGER_RECHTSSCHUTZ,
    value: ProceedingType.EINSTWEILIGER_RECHTSSCHUTZ,
  },
  {
    label: ProceedingTypeLabels.SONSTIGE_VERFAHREN,
    value: ProceedingType.SONSTIGE_VERFAHREN,
  },
]
