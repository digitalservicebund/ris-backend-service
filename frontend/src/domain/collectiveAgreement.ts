import { CollectiveAgreementIndustry } from "@/domain/collectiveAgreementIndustry"
import EditableListItem from "@/domain/editableListItem"

export class CollectiveAgreement implements EditableListItem {
  public id?: string // BE only
  public localId: string // FE only
  public name?: string
  public date?: string
  public norm?: string
  public industry?: CollectiveAgreementIndustry

  constructor(data: Partial<CollectiveAgreement> = {}) {
    Object.assign(this, data)
    this.localId = data.localId ?? crypto.randomUUID()
  }

  static readonly fields = ["name", "date", "norm", "industry"] as const

  get renderSummary(): string {
    return (
      [this.name, this.date, this.norm].filter(Boolean).join(`, `) +
      (this.industry ? ` (${this.industry?.label})` : "")
    )
  }

  get isEmpty(): boolean {
    return this.name == null || this.name.length === 0
  }

  equals(entry: CollectiveAgreement): boolean {
    return this.localId === entry.localId
  }
}
