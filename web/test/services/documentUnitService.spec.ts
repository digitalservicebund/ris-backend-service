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
        get: vi.fn().mockReturnValue(testResponse),
      },
    }
  })

  it("appends correct error message if status 500", async () => {
    const result = await service.getAllListEntries()
    expect(result.error?.title).toEqual(
      "Dokumentationseinheiten konnten nicht geladen werden."
    )
  })
})
