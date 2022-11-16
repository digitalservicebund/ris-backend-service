import httpClient from "@/services/httpClient"
import {
  editNormFrame,
  getAllNorms,
  getNormByGuid,
} from "@/services/normsService"

vi.mock("@/services/httpClient")

describe("normsService", () => {
  beforeEach(() => {
    vi.resetAllMocks()
  })

  describe("list norms", () => {
    it("queries the backend with the correct parameters", async () => {
      const httpClientGet = vi
        .mocked(httpClient)
        .get.mockResolvedValueOnce({ status: 200, data: {} })

      await getAllNorms()

      expect(httpClientGet).toHaveBeenCalledOnce()
      expect(httpClientGet).toHaveBeenLastCalledWith("norms")
    })

    it("returns data entry of body if server connection was successful", async () => {
      vi.mocked(httpClient).get.mockResolvedValueOnce({
        status: 200,
        data: { data: ["fake-norm"] },
      })

      const response = await getAllNorms()

      expect(response.data).toEqual(["fake-norm"])
    })

    it("responds with correct error message if server response status is above 300", async () => {
      vi.mocked(httpClient).get.mockResolvedValueOnce({
        status: 300,
        data: {},
      })

      const response = await getAllNorms()

      expect(response.error?.title).toBe(
        "Dokumentationseinheiten konnten nicht geladen werden."
      )
    })

    it("responds with correct error message if connection failed", async () => {
      vi.mocked(httpClient).get.mockResolvedValueOnce({
        status: 500,
        error: { title: "error" },
      })

      const response = await getAllNorms()

      expect(response.error?.title).toBe(
        "Dokumentationseinheiten konnten nicht geladen werden."
      )
    })
  })

  describe("load norm", () => {
    it("queries the backend with the correct parameters", async () => {
      const httpClientGet = vi
        .mocked(httpClient)
        .get.mockResolvedValueOnce({ status: 200, data: "" })

      await getNormByGuid("fake-guid")

      expect(httpClientGet).toHaveBeenCalledOnce()
      expect(httpClientGet).toHaveBeenLastCalledWith("norms/fake-guid")
    })

    it("returns response body if server connection was successful", async () => {
      vi.mocked(httpClient).get.mockResolvedValue({
        status: 200,
        data: "fake-norm",
      })

      const response = await getNormByGuid("fake-guid")

      expect(response.data).toBe("fake-norm")
    })

    it("responds with correct error message if server response status is above 300", async () => {
      vi.mocked(httpClient).get.mockResolvedValueOnce({
        status: 300,
        data: "",
      })

      const response = await getNormByGuid("fake-guid")

      expect(response.error?.title).toBe(
        "Dokumentationseinheit konnte nicht geladen werden."
      )
    })

    it("responds with correct error message if connection failed", async () => {
      vi.mocked(httpClient).get.mockResolvedValueOnce({
        status: 500,
        error: { title: "error" },
      })

      const response = await getNormByGuid("fake-guid")

      expect(response.error?.title).toBe(
        "Dokumentationseinheit konnte nicht geladen werden."
      )
    })
  })

  describe("edit norm frame", () => {
    it("sends command to the backend with the correct parameters", async () => {
      const httpClientPatch = vi
        .mocked(httpClient)
        .patch.mockResolvedValueOnce({ status: 204, data: "" })

      await editNormFrame("fake-guid", "new title")

      expect(httpClientPatch).toHaveBeenCalledOnce()
      expect(httpClientPatch).toHaveBeenLastCalledWith(
        "norms/fake-guid",
        undefined,
        { longTitle: "new title" }
      )
    })

    it("responds with correct error message if server response status is above 300", async () => {
      vi.mocked(httpClient).patch.mockResolvedValueOnce({
        status: 300,
        data: "",
      })

      const response = await editNormFrame("fake-guid", "new title")

      expect(response.error?.title).toBe(
        "Dokumentationseinheit konnte nicht bearbeitet werden."
      )
    })

    it("responds with correct error message if connection failed", async () => {
      vi.mocked(httpClient).patch.mockResolvedValueOnce({
        status: 500,
        error: { title: "error" },
      })

      const response = await editNormFrame("fake-guid", "new title")

      expect(response.error?.title).toBe(
        "Dokumentationseinheit konnte nicht bearbeitet werden."
      )
    })
  })
})
