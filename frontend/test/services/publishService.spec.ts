import PublicationHistoryRecord from "@/domain/xmlMail"
import service from "@/services/publishService"

// TODO as stated in https://vitest.dev/api/vi.html#vi-mock, the vi.mock is the same for all tests. This should be refactored
describe("publishService", () => {
  it("returns error message if xmlMail contains error but status is success", async () => {
    vi.mock("@/services/httpClient", () => {
      const testXml: PublicationHistoryRecord = { statusCode: "400" }
      return {
        default: {
          put: vi.fn().mockReturnValue({ status: 200, data: testXml }),
        },
      }
    })

    const result = await service.publishDocument("123")
    expect(result.error?.title).toEqual("Leider ist ein Fehler aufgetreten.")
    expect(result.error?.description).toContain(
      "Die Dokumentationseinheit kann nicht veröffentlicht werden.",
    )
  })

  it("returns correct error message if preview contains error", async () => {
    vi.mock("@/services/httpClient", () => {
      return {
        default: {
          get: vi.fn().mockReturnValue({ status: 400, data: {} }),
          put: vi.fn().mockReturnValue({ status: 400, data: {} }),
        },
      }
    })

    const result = await service.getPreview("123")
    expect(result.error?.title).toEqual(
      "Fehler beim Laden der Veröffentlichungs-Vorschau",
    )
    // expect(result.error?.description).toContain(
    //   "Die Vorschau konnte nicht geladen werden.",
    // )
  })

  it("returns correct error message if preview contains XML error", async () => {
    vi.mock("@/services/httpClient", () => {
      return {
        default: {
          get: vi.fn().mockReturnValue({
            status: 200,
            data: {
              statusCode: "400",
              statusMessages: ["Fehler 1", "Fehler 2"],
            },
          }),
          put: vi.fn().mockReturnValue({
            status: 200,
            data: {
              statusCode: "400",
              statusMessages: ["Fehler 1", "Fehler 2"],
            },
          }),
        },
      }
    })

    const result = await service.getPreview("123")
    expect(result.error?.title).toEqual(
      "Fehler beim Laden der Veröffentlichungs-Vorschau",
    )
    expect(result.error?.multipartDescription).toContain("Fehler 2")
  })
})
