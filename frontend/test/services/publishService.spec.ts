import XmlMail from "@/domain/xmlMail"
import service from "@/services/publishService"

describe("publishService", () => {
  it("returns error message if xmlMail contains error but status is success", async () => {
    vi.mock("@/services/httpClient", () => {
      const testXml: XmlMail = { statusCode: "400" }
      return {
        default: {
          put: vi.fn().mockReturnValue({ status: 200, data: testXml }),
        },
      }
    })

    const result = await service.publishDocument("123")
    expect(result.error?.title).toEqual("Leider ist ein Fehler aufgetreten.")
    expect(result.error?.description).toEqual(
      "Die Dokumentationseinheit kann nicht veröffentlicht werden."
    )
  })
})
