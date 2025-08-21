import service from "@/services/publishDocumentationUnitService"

const { putMock } = vi.hoisted(() => {
  return {
    putMock: vi.fn(),
  }
})
vi.mock("@/services/httpClient", () => {
  return {
    default: {
      put: putMock,
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

  describe("withdraw", () => {
    it("returns error on failure", async () => {
      putMock.mockReturnValue({
        status: 400,
        error: { message: "invalid request" },
      })
      const result = await service.withdrawDocument("123")
      expect(result.error?.title).toEqual(
        "Fehler beim Zurückziehen der Dokumentationseinheit",
      )
      expect(result.error?.description).toContain(
        "Die Dokumentationseinheit konnte nicht zurückgezogen werden",
      )
    })

    it("returns data on success", async () => {
      putMock.mockReturnValue({
        status: 200,
        data: { message: "success" },
      })
      const result = await service.withdrawDocument("123")
      expect(result.data).toEqual({ message: "success" })
    })
  })
})
