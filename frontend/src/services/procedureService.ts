import httpClient, { ServiceResponse } from "./httpClient"
import { Procedure } from "@/domain/documentUnit"
import errorMessages from "@/shared/i18n/errors.json"

interface ProcedureService {
  getAll(): Promise<ServiceResponse<Procedure[]>>
}

const service: ProcedureService = {
  async getAll() {
    const response = await httpClient.get<Procedure[]>(`caselaw/procedure`)
    if (response.status >= 300) {
      response.error = {
        title: errorMessages.PROCEDURE_GET_ALL.title,
      }
    }
    return response
  },
}

export default service
