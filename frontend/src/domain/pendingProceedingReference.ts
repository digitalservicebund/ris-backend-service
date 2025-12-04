import dayjs from "dayjs"
import EditableListItem from "./editableListItem"
import RelatedDocumentation from "./relatedDocumentation"

export default class RelatedPendingProceeding
  extends RelatedDocumentation
  implements EditableListItem
{
  static readonly fields = ["documentNumber", "fileNumber"] as const

  constructor(data: Partial<RelatedPendingProceeding> = {}) {
    super()
    Object.assign(this, data)
    if (this.uuid == undefined) {
      this.uuid = crypto.randomUUID()
      this.newEntry = true
    }
  }

  get renderSummary(): string {
    return [
      ...(this.court ? [`${this.court?.label}`] : []),
      ...(this.decisionDate
        ? [dayjs(this.decisionDate).format("DD.MM.YYYY")]
        : []),
      ...(this.fileNumber ? [this.fileNumber] : []),
    ].join(", ")
  }

  get id() {
    return this.uuid
  }

  get isEmpty(): boolean {
    return RelatedPendingProceeding.fields.every((field) =>
      this.fieldIsEmpty(this[field]),
    )
  }

  get isReadOnly(): boolean {
    return false
  }

  equals(entry: RelatedPendingProceeding): boolean {
    return this.id === entry.id
  }

  private fieldIsEmpty(
    value: RelatedPendingProceeding[(typeof RelatedPendingProceeding.fields)[number]],
  ): boolean {
    return !value
  }
}
