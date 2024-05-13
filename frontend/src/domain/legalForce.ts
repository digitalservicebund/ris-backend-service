export default class LegalForce {
  uuid?: string
  type?: LegalForceType
  region?: LegalForceRegion

  static readonly requiredFields = ["type", "region"] as const
  static readonly fields = ["type", "region"] as const

  constructor(data: Partial<LegalForce> = {}) {
    Object.assign(this, data)
  }

  private fieldIsEmpty(value: LegalForce[(typeof LegalForce.fields)[number]]) {
    return value === undefined || !value || Object.keys(value).length === 0
  }

  get hasMissingRequiredFields(): boolean {
    return this.missingRequiredFields.length > 0
  }

  get missingRequiredFields() {
    return LegalForce.requiredFields.filter((field) =>
      this.fieldIsEmpty(this[field]),
    )
  }
}

export type LegalForceType = {
  uuid?: string
  abbreviation: string
}

export type LegalForceRegion = {
  uuid?: string
  code?: string
  longText: string
}
