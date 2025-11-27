import httpClient, { ServiceResponse } from "./httpClient"
import errorMessages from "@/i18n/errors.json"

interface CourtService {
  getBranchLocations(
    type: string,
    location?: string,
  ): Promise<ServiceResponse<string[]>>
}

const service: CourtService = {
  async getBranchLocations(type: string, location: string) {
    const locationParam = location ? "&location=" + location : ""
    const response = await httpClient.get<string[]>(
      "caselaw/courts/branchlocations?type=" + type + locationParam,
    )
    if (response.status >= 300) {
      response.error = {
        title: errorMessages.SERVER_ERROR.title,
      }
    }
    return response
  },
}

export default service
