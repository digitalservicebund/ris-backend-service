import { vi } from "vitest" // Adjust the path as needed
import DocumentationUnitProcessStep from "@/domain/documentationUnitProcessStep"
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

  // --- getProcessSteps Tests ---
  describe("getProcessSteps", () => {
    it("should return process steps on successful API call", async () => {
      const mockProcessSteps: DocumentationUnitProcessStep[] = [
        {
          id: "a-id",
          userId: "user1-id",
          createdAt: new Date(),
          processStep: { uuid: "neu-id", name: "Neu", abbreviation: "N" },
        },
        {
          id: "b-id",
          userId: "user1-id",
          createdAt: new Date(),
          processStep: {
            uuid: "blockiert-id",
            name: "Blockiert",
            abbreviation: "B",
          },
        },
        {
          id: "c-id",
          userId: "user2-id",
          createdAt: new Date(),
          processStep: { uuid: "fertig-id", name: "Fertig", abbreviation: "F" },
        },
      ]
      vi.mocked(httpClient).get.mockResolvedValueOnce({
        status: 200,
        data: mockProcessSteps,
      })

      const result = await service.getProcessSteps("testDocUnitId123")

      expect(httpClient.get).toHaveBeenCalledWith(
        `caselaw/processsteps/${"testDocUnitId123"}/history`,
      )
      expect(result.data).toEqual(mockProcessSteps)
      expect(result.error).toBeUndefined()
    })

    it("should return an error if the API call for getProcessSteps fails with status >= 300", async () => {
      vi.mocked(httpClient).get.mockResolvedValueOnce({
        status: 404,
        error: { title: "Not Found" },
      })

      const result = await service.getProcessSteps("testDocUnitId123")

      expect(result.data).toBeUndefined()
      expect(result.error?.title).toEqual(
        errorMessages.DOCUMENTATION_UNIT_PROCESS_STEP_COULD_NOT_BE_LOADED.title,
      )
    })

    it("should return an error if the API call for getProcessSteps has an error property", async () => {
      vi.mocked(httpClient).get.mockResolvedValueOnce({
        status: 200, // Even with a 200, if error is present, it should fail
        error: { title: "Internal Server Error" },
      })

      const result = await service.getProcessSteps("testDocUnitId123")

      expect(result.data).toBeUndefined()
      expect(result.error?.title).toEqual(
        errorMessages.DOCUMENTATION_UNIT_PROCESS_STEP_COULD_NOT_BE_LOADED.title,
      )
    })
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
        `caselaw/processsteps/${"testDocUnitId123"}/next`,
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

  // --- moveToNextProcessStep Tests ---
  describe("moveToNextProcessStep", () => {
    const mockProcessStep: ProcessStep = {
      uuid: "testProcessStepUuid456",
      name: "Current Step",
      abbreviation: "CS",
    }
    const mockNewDocumentationUnitProcessStep: DocumentationUnitProcessStep = {
      id: "newDocUnitProcessStepId",
      userId: "userId",
      createdAt: new Date(),
      processStep: mockProcessStep,
    }

    it("should return the new documentation unit process step when process step moved", async () => {
      vi.mocked(httpClient).post.mockResolvedValueOnce({
        status: 200,
        data: mockNewDocumentationUnitProcessStep,
      })

      const result = await service.moveToNextProcessStep(
        "testDocUnitId123",
        mockProcessStep,
      )

      expect(httpClient.post).toHaveBeenCalledWith(
        `caselaw/processsteps/${"testDocUnitId123"}/new`,
        {
          headers: {
            Accept: "application/json",
            "Content-Type": "application/json",
          },
        },
        "testProcessStepUuid456",
      )
      expect(result.data).toEqual(mockNewDocumentationUnitProcessStep)
      expect(result.error).toBeUndefined()
    })

    it("should return an error if the API call for moveToNextProcessStep fails with status >= 300", async () => {
      vi.mocked(httpClient).post.mockResolvedValueOnce({
        status: 400,
        error: { title: "Bad Request" },
      })

      const result = await service.moveToNextProcessStep(
        "testDocUnitId123",
        mockProcessStep,
      )

      expect(result.data).toBeUndefined()
      expect(result.error?.title).toEqual(
        errorMessages.DOCUMENTATION_UNIT_PROCESS_STEP_COULD_NOT_BE_LOADED.title,
      )
    })

    it("should return an error if the API call for moveToNextProcessStep has an error property", async () => {
      vi.mocked(httpClient).post.mockResolvedValueOnce({
        status: 200,
        error: { title: "Validation Error" },
      })

      const result = await service.moveToNextProcessStep(
        "testDocUnitId123",
        mockProcessStep,
      )

      expect(result.data).toBeUndefined()
      expect(result.error?.title).toEqual(
        errorMessages.DOCUMENTATION_UNIT_PROCESS_STEP_COULD_NOT_BE_LOADED.title,
      )
    })
  })
})
