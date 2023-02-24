import httpClient, { ServiceResponse } from "./httpClient"
import { FieldOfLawNode } from "@/domain/fieldOfLaw"

interface FieldOfLawService {
  getChildrenOf(identifier: string): Promise<ServiceResponse<FieldOfLawNode[]>>
  getTreeForIdentifier(
    identifier: string
  ): Promise<ServiceResponse<FieldOfLawNode>>
}

const service: FieldOfLawService = {
  async getChildrenOf(identifier: string) {
    const response = await httpClient.get<FieldOfLawNode[]>(
      `caselaw/fieldsoflaw/${identifier}/children`
    )
    if (response.status >= 300) {
      response.error = {
        title:
          "Sachgebiete unterhalb von " +
          identifier +
          " konnten nicht geladen werden.",
      }
    }
    return response
  },
  async getTreeForIdentifier(identifier: string) {
    const response = await httpClient.get<FieldOfLawNode>(
      `caselaw/fieldsoflaw/${identifier}/tree`
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
