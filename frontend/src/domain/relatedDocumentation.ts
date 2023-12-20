import dayjs from "dayjs"
import { Court, DocumentType } from "./documentUnit"

export default class RelatedDocumentation {
  public uuid?: string
  public documentNumber?: string
  public court?: Court
  public decisionDate?: string
  public fileNumber?: string
  public documentType?: DocumentType
  public referenceFound?: boolean

  get hasForeignSource(): boolean {
    return this.documentNumber != null && !!this.referenceFound
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
      ...(this.fileNumber ? [this.fileNumber] : []),
      ...(this.documentType ? [this.documentType.label] : []),
      ...(this.documentNumber ? [this.documentNumber] : []),
    ].join(", ")
  }
}
