export default interface EditableListItem {
  localId: string
  documentNumber?: string
  renderSummary?: string
  hasMissingRequiredFields?: boolean
  missingRequiredFields?: string[]

  equals(entry: EditableListItem): boolean
}
