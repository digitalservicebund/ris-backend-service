export default interface EditableListItem {
  id?: string
  renderDecision: string
  hasMissingRequiredFields?: boolean
  missingRequiredFields?: string[]

  equals(entry: EditableListItem): boolean
}
