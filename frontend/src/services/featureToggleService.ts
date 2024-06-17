import httpClient, { ServiceResponse } from "./httpClient"
import errorMessages from "@/i18n/errors.json"

interface FeatureToggleService {
  isEnabled(toggleName: string): Promise<ServiceResponse<boolean>>
  getEnabledToggles(): Promise<Record<string, boolean>>
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

  /**
   * For a list of feature flags, fetch each enabled status from the backend and build a map.
   * Workaround as long as we don't get a project-specific set of feature flags from the backend resp. Unleash.
   */
  async getEnabledToggles(): Promise<Record<string, boolean>> {
    const TOGGLE_NAMES = ["neuris.note"]

    const featureFlagsList = await Promise.all(
      TOGGLE_NAMES.map(async (name) => [
        [name],
        (await this.isEnabled(name))?.data ?? false,
      ]),
    )

    return Object.fromEntries(featureFlagsList)
  },
}

export default service
