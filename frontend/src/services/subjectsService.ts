import httpClient, { ServiceResponse } from "./httpClient"
import { SubjectNode } from "@/domain/SubjectTree"

interface SubjectsService {
  getChildrenOf(
    subjectFieldNumber: string
  ): Promise<ServiceResponse<SubjectNode[]>>
  getTreeForSubjectFieldNumber(
    subjectFieldNumber: string
  ): Promise<ServiceResponse<SubjectNode>>
}

const service: SubjectsService = {
  async getChildrenOf(subjectFieldNumber: string) {
    const response = await httpClient.get<SubjectNode[]>(
      `caselaw/lookuptable/subjectFieldChildren/${subjectFieldNumber}`
    )
    if (response.status >= 300) {
      response.error = {
        title:
          "Sachgebiete unterhalb von " +
          subjectFieldNumber +
          " konnten nicht geladen werden.",
      }
    }
    return response
  },
  async getTreeForSubjectFieldNumber(subjectFieldNumber: string) {
    const response = await httpClient.get<SubjectNode>(
      `caselaw/lookuptable/subjectFields/${subjectFieldNumber}/tree`
    )
    // if (response.data) console.log("service - load tree:", response.data)
    if (response.status >= 300) {
      response.error = {
        title: "Pfad zu ausgewähltem Sachgebiet konnte nicht geladen werden.",
      }
    }
    return response
  },
}

export default service
