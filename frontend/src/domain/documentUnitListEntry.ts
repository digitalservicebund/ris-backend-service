import DocumentationOffice from "./documentationOffice"
import DocumentUnit from "./documentUnit"

export type DocumentUnitListEntry = {
  id: string
  uuid: string
  documentNumber: string
  creationTimestamp: string
  status: NonNullable<DocumentUnit["status"]>
  fileName?: string
  fileNumber?: string
  documentationOffice?: DocumentationOffice
}
