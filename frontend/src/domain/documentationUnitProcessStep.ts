import ProcessStep from "@/domain/processStep"

type DocumentationUnitProcessStep = {
  id?: string
  userId?: string
  createdAt?: Date
  processStep: ProcessStep
}

export default DocumentationUnitProcessStep
