import EditableListItem from "@/domain/editableListItem"

export default class DummyListItem implements EditableListItem {
  public localId: string // FE only
  public text?: string

  constructor(data: Partial<DummyListItem> = {}) {
    Object.assign(this, data)
    this.localId ??= crypto.randomUUID()
  }

  get renderSummary(): string {
    return this.text || "default text"
  }

  get isEmpty(): boolean {
    return !this.text
  }

  equals(entry: DummyListItem): boolean {
    return this.localId === entry.localId
  }
}
