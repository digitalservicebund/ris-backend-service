export default class Reference {
  citation?: string
  referenceSupplement?: string
  footnote?: string
  primaryReference?: boolean
  legalPeriodicalId?: string
  legalPeriodicalTitle?: string
  legalPeriodicalSubtitle?: string
  legalPeriodicalAbbreviation?: string

  static readonly fields = ["legalPeriodicalAbbreviation", "citation"] as const

  constructor(data: Partial<Reference> = {}) {
    Object.assign(this, data)
  }

  get renderDecision(): string {
    return [
      ...(this.legalPeriodicalAbbreviation
        ? [this.legalPeriodicalAbbreviation]
        : []),
      ...(this.citation ? [this.citation] : []),
      ...(this.primaryReference ? ["amtlich"] : ["nichtamtlich"]),
    ].join(", ")
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
