export default interface EditableListItem {
  isReadOnly: boolean
  isEmpty: boolean
  renderDecision: string
  hasMissingRequiredFields: boolean
  missingRequiredFields: string[]
}
