import ActiveCitation from "@/domain/activeCitation"
import Definition from "@/domain/definition"
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
  evsf?: string
  definitions?: Definition[]
}
