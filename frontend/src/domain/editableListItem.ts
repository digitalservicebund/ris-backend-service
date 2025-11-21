export default interface EditableListItem {
  id?: string
  documentNumber?: string
  renderSummary?: string
  hasMissingRequiredFields?: boolean
  missingRequiredFields?: string[]

  equals(entry: EditableListItem): boolean
}
