import ProcessStep from "@/domain/processStep"
import { User } from "@/domain/user"

type DocumentationUnitProcessStep = {
  id?: string
  user?: User
  createdAt?: Date
  processStep: ProcessStep
}

export default DocumentationUnitProcessStep
