import { CourtBranchLocation } from "@/domain/courtBranchLocation"
import DocumentationOffice from "@/domain/documentationOffice"

export type Court = {
  type?: string
  location?: string
  label: string
  revoked?: string
  jurisdictionType?: JurisdictionType
  regions?: string[]
  responsibleDocOffice?: DocumentationOffice
  isSuperiorCourt?: boolean
  isForeignCourt?: boolean
  courtBranchLocations?: CourtBranchLocation[]
}

export type JurisdictionType =
  | "Arbeitsgerichtsbarkeit"
  | "Besondere Gerichtsbarkeit"
  | "Finanzgerichtsbarkeit"
  | "Ordentliche Gerichtsbarkeit"
  | "Sozialgerichtsbarkeit"
  | "Verfassungsgerichtsbarkeit"
  | "Verwaltungsgerichtsbarkeit"
