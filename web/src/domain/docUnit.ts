export type CoreData = {
  docketNumber?: string
  courtType?: string
  category?: string
  procedure?: string
  ecli?: string
  appraisalBody?: string
  decisionDate?: string
  courtLocation?: string
  legalEffect?: string
  receiptType?: string
  center?: string
  region?: string
}

export type Texts = {
  decisionName?: string
  headline?: string
  guidingPrinciple?: string
  headnote?: string
  tenor?: string
  reasons?: string
  caseFacts?: string
  decisionReasons?: string
}

export type PreviousDecision = {
  courtType?: string
  courtPlace?: string
  date?: string
  docketNumber?: string
}

export default class DocUnit implements CoreData, Texts {
  readonly uuid: string
  readonly id?: string
  readonly documentnumber?: string
  readonly creationtimestamp?: string

  public fileuploadtimestamp?: string
  public s3path?: string
  public filetype?: string
  public filename?: string
  public originalFileAsHTML?: string

  public docketNumber?: string
  public courtType?: string
  public category?: string
  public procedure?: string
  public ecli?: string
  public appraisalBody?: string
  public decisionDate?: string
  public courtLocation?: string
  public legalEffect?: string
  public receiptType?: string
  public center?: string
  public region?: string

  public decisionName?: string
  public headline?: string
  public guidingPrinciple?: string
  public headnote?: string
  public tenor?: string
  public reasons?: string
  public caseFacts?: string
  public decisionReasons?: string

  public previousDecisions?: PreviousDecision[]

  constructor(uuid: string, data: Partial<DocUnit> = {}) {
    this.uuid = String(uuid)

    let key: keyof DocUnit
    for (key in data) {
      if (data[key] === null) delete data[key]
    }
    Object.assign(this, data)
  }
  get coreData(): CoreData {
    return {
      docketNumber: this.docketNumber,
      courtType: this.courtType,
      category: this.category,
      procedure: this.procedure,
      ecli: this.ecli,
      appraisalBody: this.appraisalBody,
      decisionDate: this.decisionDate,
      courtLocation: this.courtLocation,
      legalEffect: this.legalEffect,
      receiptType: this.receiptType,
      center: this.center,
      region: this.region,
    }
  }
  get texts(): Texts {
    return {
      decisionName: this.decisionName,
      headline: this.headline,
      guidingPrinciple: this.guidingPrinciple,
      headnote: this.headnote,
      tenor: this.tenor,
      reasons: this.reasons,
      caseFacts: this.caseFacts,
      decisionReasons: this.decisionReasons,
    }
  }
  get hasFile(): boolean {
    return !!this.s3path
  }
}
