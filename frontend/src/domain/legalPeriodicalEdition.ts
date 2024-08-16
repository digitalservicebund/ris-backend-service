import Reference from "./reference"
import LegalPeriodical from "@/domain/legalPeriodical"

// Todo: use type instead of class?
export default class LegalPeriodicalEdition {
  static readonly fields = ["name", "prefix", "suffix"] as const

  static readonly requiredFields = ["name", "prefix", "suffix"] as const

  id?: string
  legalPeriodical?: LegalPeriodical
  name?: string
  prefix?: string
  suffix?: string
  references?: Reference[]

  constructor(data: Partial<LegalPeriodicalEdition> = {}) {
    Object.assign(this, data)
  }

  get hasMissingRequiredFields(): boolean {
    return this.missingRequiredFields.length > 0
  }

  get missingRequiredFields() {
    return LegalPeriodicalEdition.requiredFields.filter((field) =>
      this.fieldIsEmpty(this[field]),
    )
  }

  private fieldIsEmpty(
    value: LegalPeriodicalEdition[(typeof LegalPeriodicalEdition.fields)[number]],
  ): boolean {
    return !value
  }
}
