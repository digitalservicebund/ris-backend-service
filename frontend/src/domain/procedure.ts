import DocumentUnitListEntry from "@/domain/documentUnitListEntry"

export type Procedure = {
  id?: string
  label: string
  documentationUnitCount: number
  createdAt: string
  documentUnits?: DocumentUnitListEntry[]
  userGroupId?: string
}
