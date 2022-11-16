export type CoreData = {
  fileNumber?: string
  court?: Court
  category?: string // TODO should this be DocumentType from lookupTables.ts?
  procedure?: string
  ecli?: string
  appraisalBody?: string
  decisionDate?: string
  legalEffect?: string
  inputType?: string
  center?: string
  region?: string
}

export type Court = {
  type: string
  location: string
  label: string
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
  fileNumber?: string
}

export default class DocumentUnit {
  readonly uuid: string
  readonly id?: string
  readonly documentNumber?: string
  readonly creationtimestamp?: string

  public fileuploadtimestamp?: string
  public s3path?: string
  public filetype?: string
  public filename?: string

  public coreData: CoreData = {}
  public texts: Texts = {}
  public previousDecisions?: PreviousDecision[]

  constructor(uuid: string, data: Partial<DocumentUnit> = {}) {
    this.uuid = String(uuid)

    let rootField: keyof DocumentUnit
    for (rootField in data) {
      if (data[rootField] === null) delete data[rootField]
    }
    let coreDataField: keyof CoreData
    for (coreDataField in data.coreData) {
      if (data.coreData && data.coreData[coreDataField] === null)
        delete data.coreData[coreDataField]
    }
    let textsField: keyof Texts
    for (textsField in data.texts) {
      if (data.texts && data.texts[textsField] === null)
        delete data.texts[textsField]
    }

    Object.assign(this, data)
  }
  get hasFile(): boolean {
    return !!this.s3path
  }
}
