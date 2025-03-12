import { vi } from "vitest"
import featureToggleService from "@/services/featureToggleService"

/**
 * Mock service call for the feature toggle in vi test
 */
export function useFeatureToggleServiceMock() {
  vi.spyOn(featureToggleService, "isEnabled").mockResolvedValue({
    status: 200,
    data: true,
  })
}
