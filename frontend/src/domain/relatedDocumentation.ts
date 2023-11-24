import dayjs from "dayjs"
import { Court, DocumentType } from "./documentUnit"

export default class RelatedDocumentation {
  public uuid?: string
  public documentNumber?: string
  public court?: Court
  public decisionDate?: string
  public fileNumber?: string
  public documentType?: DocumentType
  public referencedDocumentationUnitId?: string

  get hasForeignSource(): boolean {
    return (
      this.documentNumber != null && this.referencedDocumentationUnitId != null
    )
  }

  constructor(data: Partial<RelatedDocumentation> = {}) {
    Object.assign(this, data)
  }

  public isLinkedWith<Type extends RelatedDocumentation>(
    localDecisions: Type[] | undefined,
  ): boolean {
    if (!localDecisions) return false

    return localDecisions.some(
      (localDecision) => localDecision.documentNumber == this.documentNumber,
    )
  }

  get renderDecision(): string {
    return [
      ...(this.court ? [`${this.court?.label}`] : []),
      ...(this.decisionDate
        ? [dayjs(this.decisionDate).format("DD.MM.YYYY")]
        : []),
      ...(this.documentType ? [this.documentType.label] : []),
      ...(this.fileNumber ? [this.fileNumber] : []),
      ...(this.documentNumber && this.hasForeignSource
        ? [this.documentNumber]
        : []),
    ].join(", ")
  }
}
