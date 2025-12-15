import EditableListItem from "./editableListItem"

export type CurrencyCode = {
  id: string
  label: string
}

export enum ProceedingType {
  VERFASSUNGSBESCHWERDE = "VERFASSUNGSBESCHWERDE",
  ORGANSTREITVERFAHREN = "ORGANSTREITVERFAHREN",
  KONKRETE_NORMENKONTROLLE = "KONKRETE_NORMENKONTROLLE",
  ABSTRAKTE_NORMENKONTROLLE = "ABSTRAKTE_NORMENKONTROLLE",
  BUND_LAENDER_STREITIGKEIT = "BUND_LAENDER_STREITIGKEIT",
  WAHLPRUEFUNGSVERFAHREN = "WAHLPRUEFUNGSVERFAHREN",
  EINSTWEILIGER_RECHTSSCHUTZ = "EINSTWEILIGER_RECHTSSCHUTZ",
  SONSTIGE_VERFAHREN = "SONSTIGE_VERFAHREN",
}

export const ProceedingTypeLabels: Record<ProceedingType, string> = {
  [ProceedingType.VERFASSUNGSBESCHWERDE]: "Verfassungsbeschwerde",
  [ProceedingType.ORGANSTREITVERFAHREN]: "Organstreitverfahren",
  [ProceedingType.KONKRETE_NORMENKONTROLLE]: "Konkrete Normenkontrolle",
  [ProceedingType.ABSTRAKTE_NORMENKONTROLLE]: "Abstrakte Normenkontrolle",
  [ProceedingType.BUND_LAENDER_STREITIGKEIT]: "Bund-Länder-Streitigkeit",
  [ProceedingType.WAHLPRUEFUNGSVERFAHREN]: "Wahlprüfungsverfahren",
  [ProceedingType.EINSTWEILIGER_RECHTSSCHUTZ]: "Einstweiliger Rechtsschutz",
  [ProceedingType.SONSTIGE_VERFAHREN]: "Sonstige Verfahren",
}

export default class ObjectValue implements EditableListItem {
  public id?: string
  public localId: string
  public newEntry?: boolean
  public amount?: number
  public currencyCode?: CurrencyCode
  public proceedingType?: ProceedingType

  static readonly requiredFields = ["amount", "currencyCode"] as const

  static readonly fields = ["amount", "currencyCode", "proceedingType"] as const

  constructor(data: Partial<ObjectValue> = {}) {
    Object.assign(this, data)
    this.localId = data.localId ?? crypto.randomUUID()
  }

  get renderSummary(): string {
    return (
      [this.amount?.toLocaleString("de-DE"), this.currencyCode?.label]
        .filter(Boolean)
        .join(` `) +
      (this.proceedingType
        ? `, ${ProceedingTypeLabels[this.proceedingType]}`
        : "")
    )
  }

  get hasMissingRequiredFields(): boolean {
    return this.missingRequiredFields.length > 0
  }

  get missingRequiredFields() {
    return ObjectValue.requiredFields.filter((field) =>
      this.fieldIsEmpty(this[field]),
    )
  }

  get isEmpty(): boolean {
    return ObjectValue.fields.every((field) => this.fieldIsEmpty(this[field]))
  }

  private fieldIsEmpty(
    value: ObjectValue[(typeof ObjectValue.fields)[number]],
  ): boolean {
    return !value
  }
}
