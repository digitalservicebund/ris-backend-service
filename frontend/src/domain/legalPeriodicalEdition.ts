import LegalPeriodical from "./legalPeriodical"
import Reference from "./reference"

export default class LegalPeriodicalEdition {
  id?: string
  legalPeriodical?: LegalPeriodical
  name?: string
  prefix?: string
  suffix?: string
  references?: Reference[]

  constructor(data: Partial<LegalPeriodicalEdition> = {}) {
    Object.assign(this, data)
  }
}