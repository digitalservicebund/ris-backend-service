import { vi } from "vitest" // Adjust the path as needed
import ProcessStep from "@/domain/processStep"
import errorMessages from "@/i18n/errors.json"
import httpClient from "@/services/httpClient"
import service from "@/services/processStepService"

// Import the mocked httpClient after vi.mock

// Mock the entire httpClient module
vi.mock("@/services/httpClient", () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
  },
}))

describe("ProcessStepService", () => {
  afterEach(() => {
    // Clear all mocks after each test to ensure isolation
    vi.clearAllMocks()
  })

  // --- getNextProcessStep Tests ---
  describe("getNextProcessStep", () => {
    it("should return the next process step on successful API call", async () => {
      const mockNextProcessStep: ProcessStep = {
        uuid: "testProcessStepUuid456",
        name: "Next Step",
        abbreviation: "NS",
      }
      vi.mocked(httpClient).get.mockResolvedValueOnce({
        status: 200,
        data: mockNextProcessStep,
      })

      const result = await service.getNextProcessStep("testDocUnitId123")

      expect(httpClient.get).toHaveBeenCalledWith(
        `caselaw/documentationUnits/${"testDocUnitId123"}/processteps/next`,
      )
      expect(result.data).toEqual(mockNextProcessStep)
      expect(result.error).toBeUndefined()
    })

    it("should return an error if the API call for getNextProcessStep fails with status >= 300", async () => {
      vi.mocked(httpClient).get.mockResolvedValueOnce({
        status: 500,
        error: { title: "Server Error" },
      })

      const result = await service.getNextProcessStep("testDocUnitId123")

      expect(result.data).toBeUndefined()
      expect(result.error?.title).toEqual(
        errorMessages.DOCUMENTATION_UNIT_PROCESS_STEP_COULD_NOT_BE_LOADED.title,
      )
    })

    it("should return an error if the API call for getNextProcessStep has an error property", async () => {
      vi.mocked(httpClient).get.mockResolvedValueOnce({
        status: 200,
        error: { title: "Network Error" },
      })

      const result = await service.getNextProcessStep("testDocUnitId123")

      expect(result.data).toBeUndefined()
      expect(result.error?.title).toEqual(
        errorMessages.DOCUMENTATION_UNIT_PROCESS_STEP_COULD_NOT_BE_LOADED.title,
      )
    })
  })
})
