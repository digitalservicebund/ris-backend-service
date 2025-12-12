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
  }

  get isReadOnly(): boolean {
    return (
      this.documentNumber != null &&
      this.fileNumber != null &&
      this.court != null &&
      this.decisionDate != null
    )
  }

  get isEmpty(): boolean {
    return RelatedPendingProceeding.fields.every((field) =>
      this.fieldIsEmpty(this[field]),
    )
  }

  equals(entry: RelatedPendingProceeding): boolean {
    return this.localId === entry.localId
  }

  private fieldIsEmpty(
    value: RelatedPendingProceeding[(typeof RelatedPendingProceeding.fields)[number]],
  ): boolean {
    return !value
  }
}
