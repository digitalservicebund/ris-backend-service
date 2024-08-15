import EditableListItem from "./editableListItem"

// Todo: LegalPeriodical duplicate with domain/legalPeriodical class
export type LegalPeriodical = {
  legalPeriodicalId?: string
  legalPeriodicalTitle?: string
  legalPeriodicalSubtitle?: string
  legalPeriodicalAbbreviation: string
  primaryReference?: boolean
  citationStyle?: string
}

export default class Reference implements EditableListItem {
  public uuid?: string
  citation?: string
  referenceSupplement?: string
  footnote?: string
  legalPeriodical?: LegalPeriodical
  legalPeriodicalRawValue?: string

  static readonly requiredFields = ["legalPeriodical", "citation"] as const
  static readonly fields = [
    "legalPeriodical",
    "citation",
    "referenceSupplement",
  ] as const

  constructor(data: Partial<Reference> = {}) {
    Object.assign(this, data)
    if (this.uuid == undefined) {
      this.uuid = crypto.randomUUID()
    }
  }

  get renderDecision(): string {
    const parts = [
      ...(this.legalPeriodical
        ? [this.legalPeriodical.legalPeriodicalAbbreviation]
        : [this.legalPeriodicalRawValue]),
      ...(this.citation && this.referenceSupplement
        ? [`${this.citation} (${this.referenceSupplement})`]
        : [this.citation]),
    ]
    return parts.join(" ")
  }

  get hasMissingRequiredFields(): boolean {
    return this.missingRequiredFields.length > 0
  }

  get missingRequiredFields() {
    return Reference.requiredFields.filter((field) =>
      this.fieldIsEmpty(this[field]),
    )
  }

  get id() {
    return this.uuid
  }

  equals(entry: Reference): boolean {
    return this.id === entry.id
  }

  get isEmpty(): boolean {
    let isEmpty = true

    Reference.fields.map((key) => {
      if (!this.fieldIsEmpty(this[key])) {
        isEmpty = false
      }
    })
    return isEmpty
  }

  private fieldIsEmpty(value: Reference[(typeof Reference.fields)[number]]) {
    return value === undefined || !value || Object.keys(value).length === 0
  }
}
