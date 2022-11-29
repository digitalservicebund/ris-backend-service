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
      const httpClientPut = vi
        .mocked(httpClient)
        .put.mockResolvedValueOnce({ status: 204, data: "" })

      await editNormFrame("fake-guid", {
        longTitle: "new title",
        officialShortTitle: "",
        officialAbbreviation: undefined,
        referenceNumber: "",
        publicationDate: "2022-11-14T23:00:00.000Z",
        announcementDate: "",
        citationDate: undefined,
        frameKeywords: "",
        authorEntity: "new author entity",
        authorDecidingBody: undefined,
        authorIsResolutionMajority: undefined,
        leadJurisdiction: undefined,
        leadUnit: undefined,
        participationType: undefined,
        participationInstitution: undefined,
      })

      expect(httpClientPut).toHaveBeenCalledOnce()
      expect(httpClientPut).toHaveBeenLastCalledWith(
        "norms/fake-guid",
        {
          headers: {
            Accept: "application/json",
            "Content-Type": "application/json",
          },
        },
        {
          longTitle: "new title",
          officialShortTitle: null,
          officialAbbreviation: null,
          referenceNumber: null,
          publicationDate: "2022-11-15",
          announcementDate: null,
          citationDate: null,
          frameKeywords: null,
          authorEntity: "new author entity",
          authorDecidingBody: null,
          authorIsResolutionMajority: null,
          leadJurisdiction: null,
          leadUnit: null,
          participationType: null,
          participationInstitution: null,
          documentTypeName: null,
          documentNormCategory: null,
          documentTemplateName: null,
          subjectFna: null,
          subjectPreviousFna: null,
          subjectGesta: null,
          subjectBgb3: null,
          unofficialTitle: null,
          unofficialShortTitle: null,
          unofficialAbbreviation: null,
          risAbbreviation: null,
        }
      )
    })

    it("responds with correct error message if server response status is above 300", async () => {
      vi.mocked(httpClient).put.mockResolvedValueOnce({
        status: 300,
        data: "",
      })

      const response = await editNormFrame("fake-guid", {
        longTitle: "new title",
      })

      expect(response.error?.title).toBe(
        "Dokumentationseinheit konnte nicht bearbeitet werden."
      )
    })

    it("responds with correct error message if connection failed", async () => {
      vi.mocked(httpClient).put.mockResolvedValueOnce({
        status: 500,
        error: { title: "error" },
      })

      const response = await editNormFrame("fake-guid", {
        longTitle: "new title",
      })

      expect(response.error?.title).toBe(
        "Dokumentationseinheit konnte nicht bearbeitet werden."
      )
    })
  })
})
