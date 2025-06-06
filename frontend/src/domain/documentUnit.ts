import dayjs from "dayjs"
import ActiveCitation from "./activeCitation"
import DocumentationOffice from "./documentationOffice"
import EnsuingDecision from "./ensuingDecision"
import { FieldOfLaw } from "./fieldOfLaw"
import NormReference from "./normReference"
import PreviousDecision from "./previousDecision"
import Reference from "./reference"
import SingleNorm from "./singleNorm"
import Attachment from "@/domain/attachment"
import { DocumentType } from "@/domain/documentType"
import LegalForce from "@/domain/legalForce"
import ParticipatingJudge from "@/domain/participatingJudge"
import { Procedure } from "@/domain/procedure"
import { PublicationState, PublicationStatus } from "@/domain/publicationStatus"

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
  creatingDocOffice?: DocumentationOffice
  yearsOfDispute?: string[]
  leadingDecisionNormReferences?: string[]
  source?: Source
  isResolved?: boolean
  resolutionDate?: string
}

export enum SourceValue {
  UnaufgefordertesOriginal = "O",
  AngefordertesOriginal = "A",
  Zeitschrift = "Z",
  Email = "E",
  LaenderEuGH = "L",
  Sonstige = "S",
}

export type Source = {
  value?: SourceValue
  reference?: Reference
  sourceRawValue?: string
}

export enum InboxType {
  EXTERNAL_HANDOVER,
  EU,
}

export type ContentRelatedIndexing = {
  collectiveAgreements?: string[]
  dismissalTypes?: string[]
  dismissalGrounds?: string[]
  keywords?: string[]
  norms?: NormReference[]
  activeCitations?: ActiveCitation[]
  fieldsOfLaw?: FieldOfLaw[]
  jobProfiles?: string[]
  hasLegislativeMandate?: boolean
}

export type Court = {
  type?: string
  location?: string
  label: string
  revoked?: string
  jurisdictionType?: string
  region?: string
  responsibleDocOffice?: DocumentationOffice
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

export enum DuplicateRelationStatus {
  PENDING = "PENDING",
  IGNORED = "IGNORED",
}

export type DuplicateRelation = {
  documentNumber: string
  status: DuplicateRelationStatus
  isJdvDuplicateCheckActive: boolean
  courtLabel?: string
  decisionDate?: string
  fileNumber?: string
  documentType?: string
  publicationStatus?: PublicationState
}

export type ManagementData = {
  scheduledPublicationDateTime?: string
  scheduledByEmail?: string
  duplicateRelations: DuplicateRelation[]
  borderNumbers: string[]
  lastUpdatedAtDateTime?: string
  lastUpdatedByName?: string
  lastUpdatedByDocOffice?: string
  createdAtDateTime?: string
  createdByName?: string
  createdByDocOffice?: string
  firstPublishedAtDateTime?: string
}

export type DocumentationUnitParameters = {
  documentationOffice?: DocumentationOffice
  documentType?: DocumentType
  decisionDate?: string
  fileNumber?: string
  court?: Court
  reference?: Reference
}

export type DocumentUnitSearchParameter =
  | "documentNumber"
  | "fileNumber"
  | "publicationStatus"
  | "publicationDate"
  | "scheduledOnly"
  | "courtType"
  | "courtLocation"
  | "decisionDate"
  | "decisionDateEnd"
  | "withError"
  | "myDocOfficeOnly"
  | "withDuplicateWarning"

export type EurlexParameters = {
  documentationOffice: DocumentationOffice
  celexNumbers: string[]
}
export enum Kind {
  DOCUMENT_UNIT = "DOCUMENT_UNIT",
  PENDING_PROCEEDING = "PENDING_PROCEEDING",
}

export default class DocumentUnit {
  readonly uuid: string
  readonly id?: string
  readonly documentNumber: string = ""
  readonly status?: PublicationStatus
  readonly kind = Kind.DOCUMENT_UNIT
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
