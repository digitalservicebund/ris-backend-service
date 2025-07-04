import dayjs from "dayjs"
import ActiveCitation from "@/domain/activeCitation"
import Attachment from "@/domain/attachment"
import { ContentRelatedIndexing } from "@/domain/contentRelatedIndexing"
import { CoreData } from "@/domain/coreData"
import { Kind } from "@/domain/documentationUnitKind"
import EnsuingDecision from "@/domain/ensuingDecision"
import LegalForce from "@/domain/legalForce"
import { ManagementData } from "@/domain/managementData"
import NormReference from "@/domain/normReference"
import ParticipatingJudge from "@/domain/participatingJudge"
import PreviousDecision from "@/domain/previousDecision"
import { PublicationStatus } from "@/domain/publicationStatus"
import Reference from "@/domain/reference"
import SingleNorm from "@/domain/singleNorm"

export enum InboxStatus {
  EXTERNAL_HANDOVER,
  EU,
}

export type ShortTexts = {
  decisionName?: string
  headline?: string
  guidingPrinciple?: string
  headnote?: string
  otherHeadnote?: string
}
export const shortTextLabels: {
  [shortTextKey in keyof Required<ShortTexts>]: string
} = {
  decisionName: "Entscheidungsname",
  headline: "Titelzeile",
  guidingPrinciple: "Leitsatz",
  headnote: "Orientierungssatz",
  otherHeadnote: "Sonstiger Orientierungssatz",
}
export type LongTexts = {
  tenor?: string
  reasons?: string
  caseFacts?: string
  decisionReasons?: string
  dissentingOpinion?: string
  participatingJudges?: ParticipatingJudge[]
  otherLongText?: string
  outline?: string
}
export const longTextLabels: {
  [longTextKey in keyof Required<LongTexts>]: string
} = {
  tenor: "Tenor",
  reasons: "Gründe",
  caseFacts: "Tatbestand",
  decisionReasons: "Entscheidungsgründe",
  dissentingOpinion: "Abweichende Meinung",
  participatingJudges: "Mitwirkende Richter",
  otherLongText: "Sonstiger Langtext",
  outline: "Gliederung",
}
export const contentRelatedIndexingLabels = {
  keywords: "Schlagwörter",
  fieldsOfLaw: "Sachgebiete",
  norms: "Normen",
}
export const allLabels = {
  caselawReferences: "Rechtsprechungsfundstellen",
  literatureReferences: "Literaturfundstellen",
  activeCitations: "Aktivzitierung",
  ...contentRelatedIndexingLabels,
  ...shortTextLabels,
  ...longTextLabels,
}

export class Decision {
  readonly uuid: string
  readonly id?: string
  readonly documentNumber: string = ""
  readonly status?: PublicationStatus
  readonly kind = Kind.DECISION
  public version: number = 0
  public attachments: Attachment[] = []
  public coreData: CoreData = {}
  public shortTexts: ShortTexts = {}
  public longTexts: LongTexts = {}
  public previousDecisions?: PreviousDecision[]
  public ensuingDecisions?: EnsuingDecision[]
  public contentRelatedIndexing: ContentRelatedIndexing = {}
  public note: string = ""
  public caselawReferences?: Reference[]
  public literatureReferences?: Reference[]
  public isEditable: boolean = false
  public managementData: ManagementData = {
    borderNumbers: [],
    duplicateRelations: [],
  }
  public inboxStatus?: InboxStatus

  static readonly requiredFields = [
    "fileNumbers",
    "court",
    "decisionDate",
    "legalEffect",
    "documentType",
  ] as const

  // prettier-ignore
  constructor(uuid: string, data: Partial<Decision> = {}) { // NOSONAR: Ignore Cognitive Complexity
    this.uuid = String(uuid)

    let rootField: keyof Decision
    for (rootField in data) {
      if (data[rootField] === null) delete data[rootField]
    }
    let coreDataField: keyof CoreData
    for (coreDataField in data.coreData) {
      if (data.coreData && data.coreData[coreDataField] === null)
        delete data.coreData[coreDataField]
    }
    let shortTextsField: keyof ShortTexts
    for (shortTextsField in data.shortTexts) {
      if (data.shortTexts && data.shortTexts[shortTextsField] === null)
        delete data.shortTexts[shortTextsField]
    }

    let longTextsField: keyof LongTexts
    for (longTextsField in data.longTexts) {
      if (data.longTexts && data.longTexts[longTextsField] === null)
        delete data.longTexts[longTextsField]
    }

    let managementDataField: keyof ManagementData
    for (managementDataField in data.managementData) {
      if (
        data.managementData &&
        data.managementData[managementDataField] === null
      )
        delete data.managementData[managementDataField]
    }

    if (data.longTexts?.participatingJudges)
      data.longTexts.participatingJudges =
        data.longTexts.participatingJudges.map(
          (judge) => new ParticipatingJudge({ ...judge }),
        )

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
      data.attachments = data.attachments.map(
        (attachment: Attachment) => new Attachment({ ...attachment }),
      )
    }

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

  get hasAttachments(): boolean {
    return this.attachments && this.attachments.length > 0
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

  get missingRequiredFields() {
    return Decision.requiredFields.filter((field) =>
      this.isEmpty(this.coreData[field]),
    )
  }

  public static isRequiredField(fieldName: string) {
    return Decision.requiredFields.some(
      (requiredfieldName) => requiredfieldName === fieldName,
    )
  }

  public isEmpty(value: CoreData[(typeof Decision.requiredFields)[number]]) {
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
