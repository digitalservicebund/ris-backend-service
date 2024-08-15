import Reference, { LegalPeriodical } from "./reference"

// Todo: use type instead of class?
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
