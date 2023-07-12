export default interface EditableListItem {
  isReadOnly: boolean
  renderDecision: string
  hasMissingRequiredFields: boolean
  missingRequiredFields: string[]
}
