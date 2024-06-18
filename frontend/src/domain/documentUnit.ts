import ActiveCitation from "./activeCitation"
import DocumentationOffice from "./documentationOffice"
import DocumentUnitListEntry from "./documentUnitListEntry"
import EnsuingDecision from "./ensuingDecision"
import { FieldOfLaw } from "./fieldOfLaw"
import NormReference from "./normReference"
import PreviousDecision from "./previousDecision"
import SingleNorm from "./singleNorm"
import Attachment from "@/domain/attachment"
import LegalForce from "@/domain/legalForce"

export type CoreData = {
  fileNumbers?: string[]
  deviatingFileNumbers?: string[]
  court?: Court
  deviatingCourts?: string[]
  documentType?: DocumentType
  procedure?: Procedure
  previousProcedures?: string[]
  ecli?: string
  deviatingEclis?: string[]
  appraisalBody?: string
  decisionDate?: string
  deviatingDecisionDates?: string[]
  legalEffect?: string
  inputTypes?: string[]
  documentationOffice?: DocumentationOffice
  region?: string
  yearOfDispute?: string[]
  leadingDecisionNormReferences?: string[]
}

export type ContentRelatedIndexing = {
  keywords?: string[]
  norms?: NormReference[]
  activeCitations?: ActiveCitation[]
  fieldsOfLaw?: FieldOfLaw[]
}

export type DocumentType = {
  uuid?: string
  jurisShortcut: string
  label: string
}

export type Court = {
  type?: string
  location?: string
  label: string
  revoked?: string
}

export type Procedure = {
  id?: string
  label: string
  documentUnitCount: number
  createdAt: string
  documentUnits?: DocumentUnitListEntry[]
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

export enum PublicationState {
  PUBLISHED = "PUBLISHED",
  UNPUBLISHED = "UNPUBLISHED",
  PUBLISHING = "PUBLISHING",
  DUPLICATED = "DUPLICATED",
  LOCKED = "LOCKED",
  DELETING = "DELETING",
}

export type PublicationStatus = {
  publicationStatus?: PublicationState
  withError?: boolean
}

export default class DocumentUnit {
  readonly uuid: string
  readonly id?: string
  readonly documentNumber?: string
  readonly status?: PublicationStatus
  public attachments: Attachment[] = []
  public coreData: CoreData = {}
  public texts: Texts = {}
  public previousDecisions?: PreviousDecision[]
  public ensuingDecisions?: EnsuingDecision[]
  public contentRelatedIndexing: ContentRelatedIndexing = {}
  public borderNumbers: string[] = []
  public note: string = ""

  static readonly requiredFields = [
    "fileNumbers",
    "court",
    "decisionDate",
    "legalEffect",
    "documentType",
  ] as const

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

    if (data.previousDecisions)
      data.previousDecisions = data.previousDecisions.map(
        (decision) => new PreviousDecision({ ...decision }),
      )

    if (data.ensuingDecisions)
      data.ensuingDecisions = data.ensuingDecisions.map(
        (decision) => new EnsuingDecision({ ...decision }),
      )

    if (data.contentRelatedIndexing?.norms)
      data.contentRelatedIndexing.norms = data.contentRelatedIndexing.norms.map(
        (norm) =>
          new NormReference({
            ...norm,
            singleNorms: norm.singleNorms?.map(
              (norm) =>
                new SingleNorm({
                  ...norm,
                  legalForce: norm.legalForce
                    ? new LegalForce({ ...norm.legalForce })
                    : undefined,
                }),
            ),
          }),
      )

    if (data.contentRelatedIndexing?.activeCitations)
      data.contentRelatedIndexing.activeCitations =
        data.contentRelatedIndexing.activeCitations.map(
          (activeCitations) => new ActiveCitation({ ...activeCitations }),
        )

    if (data.attachments != undefined && data.attachments.length > 0) {
      data.attachments.map(
        (attachment: Attachment) => new Attachment({ ...attachment }),
      )
    }

    Object.assign(this, data)
  }

  get hasAttachments(): boolean {
    return this.attachments && this.attachments.length > 0
  }

  get missingRequiredFields() {
    return DocumentUnit.requiredFields.filter((field) =>
      this.isEmpty(this.coreData[field]),
    )
  }

  public static isRequiredField(fieldName: string) {
    return DocumentUnit.requiredFields.some(
      (requiredfieldName) => requiredfieldName === fieldName,
    )
  }

  public isEmpty(
    value: CoreData[(typeof DocumentUnit.requiredFields)[number]],
  ) {
    if (value === undefined || !value) {
      return true
    }
    if (value instanceof Array && value.length === 0) {
      return true
    }
    if (typeof value === "object" && "location" in value && "type" in value) {
      return value.location === "" && value.type === ""
    }
    return false
  }
}
