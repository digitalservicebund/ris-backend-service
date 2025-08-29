import dayjs from "dayjs"
import DocumentationUnitProcessStep from "./documentationUnitProcessStep"
import EnsuingDecision from "./ensuingDecision"
import PreviousDecision from "./previousDecision"
import Reference from "./reference"
import ActiveCitation from "@/domain/activeCitation"
import { ContentRelatedIndexing } from "@/domain/contentRelatedIndexing"
import { CoreData } from "@/domain/coreData"
import { Kind } from "@/domain/documentationUnitKind"
import LegalForce from "@/domain/legalForce"
import { ManagementData } from "@/domain/managementData"
import NormReference from "@/domain/normReference"
import { PortalPublicationStatus } from "@/domain/portalPublicationStatus"
import ProcessStep from "@/domain/processStep"
import { PublicationStatus } from "@/domain/publicationStatus"
import SingleNorm from "@/domain/singleNorm"

export const pendingProceedingLabels = {
  headline: "Titelzeile",
  legalIssue: "Rechtsfrage",
  appellant: "Rechtsmittelführer",
  admissionOfAppeal: "Rechtsmittelzulassung",
  isResolved: "Erledigt",
  resolutionNote: "Erledigungsvermerk",
  resolutionDate: "Erledigungsmitteilung",
}

export type PendingProceedingShortTexts = {
  headline?: string
  resolutionNote?: string
  legalIssue?: string
  admissionOfAppeal?: string
  appellant?: string
}

export default class PendingProceeding {
  readonly uuid: string
  readonly id?: string
  readonly documentNumber: string = ""
  readonly status?: PublicationStatus
  readonly portalPublicationStatus: PortalPublicationStatus =
    PortalPublicationStatus.UNPUBLISHED
  readonly kind = Kind.PENDING_PROCEEDING
  public version: number = 0
  public coreData: CoreData = {}
  public shortTexts: PendingProceedingShortTexts = {}
  public previousDecisions?: PreviousDecision[]
  public ensuingDecisions?: EnsuingDecision[]
  public contentRelatedIndexing: ContentRelatedIndexing = {}
  public caselawReferences?: Reference[]
  public literatureReferences?: Reference[]
  public managementData: ManagementData = {
    borderNumbers: [],
    duplicateRelations: [],
  }
  public currentDocumentationUnitProcessStep?: DocumentationUnitProcessStep
  public previousProcessStep?: ProcessStep
  public processSteps?: DocumentationUnitProcessStep[]

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
    let shortTextsField: keyof PendingProceedingShortTexts
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

    let managementDataField: keyof ManagementData
    for (managementDataField in data.managementData) {
      if (
        data.managementData &&
        data.managementData[managementDataField] === null
      )
        delete data.managementData[managementDataField]
    }

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
