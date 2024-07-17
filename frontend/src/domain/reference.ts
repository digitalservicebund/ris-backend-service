export default class Reference {
  citation?: string
  referenceSupplement?: string
  footnote?: string
  primaryReference?: boolean
  legalPeriodicalId?: string
  legalPeriodicalTitle?: string
  legalPeriodicalSubtitle?: string
  legalPeriodicalAbbreviation?: string

  constructor(data: Partial<Reference> = {}) {
    Object.assign(this, data)
  }
}
