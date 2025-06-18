import ActiveCitation from "./activeCitation"
import DocumentationOffice from "./documentationOffice"
import { FieldOfLaw } from "./fieldOfLaw"
import NormReference from "./normReference"
import Reference from "./reference"
import { Court } from "@/domain/court"
import { DocumentType } from "@/domain/documentType"
import ParticipatingJudge from "@/domain/participatingJudge"
import { Procedure } from "@/domain/procedure"
import { PublicationState } from "@/domain/publicationStatus"

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

export enum InboxStatus {
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
  DOCUMENTION_UNIT = "DOCUMENTION_UNIT",
  PENDING_PROCEEDING = "PENDING_PROCEEDING",
}
