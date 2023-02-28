import httpClient, { ServiceResponse } from "./httpClient"
import { FieldOfLawNode } from "@/domain/fieldOfLaw"

interface FieldOfLawService {
  getSelectedFieldsOfLaw(
    uuid: string
  ): Promise<ServiceResponse<FieldOfLawNode[]>>
  addFieldOfLaw(
    uuid: string,
    identifier: string
  ): Promise<ServiceResponse<FieldOfLawNode[]>>
  removeFieldOfLaw(
    uuid: string,
    identifier: string
  ): Promise<ServiceResponse<FieldOfLawNode[]>>
  getChildrenOf(identifier: string): Promise<ServiceResponse<FieldOfLawNode[]>>
  getTreeForIdentifier(
    identifier: string
  ): Promise<ServiceResponse<FieldOfLawNode>>
}

const service: FieldOfLawService = {
  async getSelectedFieldsOfLaw(uuid: string) {
    const response = await httpClient.get<FieldOfLawNode[]>(
      `caselaw/documentunits/${uuid}/contentrelatedindexing/fieldsoflaw`
    )
    if (response.status >= 300) {
      response.error = {
        title: `Sachgebiete für die Dokumentationseinheit ${uuid} konnten nicht geladen werden.`,
      }
    }
    return response
  },
  async addFieldOfLaw(uuid: string, identifier: string) {
    const response = await httpClient.put<undefined, FieldOfLawNode[]>(
      `caselaw/documentunits/${uuid}/contentrelatedindexing/fieldsoflaw/${identifier}`
    )
    if (response.status >= 300) {
      response.error = {
        title: `Sachgebiet ${identifier} konnte nicht zu 
          Dokumentationseinheit ${uuid} hinzugefügt werden`,
      }
    }
    return response
  },
  async removeFieldOfLaw(uuid: string, identifier: string) {
    const response = await httpClient.delete<FieldOfLawNode[]>(
      `caselaw/documentunits/${uuid}/contentrelatedindexing/fieldsoflaw/${identifier}`
    )
    if (response.status >= 300) {
      response.error = {
        title: `Sachgebiet ${identifier} konnte nicht von der 
        Dokumentationseinheit ${uuid} entfernt werden`,
      }
    }
    return response
  },
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
