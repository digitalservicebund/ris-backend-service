import ActiveCitation from "@/domain/activeCitation"
import { AppealAdmission } from "@/domain/appealAdmission"
import Definition from "@/domain/definition"
import { FieldOfLaw } from "@/domain/fieldOfLaw"
import ForeignLanguageVersion from "@/domain/foreignLanguageVersion"
import NormReference from "@/domain/normReference"
import OriginOfTranslation from "@/domain/originOfTranslation"

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
  foreignLanguageVersions?: ForeignLanguageVersion[]
  originOfTranslations?: OriginOfTranslation[]
  appealAdmission?: AppealAdmission
}
