import httpClient from "@/services/httpClient"
import service from "@/services/keywordsService"

vi.mock("@/services/httpClient")
describe("keywordsService", () => {
  beforeEach(() => {
    vi.resetAllMocks()
  })

  it("gets all keywords", async () => {
    const httpClientGet = vi.mocked(httpClient).get.mockResolvedValueOnce({
      status: 200,
      data: ["one", "two"],
    })

    const result = await service.getKeywords("docunit123")
    expect(httpClientGet).toHaveBeenCalledOnce()
    expect(result.data).toEqual(["one", "two"])
  })

  it("add keyword", async () => {
    const httpClientPut = vi.mocked(httpClient).put.mockResolvedValueOnce({
      status: 200,
      data: ["one", "two", "three"],
    })

    const result = await service.addKeyword("docunit123", "three")
    expect(httpClientPut).toHaveBeenCalledOnce()
    expect(result.data).toEqual(["one", "two", "three"])
  })

  it("delete keyword", async () => {
    const httpClientDelete = vi
      .mocked(httpClient)
      .delete.mockResolvedValueOnce({
        status: 200,
        data: ["one"],
      })

    const result = await service.deleteKeyword("docunit123", "two")
    expect(httpClientDelete).toHaveBeenCalledOnce()
    expect(result.data).toEqual(["one"])
  })

  it("should return error message if status > 300 on get request", async () => {
    vi.mocked(httpClient).get.mockResolvedValueOnce({
      status: 500,
      data: [],
    })

    const getResult = await service.getKeywords("docunit123")
    expect(getResult.error?.title).toEqual(
      "Schlagwörter konnten nicht geladen werden."
    )
  })

  it("should return error message if status > 300 on put request", async () => {
    vi.mocked(httpClient).put.mockResolvedValueOnce({
      status: 500,
      data: [],
    })

    const putResult = await service.addKeyword("docunit123", "three")
    expect(putResult.error?.title).toEqual(
      "Schlagwort three konnte nicht hinzugefügt werden"
    )
  })

  it("should return error message if status > 300 on delete request", async () => {
    vi.mocked(httpClient).delete.mockResolvedValueOnce({
      status: 500,
      data: [],
    })

    const deleteResult = await service.deleteKeyword("docunit123", "three")
    expect(deleteResult.error?.title).toEqual(
      "Schlagwort three konnte nicht entfernt werden"
    )
  })
})
