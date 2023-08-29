import httpClient, { ServiceResponse } from "./httpClient"
import { Procedure } from "@/domain/documentUnit"
import errorMessages from "@/shared/i18n/errors.json"

interface ProcedureService {
  getAll(size: number, page: number): Promise<ServiceResponse<Procedure[]>>
}

const service: ProcedureService = {
  async getAll(size: number, page: number) {
    const response = await httpClient.get<Procedure[]>(`caselaw/procedure`, {
      params: { sz: size.toString(), pg: page.toString() },
    })
    if (response.status >= 300) {
      response.error = {
        title: errorMessages.PROCEDURE_GET_ALL.title,
      }
    }
    return response
  },
}

export default service
