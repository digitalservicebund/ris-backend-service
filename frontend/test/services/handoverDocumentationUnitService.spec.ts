import { HandoverMail } from "@/domain/eventRecord"
import service from "@/services/handoverDocumentationUnitService"

// TODO as stated in https://vitest.dev/api/vi.html#vi-mock, the vi.mock is the same for all tests. This should be refactored
describe("handoverDocumentationUnitService", () => {
  it("returns error message if event report contains error but status is success", async () => {
    vi.mock("@/services/httpClient", () => {
      const testXml: HandoverMail = new HandoverMail({ success: false })
      return {
        default: {
          put: vi.fn().mockReturnValue({ status: 422, data: testXml }),
        },
      }
    })

    const result = await service.handoverDocument("123")
    expect(result.error?.title).toEqual("Leider ist ein Fehler aufgetreten.")
    expect(result.error?.description).toContain(
      "Die Dokumentationseinheit kann nicht übergeben werden.",
    )
  })
})
