import dayjs from "dayjs"
import { Court, DocumentType } from "./documentUnit"

export default class LinkedDocumentUnit {
  public uuid?: string
  public documentNumber?: string
  public court?: Court
  public decisionDate?: string
  public fileNumber?: string
  public documentType?: DocumentType
  public dataSource?:
    | "NEURIS"
    | "MIGRATION"
    | "PROCEEDING_DECISION"
    | "ACTIVE_CITATION"

  constructor(data: Partial<LinkedDocumentUnit> = {}) {
    Object.assign(this, data)
  }

  public isLinked<Type extends LinkedDocumentUnit>(
    localDecisions: Type[] | undefined
  ): boolean {
    if (!localDecisions) return false

    return localDecisions.some(
      (localDecision) => localDecision.uuid == this.uuid
    )
  }

  public isDocUnit(): boolean {
    return this.dataSource === "NEURIS" || this.dataSource === "MIGRATION"
  }

  get renderDecision(): string {
    return [
      ...(this.court?.label ? [`${this.court?.label}`] : []),
      ...(this.decisionDate
        ? [dayjs(this.decisionDate).format("DD.MM.YYYY")]
        : []),
      ...(this.documentType ? [this.documentType.label] : []),
      ...(this.fileNumber ? [this.fileNumber] : []),
      ...(this.documentNumber && this.isDocUnit() ? [this.documentNumber] : []),
    ].join(", ")
  }
}
