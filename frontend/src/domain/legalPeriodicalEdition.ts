import Reference from "./reference"
import LegalPeriodical from "@/domain/legalPeriodical"

export default class LegalPeriodicalEdition {
  static readonly fields = [
    "name",
    "prefix",
    "suffix",
    "legalPeriodical",
  ] as const

  static readonly requiredFields = [
    "name",
    "prefix",
    "legalPeriodical",
  ] as const

  uuid?: string
  name?: string
  prefix?: string
  suffix?: string
  legalPeriodical?: LegalPeriodical
  references?: Reference[]

  constructor(data: Partial<LegalPeriodicalEdition> = {}) {
    Object.assign(this, data)
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

  get isEmpty(): boolean {
    const emptyFields = LegalPeriodicalEdition.fields.filter((field) =>
      this.fieldIsEmpty(this[field]),
    )
    return emptyFields.length !== LegalPeriodicalEdition.fields.length
  }
}
