import { Court, DocumentType } from "./documentUnit"

export default class LinkedDocumentUnit {
  public uuid?: string
  public documentNumber?: string
  public court?: Court
  public decisionDate?: string
  public fileNumber?: string
  public documentType?: DocumentType

  constructor(data: Partial<LinkedDocumentUnit> = {}) {
    Object.assign(this, data)
  }

  isLinked<Type extends LinkedDocumentUnit>(
    localDecisions: Type[] | undefined
  ): boolean {
    if (!localDecisions) return false

    return localDecisions.some(
      (localDecision) => localDecision.uuid == this.uuid
    )
  }
}
