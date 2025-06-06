import dayjs from "dayjs"
import EnsuingDecision from "./ensuingDecision"
import PreviousDecision from "./previousDecision"
import Reference from "./reference"
import ActiveCitation from "@/domain/activeCitation"
import { ContentRelatedIndexing, CoreData } from "@/domain/documentUnit"
import LegalForce from "@/domain/legalForce"
import NormReference from "@/domain/normReference"
import { PublicationStatus } from "@/domain/publicationStatus"
import SingleNorm from "@/domain/singleNorm"

export type PendingDecisionShortTexts = {
  headline?: string
  legalIssue?: string
  appellant?: string
  admissionOfAppeal?: string
  isResolved?: string
  resolutionNote?: string
  resolutionDate?: string
}
export const pendingProceedingShortTextLabels: {
  [shortTextKey in keyof Required<PendingDecisionShortTexts>]: string
} = {
  headline: "Titelzeile",
  legalIssue: "Rechtsfrage",
  appellant: "Rechtsmittelführer",
  admissionOfAppeal: "Rechtsmittelzulassung",
  isResolved: "Erledigt",
  resolutionNote: "Erledigungsvermerk",
  resolutionDate: "Erledigungsmitteilung",
}
export default class PendingProceeding {
  readonly uuid: string
  readonly id?: string
  readonly documentNumber: string = ""
  readonly status?: PublicationStatus
  public coreData: CoreData = {}
  public shortTexts: PendingDecisionShortTexts = {}
  public previousDecisions?: PreviousDecision[]
  public ensuingDecisions?: EnsuingDecision[]
  public contentRelatedIndexing: ContentRelatedIndexing = {}
  public caselawReferences?: Reference[]
  public literatureReferences?: Reference[]
  public resolutionNote?: string = ""
  public legalIssue?: string = ""
  public admissionOfAppeal?: string = ""
  public appellant?: string = ""
  public isEditable?: boolean

  static readonly requiredFields = [
    "fileNumbers",
    "court",
    "decisionDate",
    "documentType",
  ] as const

  constructor(uuid: string, data: Partial<PendingProceeding> = {}) {
    this.uuid = String(uuid)

    let rootField: keyof PendingProceeding
    for (rootField in data) {
      if (data[rootField] === null) delete data[rootField]
    }
    let coreDataField: keyof CoreData
    for (coreDataField in data.coreData) {
      if (data.coreData && data.coreData[coreDataField] === null)
        delete data.coreData[coreDataField]
    }
    let shortTextsField: keyof PendingDecisionShortTexts
    for (shortTextsField in data.shortTexts) {
      if (data.shortTexts && data.shortTexts[shortTextsField] === null)
        delete data.shortTexts[shortTextsField]
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

    if (data.caselawReferences)
      data.caselawReferences = data.caselawReferences.map(
        (reference) => new Reference({ ...reference }),
      )

    if (data.literatureReferences)
      data.literatureReferences = data.literatureReferences.map(
        (literatureReference) => new Reference({ ...literatureReference }),
      )

    Object.assign(this, data)
  }

  get renderSummary(): string {
    return [
      this.coreData.court?.label,
      this.coreData.decisionDate
        ? dayjs(this.coreData.decisionDate).format("DD.MM.YYYY")
        : null,
      this.coreData.fileNumbers ? this.coreData.fileNumbers[0] : null,
      this.coreData.documentType?.label,
    ]
      .filter(Boolean)
      .join(", ")
  }

  public isEmpty(
    value: CoreData[(typeof PendingProceeding.requiredFields)[number]],
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
