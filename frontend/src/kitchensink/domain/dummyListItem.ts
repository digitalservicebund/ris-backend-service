import EditableListItem from "@/domain/editableListItem"

export default class DummyListItem implements EditableListItem {
  public text?: string

  constructor(data: Partial<DummyListItem> = {}) {
    Object.assign(this, data)
  }

  get renderDecision(): string {
    return this.text ? this.text : "default text"
  }

  get isEmpty(): boolean {
    if (!this.text) return true
    return false
  }
}
