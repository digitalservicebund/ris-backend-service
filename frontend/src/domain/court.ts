import DocumentationOffice from "@/domain/documentationOffice"

export type Court = {
  type?: string
  location?: string
  label: string
  revoked?: string
  jurisdictionType?: string
  region?: string
  responsibleDocOffice?: DocumentationOffice
}
