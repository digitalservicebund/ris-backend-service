import service from "@/services/documentUnitService"
import { ServiceResponse } from "@/services/httpClient"

describe("documentUnitService", () => {
  vi.mock("@/services/httpClient", () => {
    const testResponse: ServiceResponse<string> = {
      status: 500,
      data: "foo",
    }
    return {
      default: {
        put: vi.fn().mockReturnValue(testResponse),
      },
    }
  })

  it("appends correct error message if status 500", async () => {
    const result = await service.searchByDocumentUnitSearchInput(0, 20)
    expect(result.error?.title).toEqual(
      "Die Suche nach passenden Dokumentationseinheit konnte nicht ausgeführt werden.",
    )
  })
})
