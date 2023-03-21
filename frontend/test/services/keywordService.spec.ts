import { ServiceResponse } from "@/services/httpClient"
import service from "@/services/keywordsService"

describe("keywordsService", () => {
  vi.mock("@/services/keywordsService", () => {
    const testGetResponse: ServiceResponse<string[]> = {
      status: 200,
      data: ["one", "two"],
    }
    const testPutResponse: ServiceResponse<string[]> = {
      status: 200,
      data: ["one", "two", "three"],
    }
    const testDeleteResponse: ServiceResponse<string[]> = {
      status: 200,
      data: ["one"],
    }
    return {
      default: {
        getKeywords: vi.fn().mockReturnValue(testGetResponse),
        addKeyword: vi.fn().mockReturnValue(testPutResponse),
        deleteKeyword: vi.fn().mockReturnValue(testDeleteResponse),
      },
    }
  })

  it("get all keywords", async () => {
    const result = await service.getKeywords("docunit123")
    expect(result.data).toEqual(["one", "two"])
  })

  it("add keyword", async () => {
    const result = await service.addKeyword("docunit123", "three")
    expect(result.data).toEqual(["one", "two", "three"])
  })

  it("delete keyword", async () => {
    const result = await service.deleteKeyword("docunit123", "two")
    expect(result.data).toEqual(["one"])
  })
})
