import ActiveCitation from "@/domain/activeCitation"
import { Appeal } from "@/domain/appeal"
import { AppealAdmission } from "@/domain/appealAdmission"
import { CollectiveAgreement } from "@/domain/collectiveAgreement"
import Definition from "@/domain/definition"
import { FieldOfLaw } from "@/domain/fieldOfLaw"
import ForeignLanguageVersion from "@/domain/foreignLanguageVersion"
import NormReference from "@/domain/normReference"
import ObjectValue from "@/domain/objectValue"
import OriginOfTranslation from "@/domain/originOfTranslation"

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
  originOfTranslations?: OriginOfTranslation[]
  appealAdmission?: AppealAdmission
  appeal?: Appeal
  objectValues?: ObjectValue[]
  nonApplicationNorms?: NormReference[]
}
