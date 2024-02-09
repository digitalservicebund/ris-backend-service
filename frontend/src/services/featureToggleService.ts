import httpClient, { ServiceResponse } from "./httpClient"
import errorMessages from "@/i18n/errors.json"

interface FeatureToggleService {
  isEnabled(toggleName: string): Promise<ServiceResponse<boolean>>
}

const service: FeatureToggleService = {
  async isEnabled(toggleName: string) {
    const response = await httpClient.get<boolean>(
      "feature-toggles/" + toggleName,
    )
    if (response.status >= 300) {
      response.error = {
        title: errorMessages.FEATURE_TOGGLE_COULD_NOT_BE_LOADED.title,
      }
    }
    return response
  },
}

export default service
