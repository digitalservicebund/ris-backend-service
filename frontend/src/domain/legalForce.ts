export default class LegalForce {
  uuid?: string
  type?: LegalForceType
  region?: LegalForceRegion

  static readonly requiredFields = ["type", "region"] as const
  static readonly fields = ["type", "region"] as const

  private fieldIsEmpty(value: LegalForce[(typeof LegalForce.fields)[number]]) {
    if (
      value === undefined ||
      !value ||
      value === null ||
      Object.keys(value).length === 0
    ) {
      return true
    }

    return false
  }

  get hasMissingRequiredFields(): boolean {
    console.log("has missing req fields")
    return this.missingRequiredFields.length > 0
  }

  get missingRequiredFields() {
    return LegalForce.requiredFields.filter((field) => {
      console.log("field is empty: " + this.fieldIsEmpty(this[field]))
      this.fieldIsEmpty(this[field])
    })
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
