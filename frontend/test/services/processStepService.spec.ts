import { vi } from "vitest"
import ProcessStep from "@/domain/processStep"
import errorMessages from "@/i18n/errors.json"
import httpClient from "@/services/httpClient"
import service from "@/services/processStepService"

vi.mock("@/services/httpClient", () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
  },
}))

describe("ProcessStepService", () => {
  afterEach(() => {
    vi.clearAllMocks()
  })

  const mockProcessSteps: ProcessStep[] = [
    { uuid: "uuid1", name: "Step A", abbreviation: "A" },
  ]

  const mockNextProcessStep: ProcessStep = {
    uuid: "testProcessStepUuid456",
    name: "Next Step",
    abbreviation: "NS",
  }

  // --- getNextProcessStep Tests ---
  describe("getNextProcessStep", () => {
    it("should return the next process step on successful API call", async () => {
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
  describe("getProcessSteps URL Construction and Success Handling", () => {
    const testCases = [
      {
        description:
          "should call the API with '?assignableOnly=false' when parameter is omitted (default)",
        assignableOnlyArg: undefined,
        expectedUrl: "caselaw/processsteps?assignableOnly=false",
      },
      {
        description:
          "should call the API with '?assignableOnly=true' when parameter is true",
        assignableOnlyArg: true,
        expectedUrl: "caselaw/processsteps?assignableOnly=true",
      },
      {
        description:
          "should call the API with '?assignableOnly=false' when parameter is false (explicit call)",
        assignableOnlyArg: false,
        expectedUrl: "caselaw/processsteps?assignableOnly=false",
      },
    ]

    it.each(testCases)(
      "$description",
      async ({ assignableOnlyArg, expectedUrl }) => {
        vi.mocked(httpClient).get.mockResolvedValueOnce({
          status: 200,
          data: mockProcessSteps,
        })

        const result = await service.getProcessSteps(assignableOnlyArg)

        expect(httpClient.get).toHaveBeenCalledWith(expectedUrl)
        expect(result.data).toEqual(mockProcessSteps)
        expect(result.error).toBeUndefined()
      },
    )

    it("should return an error and undefined data if API call fails with status >= 300 (error handling)", async () => {
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

    it("should return an error and undefined data if the API call has an error property", async () => {
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
