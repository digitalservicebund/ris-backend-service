import { CollectiveAgreementIndustry } from "@/domain/collectiveAgreementIndustry"
import EditableListItem from "@/domain/editableListItem"

export class CollectiveAgreement implements EditableListItem {
  id?: string
  name?: string
  date?: string
  norm?: string
  industry?: CollectiveAgreementIndustry

  public newEntry?: boolean

  static readonly fields = ["name", "date", "norm", "industry"] as const

  constructor(data: Partial<CollectiveAgreement> = {}) {
    Object.assign(this, data)

    if (this.id == undefined) {
      this.id = crypto.randomUUID()
      this.newEntry = true
    } else if (data.newEntry == undefined) {
      this.newEntry = false
    }
  }

  get renderSummary(): string {
    return (
      [this.name, this.date, this.norm].filter(Boolean).join(`, `) +
      (this.industry ? ` (${this.industry?.label})` : "")
    )
  }

  get isEmpty(): boolean {
    return CollectiveAgreement.fields.every((field) =>
      this.fieldIsEmpty(this[field]),
    )
  }

  equals(entry: CollectiveAgreement): boolean {
    return this.id === entry.id
  }

  private fieldIsEmpty(
    value: CollectiveAgreement[(typeof CollectiveAgreement.fields)[number]],
  ): boolean {
    return !value
  }
}
