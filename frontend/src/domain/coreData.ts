import { Court } from "@/domain/court"
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
  documentType?: DocumentType
  procedure?: Procedure
  previousProcedures?: string[]
  ecli?: string
  deviatingEclis?: string[]
  appraisalBody?: string
  decisionDate?: string
  deviatingDecisionDates?: string[]
  legalEffect?: string
  inputTypes?: string[]
  documentationOffice?: DocumentationOffice
  creatingDocOffice?: DocumentationOffice
  yearsOfDispute?: string[]
  leadingDecisionNormReferences?: string[]
  source?: Source
  isResolved?: boolean
  resolutionDate?: string
}
