import dayjs from "dayjs"
import DocumentationOffice from "./documentationOffice"
import { Court } from "@/domain/court"
import { DocumentType } from "@/domain/documentType"

import {
  Label,
  PublicationState,
  PublicationStatus,
} from "@/domain/publicationStatus"

export default class RelatedDocumentation {
  public uuid?: string
  public newEntry: boolean
  public documentNumber?: string
  public status?: PublicationStatus
  public deviatingFileNumber?: string
  public court?: Court
  public decisionDate?: string
  public fileNumber?: string
  public documentType?: DocumentType
  public createdByReference?: string
  public documentationOffice?: DocumentationOffice
  public creatingDocOffice?: DocumentationOffice
  public hasPreviewAccess?: boolean

  get hasForeignSource(): boolean {
    return this.documentNumber != null
  }

  constructor(data: Partial<RelatedDocumentation> = {}) {
    Object.assign(this, data)

    this.newEntry = false
    if (data.uuid == undefined) {
      this.newEntry = true
    }
  }

  public isLinkedWith<Type extends RelatedDocumentation>(
    localDecisions: Type[] | undefined,
  ): boolean {
    if (!localDecisions) return false

    return localDecisions.some(
      (localDecision) => localDecision.documentNumber == this.documentNumber,
    )
  }

  public static getStatusLabel(status: PublicationStatus) {
    if (!status) return ""

    switch (status.publicationStatus) {
      case PublicationState.PUBLISHED:
        return Label.PUBLISHED
      case PublicationState.UNPUBLISHED:
        return Label.UNPUBLISHED
      case PublicationState.PUBLISHING:
        return Label.PUBLISHING
      case PublicationState.DUPLICATED:
        return Label.DUPLICATED
      case PublicationState.LOCKED:
        return Label.LOCKED
      case PublicationState.DELETING:
        return Label.DELETING
      case PublicationState.EXTERNAL_HANDOVER_PENDING:
        return Label.EXTERNAL_HANDOVER_PENDING
      default:
        return ""
    }
  }

  get renderSummary(): string {
    return [
      ...(this.court ? [`${this.court?.label}`] : []),
      ...(this.decisionDate
        ? [dayjs(this.decisionDate).format("DD.MM.YYYY")]
        : []),
      ...(this.fileNumber ? [this.fileNumber] : []),
      ...(this.documentType?.label ? [this.documentType.label] : []),
    ].join(", ")
  }
}
