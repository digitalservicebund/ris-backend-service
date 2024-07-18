import EditableListItem from "@/domain/editableListItem"

export default class DummyListItem implements EditableListItem {
  public text?: string
  public uuid?: string

  constructor(data: Partial<DummyListItem> = {}) {
    Object.assign(this, data)
    this.uuid = crypto.randomUUID()
  }

  get id() {
    return this.uuid
  }

  get renderDecision(): string {
    return this.text ? this.text : "default text"
  }

  get isEmpty(): boolean {
    if (!this.text) return true
    return false
  }

  equals(entry: DummyListItem): boolean {
    return this.id === entry.id
  }
}
