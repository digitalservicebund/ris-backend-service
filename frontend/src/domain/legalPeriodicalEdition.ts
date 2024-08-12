import DocumentUnit from "./documentUnit"
import LegalPeriodical from "./legalPeriodical"

export default class LegalPeriodicalEdition {
  uuid?: string
  legalPeriodical?: LegalPeriodical
  editionName?: string
  prefix?: string
  suffix?: string
  documentationUnits?: DocumentUnit[]

  constructor(data: Partial<LegalPeriodicalEdition> = {}) {
    Object.assign(this, data)
  }
}
