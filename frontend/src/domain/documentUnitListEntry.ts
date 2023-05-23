import { DocumentationOffice } from "./documentUnit"

export type DocumentUnitListEntry = {
  id: string
  uuid: string
  documentNumber: string
  creationTimestamp: string
  fileName?: string
  fileNumber?: string
  documentationOffice?: DocumentationOffice
}
