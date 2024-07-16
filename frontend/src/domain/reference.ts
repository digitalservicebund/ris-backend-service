import LegalPeriodical from "./legalPeriodical"

export default class Reference {
  rank?: number
  citation?: string
  referenceSupplement?: string
  footnote?: string
  legalPeriodical?: LegalPeriodical

  constructor(data: Partial<Reference> = {}) {
    Object.assign(this, data)
  }
}
