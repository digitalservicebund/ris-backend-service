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
        officialShortTitle: undefined,
        officialAbbreviation: undefined,
        risAbbreviation: undefined,
        referenceNumber: undefined,
        publicationDate: undefined,
        announcementDate: undefined,
        citationDate: undefined,
        frameKeywords: undefined,
        authorEntity: undefined,
        authorDecidingBody: undefined,
        authorIsResolutionMajority: undefined,
        leadJurisdiction: undefined,
        leadUnit: undefined,
        participationType: undefined,
        participationInstitution: undefined,
        documentTypeName: undefined,
        documentNormCategory: undefined,
        documentTemplateName: undefined,
        subjectFna: undefined,
        subjectPreviousFna: undefined,
        subjectGesta: undefined,
        subjectBgb3: undefined,
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
          officialShortTitle: undefined,
          officialAbbreviation: undefined,
          referenceNumber: undefined,
          publicationDate: undefined,
          announcementDate: undefined,
          citationDate: undefined,
          frameKeywords: undefined,
          authorEntity: undefined,
          authorDecidingBody: undefined,
          authorIsResolutionMajority: undefined,
          leadJurisdiction: undefined,
          leadUnit: undefined,
          participationType: undefined,
          participationInstitution: undefined,
          documentTypeName: undefined,
          documentNormCategory: undefined,
          documentTemplateName: undefined,
          subjectFna: undefined,
          subjectPreviousFna: undefined,
          subjectGesta: undefined,
          subjectBgb3: undefined,
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
        officialShortTitle: undefined,
        officialAbbreviation: undefined,
        referenceNumber: undefined,
        publicationDate: undefined,
        announcementDate: undefined,
        citationDate: undefined,
        frameKeywords: undefined,
        authorEntity: undefined,
        authorDecidingBody: undefined,
        authorIsResolutionMajority: undefined,
        leadJurisdiction: undefined,
        leadUnit: undefined,
        participationType: undefined,
        participationInstitution: undefined,
        documentTypeName: undefined,
        documentNormCategory: undefined,
        documentTemplateName: undefined,
        subjectFna: undefined,
        subjectPreviousFna: undefined,
        subjectGesta: undefined,
        subjectBgb3: undefined,
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
        officialShortTitle: undefined,
        officialAbbreviation: undefined,
        referenceNumber: undefined,
        publicationDate: undefined,
        announcementDate: undefined,
        citationDate: undefined,
        frameKeywords: undefined,
        authorEntity: undefined,
        authorDecidingBody: undefined,
        authorIsResolutionMajority: undefined,
        leadJurisdiction: undefined,
        leadUnit: undefined,
        participationType: undefined,
        participationInstitution: undefined,
        documentTypeName: undefined,
        documentNormCategory: undefined,
        documentTemplateName: undefined,
        subjectFna: undefined,
        subjectPreviousFna: undefined,
        subjectGesta: undefined,
        subjectBgb3: undefined,
      })
      expect(response.error?.title).toBe(
        "Dokumentationseinheit konnte nicht bearbeitet werden."
      )
    })
  })
})
