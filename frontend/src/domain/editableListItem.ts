export default interface EditableListItem {
  isReadOnly: boolean
  // hasForeignSource: boolean
  isEmpty: boolean
  renderDecision: string
  hasMissingRequiredFields: boolean
  missingRequiredFields: string[]
}
