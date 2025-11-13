import httpClient, { ServiceResponse } from "./httpClient"
import { AppealStatus, Appellant } from "@/domain/appeal"
import errorMessages from "@/i18n/errors.json"

interface AppealOptionService {
  getAppellants(): Promise<ServiceResponse<Appellant[]>>
  getAppealStatuses(): Promise<ServiceResponse<AppealStatus[]>>
}

const service: AppealOptionService = {
  async getAppellants() {
    const response = await httpClient.get<Appellant[]>(
      "caselaw/appealoptions/appellants",
    )
    if (response.status >= 300 || response.error) {
      response.data = undefined
      response.error = errorMessages.SERVER_ERROR
    }
    return response
  },

  async getAppealStatuses() {
    const response = await httpClient.get<AppealStatus[]>(
      "caselaw/appealoptions/statuses",
    )
    if (response.status >= 300 || response.error) {
      response.data = undefined
      response.error = errorMessages.SERVER_ERROR
    }
    return response
  },
}

export default service
