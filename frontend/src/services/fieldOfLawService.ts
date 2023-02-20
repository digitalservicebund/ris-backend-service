import httpClient, { ServiceResponse } from "./httpClient"
import { FieldOfLawNode } from "@/domain/fieldOfLawTree"

interface FieldOfLawService {
  getChildrenOf(number: string): Promise<ServiceResponse<FieldOfLawNode[]>>
  getTreeForNumber(number: string): Promise<ServiceResponse<FieldOfLawNode>>
}

const service: FieldOfLawService = {
  async getChildrenOf(number: string) {
    const response = await httpClient.get<FieldOfLawNode[]>(
      `caselaw/fieldsoflaw/${number}/children`
    )
    if (response.status >= 300) {
      response.error = {
        title:
          "Sachgebiete unterhalb von " +
          number +
          " konnten nicht geladen werden.",
      }
    }
    return response
  },
  async getTreeForNumber(number: string) {
    const response = await httpClient.get<FieldOfLawNode>(
      `caselaw/fieldsoflaw/${number}/tree`
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
