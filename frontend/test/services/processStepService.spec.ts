import { vi } from "vitest"
import ProcessStep from "@/domain/processStep"
import errorMessages from "@/i18n/errors.json"
import httpClient from "@/services/httpClient"
import service from "@/services/processStepService"

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
        `caselaw/documentationUnits/${"testDocUnitId123"}/processsteps/next`,
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
      expect(result.error).toEqual(
        errorMessages.NEXT_PROCESS_STEP_FOR_DOCUMENATION_UNIT_COULD_NOT_BE_LOADED,
      )
    })

    it("should return an error if the API call for getNextProcessStep has an error property", async () => {
      vi.mocked(httpClient).get.mockResolvedValueOnce({
        status: 200,
        error: { title: "Network Error" },
      })

      const result = await service.getNextProcessStep("testDocUnitId123")

      expect(result.data).toBeUndefined()
      expect(result.error).toEqual(
        errorMessages.NEXT_PROCESS_STEP_FOR_DOCUMENATION_UNIT_COULD_NOT_BE_LOADED,
      )
    })
  })

  // --- getProcessSteps Tests ---
  describe("getProcessSteps", () => {
    const mockAllSteps: ProcessStep[] = [
      { uuid: "uuid1", name: "Neu", abbreviation: "N" },
      { uuid: "uuid2", name: "Ersterfassung", abbreviation: "E" },
    ]

    const mockAssignableSteps: ProcessStep[] = [
      { uuid: "uuid2", name: "Ersterfassung", abbreviation: "E" },
    ]

    it("should return all process steps on successful API call (default call)", async () => {
      vi.mocked(httpClient).get.mockResolvedValueOnce({
        status: 200,
        data: mockAllSteps,
      })

      // Calling without parameter uses the default: assignableOnly = false
      const result = await service.getProcessSteps()

      expect(httpClient.get).toHaveBeenCalledWith("caselaw/processsteps")
      expect(result.data).toEqual(mockAllSteps)
      expect(result.error).toBeUndefined()
    })

    it("should return only assignable steps when assignableOnly is true", async () => {
      vi.mocked(httpClient).get.mockResolvedValueOnce({
        status: 200,
        data: mockAssignableSteps,
      })

      // Calling with parameter true
      const result = await service.getProcessSteps(true)

      expect(httpClient.get).toHaveBeenCalledWith(
        "caselaw/processsteps?assignableOnly=true",
      )
      expect(result.data).toEqual(mockAssignableSteps)
      expect(result.error).toBeUndefined()
    })

    it("should return all steps when assignableOnly is false (explicit call)", async () => {
      vi.mocked(httpClient).get.mockResolvedValueOnce({
        status: 200,
        data: mockAllSteps,
      })

      // Calling with parameter false
      const result = await service.getProcessSteps(false)

      expect(httpClient.get).toHaveBeenCalledWith("caselaw/processsteps")
      expect(result.data).toEqual(mockAllSteps)
      expect(result.error).toBeUndefined()
    })

    it("should return an error if the API call for getProcessSteps fails with status >= 300 (default)", async () => {
      vi.mocked(httpClient).get.mockResolvedValueOnce({
        status: 500,
        error: { title: "Server Error" },
      })

      const result = await service.getProcessSteps()

      expect(result.data).toBeUndefined()
      expect(result.error).toEqual(
        errorMessages.PROCESS_STEPS_OF_DOCUMENTATION_OFFICE_COULD_NOT_BE_LOADED,
      )
    })

    it("should return an error if the API call for getProcessSteps fails with status >= 300 (assignableOnly=true)", async () => {
      vi.mocked(httpClient).get.mockResolvedValueOnce({
        status: 500,
        error: { title: "Server Error" },
      })

      const result = await service.getProcessSteps(true)

      // Ensure the correct URL was called
      expect(httpClient.get).toHaveBeenCalledWith(
        "caselaw/processsteps?assignableOnly=true",
      )
      expect(result.data).toBeUndefined()
      expect(result.error).toEqual(
        errorMessages.PROCESS_STEPS_OF_DOCUMENTATION_OFFICE_COULD_NOT_BE_LOADED,
      )
    })

    it("should return an error if the API call for getProcessSteps has an error property", async () => {
      vi.mocked(httpClient).get.mockResolvedValueOnce({
        status: 200,
        error: { title: "Network Error" },
      })

      const result = await service.getProcessSteps()

      expect(result.data).toBeUndefined()
      expect(result.error).toEqual(
        errorMessages.PROCESS_STEPS_OF_DOCUMENTATION_OFFICE_COULD_NOT_BE_LOADED,
      )
    })
  })
})
