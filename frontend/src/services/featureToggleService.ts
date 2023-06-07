import httpClient, { ServiceResponse } from "./httpClient"

interface FeatureToggleService {
  isEnabled(toggleName: string): Promise<ServiceResponse<boolean>>
}

const service: FeatureToggleService = {
  async isEnabled(toggleName: string) {
    const response = await httpClient.get<boolean>(
      "feature-toggles/" + toggleName
    )
    if (response.status >= 300) {
      response.error = {
        title: `Feature toggle konnten nicht geladen werden.`,
      }
    }
    return response
  },
}

export default service
