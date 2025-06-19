import { PublicationState } from "@/domain/publicationStatus"

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
