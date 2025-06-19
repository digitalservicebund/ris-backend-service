import ActiveCitation from "@/domain/activeCitation"
import { FieldOfLaw } from "@/domain/fieldOfLaw"
import NormReference from "@/domain/normReference"

export type ContentRelatedIndexing = {
  collectiveAgreements?: string[]
  dismissalTypes?: string[]
  dismissalGrounds?: string[]
  keywords?: string[]
  norms?: NormReference[]
  activeCitations?: ActiveCitation[]
  fieldsOfLaw?: FieldOfLaw[]
  jobProfiles?: string[]
  hasLegislativeMandate?: boolean
}
