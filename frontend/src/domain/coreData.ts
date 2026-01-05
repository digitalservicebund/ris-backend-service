import { Court } from "@/domain/court"
import { CourtBranchLocation } from "@/domain/courtBranchLocation"
import DocumentationOffice from "@/domain/documentationOffice"
import { DocumentType } from "@/domain/documentType"
import { Procedure } from "@/domain/procedure"

import { Source } from "@/domain/source"

export type CoreData = {
  deviatingDocumentNumbers?: string[]
  fileNumbers?: string[]
  deviatingFileNumbers?: string[]
  court?: Court
  deviatingCourts?: string[]
  courtBranchLocation?: CourtBranchLocation
  documentType?: DocumentType
  procedure?: Procedure
  previousProcedures?: string[]
  ecli?: string
  celexNumber?: string
  deviatingEclis?: string[]
  appraisalBody?: string
  decisionDate?: string
  hasDeliveryDate?: boolean
  oralHearingDates?: string[]
  deviatingDecisionDates?: string[]
  legalEffect?: string
  inputTypes?: string[]
  documentationOffice?: DocumentationOffice
  creatingDocOffice?: DocumentationOffice
  yearsOfDispute?: number[]
  leadingDecisionNormReferences?: string[]
  sources?: Source[]
  isResolved?: boolean
  resolutionDate?: string
}

export const coreDataLabels: {
  [coreDataKeyKey in keyof Required<CoreData>]: string
} = {
  celexNumber: "CELEX-Nummer",
  hasDeliveryDate: "Zustellung an Verkündungs statt",
  oralHearingDates: "Datum der mündlichen Verhandlung",
  court: "Gericht",
  deviatingCourts: "Fehlerhaftes Gericht",
  courtBranchLocation: "Sitz der Außenstelle",
  fileNumbers: "Aktenzeichen",
  deviatingFileNumbers: "Abweichendes Aktenzeichen",
  deviatingDocumentNumbers: "Abweichende Dokumentnummer",
  decisionDate: "Entscheidungsdatum",
  appraisalBody: "Spruchkörper",
  creatingDocOffice: "Erstellende Dokumentationsstelle",
  ecli: "ECLI",
  documentationOffice: "Dokumentationsstelle",
  inputTypes: "Eingangsart",
  isResolved: "Erledigt",
  yearsOfDispute: "Streitjahr",
  resolutionDate: "Erledigungsmitteilung",
  sources: "Quelle",
  deviatingDecisionDates: "Abweichendes Entscheidungsdatum",
  documentType: "Dokumenttyp",
  deviatingEclis: "Abweichender ECLI",
  procedure: "Vorgang",
  previousProcedures: "Vorgang Historie",
  legalEffect: "Rechtskraft",
  leadingDecisionNormReferences: "BGH Nachschlagewerk",
}
