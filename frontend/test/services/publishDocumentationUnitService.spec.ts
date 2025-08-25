import errorMessages from "../../src/i18n/errors.json"
import service from "@/services/publishDocumentationUnitService"

const { putMock, getMock } = vi.hoisted(() => {
  return {
    putMock: vi.fn(),
    getMock: vi.fn(),
  }
})
vi.mock("@/services/httpClient", () => {
  return {
    default: {
      put: putMock,
      get: getMock,
    },
  }
})

describe("publishDocumentationUnitService", () => {
  describe("publish", () => {
    it("returns error on failure", async () => {
      putMock.mockReturnValue({
        status: 400,
        error: { message: "invalid request" },
      })
      const result = await service.publishDocument("123")
      expect(result.error?.title).toEqual(
        "Fehler beim Veröffentlichen der Dokumentationseinheit",
      )
      expect(result.error?.description).toContain(
        "Die Dokumentationseinheit konnte nicht veröffentlicht werden",
      )
    })

    it("returns data on success", async () => {
      putMock.mockReturnValue({
        status: 200,
        data: { message: "success" },
      })
      const result = await service.publishDocument("123")
      expect(result.data).toEqual({ message: "success" })
    })
  })

  describe("getPreview", () => {
    it("returns error for 400 Bad Request", async () => {
      getMock.mockReturnValue({
        status: 400,
        error: { message: "bad request" },
      })
      const result = await service.getPreview("123")
      expect(result.error?.title).toEqual(
        errorMessages.DOCUMENT_UNIT_LOADING_LDML_PREVIEW.title,
      )
      expect(result.error?.description).toContain(
        errorMessages.DOCUMENT_UNIT_LOADING_LDML_PREVIEW.description +
          ": " +
          errorMessages.DOCUMENT_UNIT_NOT_ALLOWED.title,
      )
    })

    it("returns error for 403 Forbidden", async () => {
      getMock.mockReturnValue({
        status: 403,
        error: { message: "forbidden" },
      })
      const result = await service.getPreview("123")
      expect(result.error?.title).toEqual(
        errorMessages.DOCUMENT_UNIT_LOADING_LDML_PREVIEW.title,
      )
      expect(result.error?.description).toContain(
        errorMessages.DOCUMENT_UNIT_LOADING_LDML_PREVIEW.description +
          ": " +
          errorMessages.DOCUMENT_UNIT_NOT_ALLOWED.title,
      )
    })

    it("returns error for 422 Unprocessable Entity", async () => {
      getMock.mockReturnValue({
        status: 422,
        data: { success: false, statusMessages: ["Not processable!"] },
      })
      const result = await service.getPreview("123")
      expect(result.error?.title).toEqual(
        errorMessages.DOCUMENT_UNIT_LOADING_LDML_PREVIEW.title,
      )
      expect(result.error?.description).toContain(
        errorMessages.DOCUMENT_UNIT_LOADING_LDML_PREVIEW.description +
          ": " +
          "Not processable!",
      )
    })

    it("returns valid preview on success", async () => {
      getMock.mockReturnValue({
        status: 200,
        data: { ldml: "ldml", success: true },
      })
      const result = await service.getPreview("123")
      expect(result.data).toEqual({ ldml: "ldml", success: true })
      expect(result.error).toBeUndefined()
    })
  })
})
