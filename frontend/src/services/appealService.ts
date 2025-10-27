import httpClient, { ServiceResponse } from "./httpClient"
import { AppealStatus } from "@/domain/appealStatus"
import { Appellant } from "@/domain/appellant"
import errorMessages from "@/i18n/errors.json"

interface AppealService {
  getAppellants(): Promise<ServiceResponse<Appellant[]>>
  getAppealStatuses(): Promise<ServiceResponse<AppealStatus[]>>
}

const service: AppealService = {
  async getAppellants() {
    const response = await httpClient.get<Appellant[]>(
      "caselaw/appeal/appellants",
    )
    if (response.status >= 300 || response.error) {
      response.data = undefined
      response.error = errorMessages.SERVER_ERROR
    }
    return response
  },

  async getAppealStatuses() {
    const response = await httpClient.get<AppealStatus[]>(
      "caselaw/appeal/statuses",
    )
    if (response.status >= 300 || response.error) {
      response.data = undefined
      response.error = errorMessages.SERVER_ERROR
    }
    return response
  },
}

export default service
