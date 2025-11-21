import ActiveCitation from "@/domain/activeCitation"
import { Appeal } from "@/domain/appeal"
import { AppealAdmission } from "@/domain/appealAdmission"
import { CollectiveAgreement } from "@/domain/collectiveAgreement"
import Definition from "@/domain/definition"
import { FieldOfLaw } from "@/domain/fieldOfLaw"
import ForeignLanguageVersion from "@/domain/foreignLanguageVersion"
import NormReference from "@/domain/normReference"

export type ContentRelatedIndexing = {
  collectiveAgreements?: CollectiveAgreement[]
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
  foreignLanguageVersions?: ForeignLanguageVersion[]
  appealAdmission?: AppealAdmission
  appeal?: Appeal
}
