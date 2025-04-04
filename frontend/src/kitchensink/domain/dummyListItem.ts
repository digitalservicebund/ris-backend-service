import EditableListItem from "@/domain/editableListItem"

export default class DummyListItem implements EditableListItem {
  public text?: string
  public uuid?: string

  constructor(data: Partial<DummyListItem> = {}) {
    Object.assign(this, data)
    if (this.uuid == undefined) {
      this.uuid = crypto.randomUUID()
    }
  }

  get id() {
    return this.uuid
  }

  get renderSummary(): string {
    return this.text || "default text"
  }

  get isEmpty(): boolean {
    return !this.text
  }

  equals(entry: DummyListItem): boolean {
    return this.id === entry.id
  }
}
