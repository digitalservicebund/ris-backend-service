export default interface EditableListItem {
  renderDecision: string
  hasMissingRequiredFields?: boolean
  missingRequiredFields?: string[]
}
