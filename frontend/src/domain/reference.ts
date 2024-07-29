import EditableListItem from "./editableListItem"

export type LegalPeriodical = {
  legalPeriodicalId?: string
  legalPeriodicalTitle?: string
  legalPeriodicalSubtitle?: string
  legalPeriodicalAbbreviation: string
}

export default class Reference implements EditableListItem {
  public uuid?: string
  citation?: string
  referenceSupplement?: string
  footnote?: string
  primaryReference?: boolean
  legalPeriodical?: LegalPeriodical

  static readonly requiredFields = [
    "legalPeriodical",
    "citation",
    "referenceSupplement",
  ] as const
  static readonly fields = [
    "legalPeriodical",
    "citation",
    "referenceSupplement",
  ] as const

  constructor(data: Partial<Reference> = {}) {
    Object.assign(this, data)
  }

  get renderDecision(): string {
    return [
      ...(this.legalPeriodical
        ? [this.legalPeriodical.legalPeriodicalAbbreviation]
        : []),
      ...(this.citation ? [this.citation] : []),
      ...(this.primaryReference ? ["amtlich"] : ["nichtamtlich"]),
    ].join(", ")
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
